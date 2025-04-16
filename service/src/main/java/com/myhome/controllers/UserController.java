/*
 * Copyright 2020 Prathab Murugan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myhome.controllers;

import com.myhome.api.UsersApi;
import com.myhome.controllers.dto.UserDto;
import com.myhome.controllers.dto.mapper.HouseMemberMapper;
import com.myhome.controllers.mapper.UserApiMapper;
import com.myhome.domain.PasswordActionType;
import com.myhome.domain.User;
import com.myhome.model.CreateUserRequest;
import com.myhome.model.CreateUserResponse;
import com.myhome.model.ForgotPasswordRequest;
import com.myhome.model.GetUserDetailsResponse;
import com.myhome.model.GetUserDetailsResponseUser;
import com.myhome.model.ListHouseMembersResponse;
import com.myhome.services.HouseService;
import com.myhome.services.UserService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * Controller for facilitating user actions.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class UserController implements UsersApi {

  private final UserService userService;
  private final UserApiMapper userApiMapper;
  private final HouseService houseService;
  private final HouseMemberMapper houseMemberMapper;

  /**
   * Handles user sign up requests by mapping the request to a UserDto, creating a new
   * user, and returning a CreateUserResponse with a 201 status if the user is created
   * successfully, or a 409 status if the user already exists.
   *
   * @param request CreateUserRequest object that contains the data for a new user to
   * be created.
   *
   * Deconstruct the `CreateUserRequest` object to contain `email`, `username`, `password`,
   * and `role`.
   *
   * @returns a `ResponseEntity` containing either a `CreateUserResponse` or a conflict
   * status.
   *
   * Contain a `status` attribute representing the HTTP status code, and a `body` attribute.
   */
  @Override
  public ResponseEntity<CreateUserResponse> signUp(@Valid CreateUserRequest request) {
    log.trace("Received SignUp request");
    UserDto requestUserDto = userApiMapper.createUserRequestToUserDto(request);
    Optional<UserDto> createdUserDto = userService.createUser(requestUserDto);
    return createdUserDto
        .map(userDto -> {
          CreateUserResponse response = userApiMapper.userDtoToCreateUserResponse(userDto);
          return ResponseEntity.status(HttpStatus.CREATED).body(response);
        })
        .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());
  }

  /**
   * Handles a request to retrieve a list of all users, mapping the user data to a
   * response object using a mapper, and returns the response as a HTTP OK status with
   * the user details.
   *
   * @param pageable pagination criteria for retrieving a subset of users from the database.
   *
   * Destructure pageable into its properties:
   * - int page: represents the current page number
   * - int size: represents the number of items per page
   * - Sort sort: represents the sorting criteria
   *
   * @returns a ResponseEntity containing a list of user details in GetUserDetailsResponse
   * format.
   *
   * Contain a set of User objects.
   */
  @Override
  public ResponseEntity<GetUserDetailsResponse> listAllUsers(Pageable pageable) {
    log.trace("Received request to list all users");

    Set<User> userDetails = userService.listAll(pageable);
    Set<GetUserDetailsResponseUser> userDetailsResponse =
        userApiMapper.userSetToRestApiResponseUserSet(userDetails);

    GetUserDetailsResponse response = new GetUserDetailsResponse();
    response.setUsers(userDetailsResponse);

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Handles a request to retrieve user details based on a provided `userId`. It calls
   * the `getUserDetails` method of the `userService` and maps the response to a
   * `GetUserDetailsResponseUser` object, returning a successful response with a 200
   * status code or a 404 status code if the user is not found.
   *
   * @param userId unique identifier of the user for whom the details are being requested.
   *
   * @returns a ResponseEntity containing a GetUserDetailsResponseUser object or an
   * HTTP NOT_FOUND status.
   */
  @Override
  public ResponseEntity<GetUserDetailsResponseUser> getUserDetails(String userId) {
    log.trace("Received request to get details of user with Id[{}]", userId);

    return userService.getUserDetails(userId)
        .map(userApiMapper::userDtoToGetUserDetailsResponse)
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  /**
   * Handles password-related actions, specifically handling "forgot" and "reset"
   * password requests. It parses the action type, calls the corresponding service
   * method, and returns a successful response if the action is successful, otherwise
   * a bad request response.
   *
   * @param action type of password action to be performed, either FORGET or RESET.
   *
   * @param forgotPasswordRequest request data for password reset or recovery actions.
   *
   * Contain email.
   *
   * @returns either an HTTP 200 OK response or an HTTP 400 Bad Request response.
   *
   * Returned output is of type `ResponseEntity<Void>`. It has a status code that is
   * either OK (200) if the action is successful, or BAD_REQUEST (400) if it fails.
   */
  @Override
  public ResponseEntity<Void> usersPasswordPost(@NotNull @Valid String action, @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
    boolean result = false;
    PasswordActionType parsedAction = PasswordActionType.valueOf(action);
    if (parsedAction == PasswordActionType.FORGOT) {
      result = true;
      userService.requestResetPassword(forgotPasswordRequest);
    } else if (parsedAction == PasswordActionType.RESET) {
      result = userService.resetPassword(forgotPasswordRequest);
    }
    if (result) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Handles a request to list all members of all houses associated with a given user
   * ID, returning a response entity containing the list of house members if found, or
   * a 404 not found response if the user ID is invalid.
   *
   * @param userId identifier of the user whose houses' members are being listed.
   *
   * @param pageable pagination criteria used to retrieve a subset of results, typically
   * controlling the page number and size.
   *
   * Pageable is an object with three main properties:
   * - `pageNumber`: the page number to be returned.
   * - `pageSize`: the number of items to be returned per page.
   * - `sort`: a list of Sort objects specifying the sort order for the result.
   *
   * @returns a ResponseEntity containing a ListHouseMembersResponse with a set of house
   * members.
   *
   * The output is a `ResponseEntity` object, which contains a `ListHouseMembersResponse`
   * body and a status code.
   */
  @Override
  public ResponseEntity<ListHouseMembersResponse> listAllHousemates(String userId, Pageable pageable) {
    log.trace("Received request to list all members of all houses of user with Id[{}]", userId);

    return houseService.listHouseMembersForHousesOfUserId(userId, pageable)
            .map(HashSet::new)
            .map(houseMemberMapper::houseMemberSetToRestApiResponseHouseMemberSet)
            .map(houseMembers -> new ListHouseMembersResponse().members(houseMembers))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Confirms a user's email by calling the `userService.confirmEmail` method, which
   * likely verifies an email confirmation token. If the verification is successful,
   * it returns a successful response; otherwise, it returns a bad request response.
   *
   * @param userId unique identifier of the user whose email confirmation is being verified.
   *
   * @param emailConfirmToken token used to confirm the user's email address.
   *
   * @returns either a successful response (200 OK) or an error response (400 Bad Request).
   */
  @Override
  public ResponseEntity<Void> confirmEmail(String userId, String emailConfirmToken) {
    boolean emailConfirmed = userService.confirmEmail(userId, emailConfirmToken);
    if(emailConfirmed) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Resends a confirmation email to a user. It calls the `resendEmailConfirm` method
   * in the `userService` to perform the actual resend operation. The function returns
   * a successful response if the resend is successful, or a bad request response otherwise.
   *
   * @param userId unique identifier for the user whose email confirmation is to be resent.
   *
   * @returns either a successful HTTP 200 response or an unsuccessful HTTP 400 response.
   */
  @Override
  public ResponseEntity<Void> resendConfirmEmailMail(String userId) {
    boolean emailConfirmResend = userService.resendEmailConfirm(userId);
    if(emailConfirmResend) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.badRequest().build();
    }
  }
}
