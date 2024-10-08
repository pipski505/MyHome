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
 * Handles user-related operations, including user creation, listing, and password
 * management through RESTful API endpoints.
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
   * Handles user sign-up by mapping a `CreateUserRequest` to a `UserDto`, creating a
   * new user using the `userService`, and returning a `CreateUserResponse` with a
   * `HttpStatus.CREATED` status if successful or `HttpStatus.CONFLICT` if the user
   * already exists.
   *
   * @param request CreateUserRequest object that contains the data for a new user to
   * be created.
   *
   * Extract the properties of the `CreateUserRequest` object:
   *
   * - email: a string representing the user's email address
   *
   * @returns a ResponseEntity containing a CreateUserResponse with a status of CREATED
   * or a conflict status.
   *
   * The output is a `ResponseEntity` with a `CreateUserResponse` body.
   * The response entity contains a status code, which is either `201 Created` or `409
   * Conflict`.
   * The body of the response entity is a `CreateUserResponse` object.
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
   * Returns a list of all users in a paginated format. It retrieves the list from a
   * `userService` instance, maps it to a response format using a `userApiMapper`, and
   * returns the result as a `ResponseEntity` with a status code of 200 (OK).
   *
   * @param pageable pagination criteria for retrieving a subset of users from the database.
   *
   * Destructure: pageable is an object with properties page, size, and sort.
   *
   * Properties:
   * - page: the current page number
   * - size: the number of items per page
   * - sort: the sorting criteria
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
   * Retrieves user details for a specified `userId`, maps the response to
   * `GetUserDetailsResponseUser`, and returns a `ResponseEntity` with a status code
   * of 200 (OK) if found or 404 (NOT_FOUND) if not.
   *
   * @param userId identifier of the user whose details are being requested.
   *
   * @returns a ResponseEntity containing a GetUserDetailsResponseUser object or a 404
   * response if the user is not found.
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
   * Handles password-related requests by parsing the action type and calling the
   * corresponding service method. It returns a successful response if the action is
   * successful, otherwise it returns a bad request response.
   *
   * @param action type of password action being requested, such as forgetting or
   * resetting a password.
   *
   * @param forgotPasswordRequest request to either reset or request a password reset,
   * depending on the action specified.
   *
   * Have the properties of `forgotPasswordRequest` as follows:
   *
   * - email: the email address of the user requesting a password reset.
   * - password: the new password for the user's account.
   *
   * @returns a ResponseEntity with either an OK status (200) or a Bad Request status
   * (400).
   *
   * The output is a `ResponseEntity` with a `Void` body, indicating no content. It has
   * a HTTP status code of 200 (OK) if the action is successful or 400 (Bad Request) otherwise.
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
   * ID, returning a list of house members in a REST API response format.
   *
   * @param userId identifier of the user for whom all house members are to be listed.
   *
   * @param pageable pagination criteria for the response, allowing the retrieval of a
   * subset of the total results.
   *
   * Destructure:
   *   - `pageable` is of type `Pageable` which is an interface in Spring Data.
   *   - It has several properties that can be destructured, including `pageNumber`,
   * `pageSize`, `sort`, `offset`, and `pageSize`.
   *
   * Main properties include:
   *   - `pageNumber` and `pageSize`: These properties are used to determine the page
   * number and size of the data to be returned.
   *   - `sort`: This property is used to sort the data returned from the database.
   *   - `offset`: This property is used to specify the starting point of the data to
   * be returned.
   *
   * @returns a ResponseEntity containing a List of House Members in a REST API response
   * format.
   *
   * The output is a `ResponseEntity` object containing a `ListHouseMembersResponse`
   * object. The `ListHouseMembersResponse` object contains a `members` attribute which
   * is a set of `houseMemberSet` objects.
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
   * Verifies a user's email confirmation by calling the `userService.confirmEmail`
   * method, passing in the user's ID and email confirmation token. It returns a
   * successful response if the email is confirmed and a bad request response otherwise.
   *
   * @param userId unique identifier of the user being confirmed.
   *
   * @param emailConfirmToken token used to confirm the user's email address.
   *
   * @returns a ResponseEntity with a 200 status code for a confirmed email or a 400
   * status code for an invalid confirmation.
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
   * Handles email confirmation resend requests by calling the `userService.resendEmailConfirm`
   * method and returns a successful response if the request is successful, otherwise
   * returns a bad request response.
   *
   * @param userId unique identifier for the user whose email confirmation mail is to
   * be resent.
   *
   * @returns either a successful HTTP 200 response or a bad request HTTP 400 response.
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
