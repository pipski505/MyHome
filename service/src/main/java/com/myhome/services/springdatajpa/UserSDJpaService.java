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

package com.myhome.services.springdatajpa;

import com.myhome.controllers.dto.UserDto;
import com.myhome.controllers.dto.mapper.UserMapper;
import com.myhome.domain.Community;
import com.myhome.domain.SecurityToken;
import com.myhome.domain.SecurityTokenType;
import com.myhome.domain.User;
import com.myhome.model.ForgotPasswordRequest;
import com.myhome.repositories.UserRepository;
import com.myhome.services.MailService;
import com.myhome.services.SecurityTokenService;
import com.myhome.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements {@link UserService} and uses Spring Data JPA repository to does its work.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserSDJpaService implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final SecurityTokenService securityTokenService;
  private final MailService mailService;

  /**
   * Creates a new user in the repository, generates a unique user ID, encrypts the
   * user password, sends an email confirmation token, and returns the new user as an
   * Optional. If a user with the same email already exists, an empty Optional is returned.
   *
   * @param request data to be used when creating a new user, including the user's
   * details and password.
   *
   * Destructure the `request` object to reveal: email.
   *
   * @returns an Optional containing a UserDto object if a new user is created, otherwise
   * an empty Optional.
   *
   * The returned output is an `Optional` containing a `UserDto` object with properties
   * including a unique user ID, encrypted password, and other user information.
   */
  @Override
  public Optional<UserDto> createUser(UserDto request) {
    if (userRepository.findByEmail(request.getEmail()) == null) {
      generateUniqueUserId(request);
      encryptUserPassword(request);
      User newUser = createUserInRepository(request);
      SecurityToken emailConfirmToken = securityTokenService.createEmailConfirmToken(newUser);
      mailService.sendAccountCreated(newUser, emailConfirmToken);
      UserDto newUserDto = userMapper.userToUserDto(newUser);
      return Optional.of(newUserDto);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns a set of all users, utilizing pagination with a page size of 200.
   *
   * @returns a set of `User` objects.
   */
  @Override
  public Set<User> listAll() {
    return listAll(PageRequest.of(0, 200));
  }

  /**
   * Retrieves all users from the database,
   * paginates the results according to the provided `Pageable` object,
   * and returns the result as a `Set` of `User` objects.
   *
   * @param pageable criteria for pagination, allowing the retrieval of a subset of
   * data from the database.
   *
   * @returns a set of User objects, paginated according to the provided Pageable object.
   */
  @Override
  public Set<User> listAll(Pageable pageable) {
    return userRepository.findAll(pageable).toSet();
  }

  /**
   * Retrieves user details from the database based on the provided user ID. It maps
   * the retrieved user object to a `UserDto` object, populates the `communityIds`
   * field, and returns the `UserDto` object wrapped in an `Optional`.
   *
   * @param userId identifier for the user whose details are being retrieved.
   *
   * @returns an Optional containing a UserDto object with the user's communities.
   *
   * The output is an `Optional` of `UserDto` containing the user details and a set of
   * community IDs.
   */
  @Override
  public Optional<UserDto> getUserDetails(String userId) {
    Optional<User> userOptional = userRepository.findByUserIdWithCommunities(userId);
    return userOptional.map(admin -> {
      Set<String> communityIds = admin.getCommunities().stream()
          .map(Community::getCommunityId)
          .collect(Collectors.toSet());

      UserDto userDto = userMapper.userToUserDto(admin);
      userDto.setCommunityIds(communityIds);
      return Optional.of(userDto);
    }).orElse(Optional.empty());
  }

  /**
   * Retains an existing user by email, converts it to a UserDto, and adds the user's
   * community IDs to the UserDto.
   *
   * @param userEmail email of the user to be searched for in the database.
   *
   * @returns an Optional containing a UserDto object with community IDs.
   *
   * Return type is an `Optional` containing a `UserDto` object. The `UserDto` object
   * has properties such as `communityIds` which is a `Set` of `String` community IDs.
   */
  public Optional<UserDto> findUserByEmail(String userEmail) {
    return Optional.ofNullable(userRepository.findByEmail(userEmail))
        .map(user -> {
          Set<String> communityIds = user.getCommunities().stream()
              .map(Community::getCommunityId)
              .collect(Collectors.toSet());

          UserDto userDto = userMapper.userToUserDto(user);
          userDto.setCommunityIds(communityIds);
          return userDto;
        });
  }

  /**
   * Processes a password reset request by validating the email, checking if a user
   * exists, generating a new password reset token, adding it to the user, sending a
   * password recovery code via email, and returning true if successful.
   *
   * @param forgotPasswordRequest request to reset a password, containing the email
   * address of the user whose password is to be reset.
   *
   * Contain email.
   *
   * @returns a boolean indicating whether the password reset request was successful
   * or not.
   */
  @Override
  public boolean requestResetPassword(ForgotPasswordRequest forgotPasswordRequest) {
    return Optional.ofNullable(forgotPasswordRequest)
        .map(ForgotPasswordRequest::getEmail)
        .flatMap(email -> userRepository.findByEmailWithTokens(email)
            .map(user -> {
              SecurityToken newSecurityToken = securityTokenService.createPasswordResetToken(user);
              user.getUserTokens().add(newSecurityToken);
              userRepository.save(user);
              return mailService.sendPasswordRecoverCode(user, newSecurityToken.getToken());
            }))
        .orElse(false);
  }

  /**
   * Resets a user's password when provided with a valid security token, new password,
   * and email. It retrieves the user with the matching email and token, then saves the
   * new password and sends a confirmation email. The function returns true if successful.
   *
   * @param passwordResetRequest input data containing the email, token, and new password
   * for password reset operations.
   *
   * Extract the `getEmail` method from `ForgotPasswordRequest`.
   *
   * @returns a boolean indicating whether the password reset was successful.
   */
  @Override
  public boolean resetPassword(ForgotPasswordRequest passwordResetRequest) {
    final Optional<User> userWithToken = Optional.ofNullable(passwordResetRequest)
        .map(ForgotPasswordRequest::getEmail)
        .flatMap(userRepository::findByEmailWithTokens);
    return userWithToken
        .flatMap(user -> findValidUserToken(passwordResetRequest.getToken(), user, SecurityTokenType.RESET))
        .map(securityTokenService::useToken)
        .map(token -> saveTokenForUser(userWithToken.get(), passwordResetRequest.getNewPassword()))
        .map(mailService::sendPasswordSuccessfullyChanged)
        .orElse(false);
  }

  /**
   * Verifies an email confirmation by checking if a user's email confirmation token
   * matches the provided token and the user's email is not already confirmed. If valid,
   * it confirms the user's email and returns true, otherwise returns false.
   *
   * @param userId identifier of the user whose email confirmation is being verified.
   *
   * @param emailConfirmToken token used to confirm an email address for a user.
   *
   * @returns a boolean indicating whether the email was confirmed successfully.
   *
   * The returned output is a Boolean value indicating whether the email confirmation
   * was successful.
   */
  @Override
  public Boolean confirmEmail(String userId, String emailConfirmToken) {
    final Optional<User> userWithToken = userRepository.findByUserIdWithTokens(userId);
    Optional<SecurityToken> emailToken = userWithToken
        .filter(user -> !user.isEmailConfirmed())
        .map(user -> findValidUserToken(emailConfirmToken, user, SecurityTokenType.EMAIL_CONFIRM)
        .map(token -> {
          confirmEmail(user);
          return token;
        })
        .map(securityTokenService::useToken)
        .orElse(null));
    return emailToken.map(token -> true).orElse(false);
  }

  /**
   * Resends an email confirmation to a user if their email has not been confirmed. It
   * removes any existing unused email confirmation tokens and saves the updated user
   * data before sending the email.
   *
   * @param userId unique identifier of the user for whom the email confirmation is
   * being resent.
   *
   * @returns a boolean value indicating success of email confirmation or failure.
   */
  @Override
  public boolean resendEmailConfirm(String userId) {
    return userRepository.findByUserId(userId).map(user -> {
      if(!user.isEmailConfirmed()) {
        SecurityToken emailConfirmToken = securityTokenService.createEmailConfirmToken(user);
        user.getUserTokens().removeIf(token -> token.getTokenType() == SecurityTokenType.EMAIL_CONFIRM && !token.isUsed());
        userRepository.save(user);
        boolean mailSend = mailService.sendAccountCreated(user, emailConfirmToken);
        return mailSend;
      } else {
        return false;
      }
    }).orElse(false);
  }

  /**
   * Encrypts a new password for a given user and updates the user's encrypted password
   * in the database, then persists the updated user object.
   *
   * @param user entity for which a new password is being saved, allowing its properties
   * to be modified.
   *
   * @param newPassword password to be encrypted and stored for the specified user.
   *
   * @returns the saved `User` object with the updated encrypted password.
   */
  private User saveTokenForUser(User user, String newPassword) {
    user.setEncryptedPassword(passwordEncoder.encode(newPassword));
    return userRepository.save(user);
  }

  /**
   * Checks for a valid security token among the user's tokens. It filters tokens by
   * type, usage status, token value, and expiry date, then returns the first matching
   * token if found.
   *
   * @param token security token to be validated against the user's tokens.
   *
   * @param user user object from which the function retrieves a list of user tokens.
   *
   * @param securityTokenType type of security token being searched for, and is used
   * in the filter condition to narrow down the search.
   *
   * @returns an `Optional` containing a valid `SecurityToken` if found, or an empty
   * `Optional` otherwise.
   */
  private Optional<SecurityToken> findValidUserToken(String token, User user, SecurityTokenType securityTokenType) {
    Optional<SecurityToken> userPasswordResetToken = user.getUserTokens()
        .stream()
        .filter(tok -> !tok.isUsed()
            && tok.getTokenType() == securityTokenType
            && tok.getToken().equals(token)
            && tok.getExpiryDate().isAfter(LocalDate.now()))
        .findFirst();
    return userPasswordResetToken;
  }

  /**
   * Maps a `UserDto` object to a `User` entity, logs the user ID, and saves the `User`
   * entity to a repository using the `userRepository` service.
   *
   * @param request data to be converted into a `User` object using the `userMapper`.
   *
   * @returns a User object saved by the userRepository.
   */
  private User createUserInRepository(UserDto request) {
    User user = userMapper.userDtoToUser(request);
    log.trace("saving user with id[{}] to repository", request.getId());
    return userRepository.save(user);
  }

  /**
   * Marks a user's email as confirmed, sends a confirmation email to the user, and
   * updates the user's record in the database.
   *
   * @param user user object being updated with a confirmed email status and saved to
   * the database.
   */
  private void confirmEmail(User user) {
    user.setEmailConfirmed(true);
    mailService.sendAccountConfirmed(user);
    userRepository.save(user);
  }

  /**
   * Encrypts a user's password using a password encoder, replaces the original password
   * with the encrypted version, and updates the `encryptedPassword` field in the
   * `UserDto` object.
   *
   * @param request User object containing the password to be encrypted.
   */
  private void encryptUserPassword(UserDto request) {
    request.setEncryptedPassword(passwordEncoder.encode(request.getPassword()));
  }

  /**
   * Generates a unique identifier for a user based on a random UUID.
   * The identifier is then assigned to the user's ID in the `UserDto` object.
   * The identifier is a string representation of the UUID.
   *
   * @param request object to which a unique user ID is assigned.
   */
  private void generateUniqueUserId(UserDto request) {
    request.setUserId(UUID.randomUUID().toString());
  }
}
