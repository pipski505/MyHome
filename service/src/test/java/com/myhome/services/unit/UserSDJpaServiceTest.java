package com.myhome.services.unit;

import com.myhome.controllers.dto.UserDto;
import com.myhome.controllers.dto.mapper.UserMapper;
import com.myhome.model.ForgotPasswordRequest;
import com.myhome.domain.Community;
import com.myhome.domain.SecurityToken;
import com.myhome.domain.SecurityTokenType;
import com.myhome.domain.User;
import com.myhome.repositories.SecurityTokenRepository;
import com.myhome.repositories.UserRepository;
import com.myhome.services.springdatajpa.MailSDJpaService;
import com.myhome.services.springdatajpa.SecurityTokenSDJpaService;
import com.myhome.services.springdatajpa.UserSDJpaService;
import helpers.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Provides a
 */
class UserSDJpaServiceTest {

  private final String USER_ID = "test-user-id";
  private final String USERNAME = "test-user-name";
  private final String USER_EMAIL = "test-user-email";
  private final String USER_PASSWORD = "test-user-password";
  private final String NEW_USER_PASSWORD = "test-user-new-password";
  private final String PASSWORD_RESET_TOKEN = "test-token";
  private final Duration TOKEN_LIFETIME = Duration.ofDays(1);

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserMapper userMapper;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private SecurityTokenSDJpaService securityTokenService;
  @Mock
  private MailSDJpaService mailService;
  @Mock
  private SecurityTokenRepository securityTokenRepository;
  @InjectMocks
  private UserSDJpaService userService;

  /**
   * Initializes Mockito annotations for the test class, setting up mock objects for
   * test execution.
   */
  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Tests the functionality of creating a new user with email confirmation. It verifies
   * that the user is created successfully, a security token is generated, and the user
   * data is correctly mapped between the request and response objects.
   */
  @Test
  void createUserSuccess() {
    // given
    UserDto request = getDefaultUserDtoRequest();
    User resultUser = getUserFromDto(request);
    UserDto response = UserDto.builder()
        .id(resultUser.getId())
        .userId(resultUser.getUserId())
        .name(resultUser.getName())
        .encryptedPassword(resultUser.getEncryptedPassword())
        .communityIds(new HashSet<>())
        .build();
    SecurityToken emailConfirmToken =
        getSecurityToken(SecurityTokenType.EMAIL_CONFIRM, "token", resultUser);

    given(userRepository.findByEmail(request.getEmail()))
        .willReturn(null);
    given(passwordEncoder.encode(request.getPassword()))
        .willReturn(request.getPassword());
    given(userMapper.userDtoToUser(request))
        .willReturn(resultUser);
    given(userRepository.save(resultUser))
        .willReturn(resultUser);
    given(userMapper.userToUserDto(resultUser))
        .willReturn(response);
    given(securityTokenService.createEmailConfirmToken(resultUser))
        .willReturn(emailConfirmToken);

    // when
    Optional<UserDto> createdUserDtoOptional = userService.createUser(request);

    // then
    assertTrue(createdUserDtoOptional.isPresent());
    UserDto createdUserDto = createdUserDtoOptional.get();
    assertEquals(response, createdUserDto);
    assertEquals(0, createdUserDto.getCommunityIds().size());
    verify(userRepository).findByEmail(request.getEmail());
    verify(passwordEncoder).encode(request.getPassword());
    verify(userRepository).save(resultUser);
    verify(securityTokenService).createEmailConfirmToken(resultUser);
  }

  /**
   * Tests the functionality of creating a user when the email already exists in the
   * database. It verifies that the `createUser` method returns an empty `Optional` and
   * that the `findByEmail` method is called on the `userRepository`.
   */
  @Test
  void createUserEmailExists() {
    // given
    UserDto request = getDefaultUserDtoRequest();
    User user = getUserFromDto(request);

    given(userRepository.findByEmail(request.getEmail()))
        .willReturn(user);

    // when
    Optional<UserDto> createdUserDto = userService.createUser(request);

    // then
    assertFalse(createdUserDto.isPresent());
    verify(userRepository).findByEmail(request.getEmail());
  }

  /**
   * Tests the retrieval of user details from a database using the `userService` class,
   * given a user ID, and verifies that the retrieved details match the expected results.
   */
  @Test
  void getUserDetailsSuccess() {
    // given
    UserDto userDto = getDefaultUserDtoRequest();
    User user = getUserFromDto(userDto);

    given(userRepository.findByUserIdWithCommunities(USER_ID))
        .willReturn(Optional.of(user));
    given(userMapper.userToUserDto(user))
        .willReturn(userDto);

    // when
    Optional<UserDto> createdUserDtoOptional = userService.getUserDetails(USER_ID);

    // then
    assertTrue(createdUserDtoOptional.isPresent());
    UserDto createdUserDto = createdUserDtoOptional.get();
    assertEquals(userDto, createdUserDto);
    assertEquals(0, createdUserDto.getCommunityIds().size());
    verify(userRepository).findByUserIdWithCommunities(USER_ID);
  }

  /**
   * Tests the retrieval of user details with associated community IDs from the database.
   * It uses a mock repository to simulate a successful database query and verifies
   * that the user details are correctly mapped and returned.
   */
  @Test
  void getUserDetailsSuccessWithCommunityIds() {
    // given
    UserDto userDto = getDefaultUserDtoRequest();
    User user = new User(userDto.getName(), userDto.getUserId(), userDto.getEmail(), false,
        userDto.getEncryptedPassword(), new HashSet<>(), null);

    Community firstCommunity = TestUtils.CommunityHelpers.getTestCommunity(user);
    Community secCommunity = TestUtils.CommunityHelpers.getTestCommunity(user);

    Set<Community> communities =
        Stream.of(firstCommunity, secCommunity).collect(Collectors.toSet());

    Set<String> communitiesIds = communities
        .stream()
        .map(community -> community.getCommunityId())
        .collect(Collectors.toSet());

    given(userRepository.findByUserIdWithCommunities(USER_ID))
        .willReturn(Optional.of(user));
    given(userMapper.userToUserDto(user))
        .willReturn(userDto);

    // when
    Optional<UserDto> createdUserDtoOptional = userService.getUserDetails(USER_ID);

    // then
    assertTrue(createdUserDtoOptional.isPresent());
    UserDto createdUserDto = createdUserDtoOptional.get();
    assertEquals(userDto, createdUserDto);
    assertEquals(communitiesIds, createdUserDto.getCommunityIds());
    verify(userRepository).findByUserIdWithCommunities(USER_ID);
  }

  /**
   * Tests the `getUserDetails` function's behavior when a user with the specified ID
   * is not found in the database. It checks if the function returns an empty Optional
   * and verifies that the repository's `findByUserIdWithCommunities` method is called
   * with the correct ID.
   */
  @Test
  void getUserDetailsNotFound() {
    // given
    given(userRepository.findByUserIdWithCommunities(USER_ID))
        .willReturn(Optional.empty());

    // when
    Optional<UserDto> createdUserDto = userService.getUserDetails(USER_ID);

    // then
    assertFalse(createdUserDto.isPresent());
    verify(userRepository).findByUserIdWithCommunities(USER_ID);
  }

  /**
   * Confirms a user's email by verifying a security token. It updates the user's status
   * as email confirmed, saves the user to the repository, and uses the security token
   * service.
   */
  @Test
  void confirmEmail() {
    // given
    User user = getDefaultUser();
    SecurityToken testSecurityToken =
        getSecurityToken(SecurityTokenType.EMAIL_CONFIRM, TOKEN_LIFETIME, PASSWORD_RESET_TOKEN,
            user);
    user.getUserTokens().add(testSecurityToken);
    given(securityTokenService.useToken(testSecurityToken))
        .willReturn(testSecurityToken);
    given(userRepository.findByUserIdWithTokens(user.getUserId()))
        .willReturn(Optional.of(user));
    //    given(mailService.sendAccountConfirmed(user))
    //        .willReturn(true);

    // when
    boolean emailConfirmed =
        userService.confirmEmail(user.getUserId(), testSecurityToken.getToken());

    // then
    assertTrue(emailConfirmed);
    assertTrue(user.isEmailConfirmed());
    verify(securityTokenService).useToken(testSecurityToken);
    verify(userRepository).save(user);
    //    verify(mailService).sendAccountConfirmed(user);
  }

  /**
   * Tests the email confirmation functionality when an invalid security token is
   * provided. It checks that the email is not confirmed and that the user repository
   * and security token service are not accessed.
   */
  @Test
  void confirmEmailWrongToken() {
    // given
    User user = getDefaultUser();
    SecurityToken testSecurityToken =
        getSecurityToken(SecurityTokenType.EMAIL_CONFIRM, TOKEN_LIFETIME, PASSWORD_RESET_TOKEN,
            user);
    user.getUserTokens().add(testSecurityToken);
    given(userRepository.findByUserIdWithTokens(user.getUserId()))
        .willReturn(Optional.of(user));

    // when
    boolean emailConfirmed = userService.confirmEmail(user.getUserId(), "wrong-token");

    // then
    assertFalse(emailConfirmed);
    assertFalse(user.isEmailConfirmed());
    verify(userRepository, never()).save(user);
    verifyNoInteractions(securityTokenService);
    verifyNoInteractions(mailService);
  }

  /**
   * Checks if confirming an email using an already used token is successful.
   * It verifies that the email confirmation is denied and the user's email confirmation
   * status remains unchanged.
   * No interactions with the database or other services are expected.
   */
  @Test
  void confirmEmailUsedToken() {
    // given
    User user = getDefaultUser();
    SecurityToken testSecurityToken =
        getSecurityToken(SecurityTokenType.EMAIL_CONFIRM, TOKEN_LIFETIME, PASSWORD_RESET_TOKEN,
            user);
    testSecurityToken.setUsed(true);
    user.getUserTokens().add(testSecurityToken);
    given(userRepository.findByUserIdWithTokens(user.getUserId()))
        .willReturn(Optional.of(user));

    // when
    boolean emailConfirmed =
        userService.confirmEmail(user.getUserId(), testSecurityToken.getToken());

    // then
    assertFalse(emailConfirmed);
    assertFalse(user.isEmailConfirmed());
    verify(userRepository, never()).save(user);
    verifyNoInteractions(securityTokenService);
    verifyNoInteractions(mailService);
  }

  /**
   * Tests the confirmation of an email when no valid token is provided. It verifies
   * that the email remains unconfirmed, no interactions occur with the repository or
   * services, and no attempt is made to save the user.
   */
  @Test
  void confirmEmailNoToken() {
    // given
    User user = getDefaultUser();
    given(userRepository.findByUserIdWithTokens(user.getUserId()))
        .willReturn(Optional.of(user));

    // when
    boolean emailConfirmed = userService.confirmEmail(user.getUserId(), "any-token");

    // then
    assertFalse(emailConfirmed);
    assertFalse(user.isEmailConfirmed());
    verify(userRepository, never()).save(user);
    verifyNoInteractions(securityTokenService);
    verifyNoInteractions(mailService);
  }

  /**
   * Tests the email confirmation functionality when the email is already confirmed.
   * It verifies that the `confirmEmail` method returns `false` and that the user
   * repository and other services are not interacted with.
   */
  @Test
  void confirmEmailAlreadyConfirmed() {
    // given
    User user = getDefaultUser();
    SecurityToken testSecurityToken =
        getSecurityToken(SecurityTokenType.EMAIL_CONFIRM, TOKEN_LIFETIME, PASSWORD_RESET_TOKEN,
            user);
    user.getUserTokens().add(testSecurityToken);
    user.setEmailConfirmed(true);
    given(userRepository.findByUserIdWithTokens(user.getUserId()))
        .willReturn(Optional.of(user));

    // when
    boolean emailConfirmed =
        userService.confirmEmail(user.getUserId(), testSecurityToken.getToken());

    // then
    assertFalse(emailConfirmed);
    verify(userRepository, never()).save(user);
    verifyNoInteractions(securityTokenService);
    verifyNoInteractions(mailService);
  }

  /**
   * Tests the retrieval of a user by email from the database and verifies that the
   * user data is correctly mapped and returned. It checks for presence and equality
   * of the user data.
   */
  @Test
  void findUserByEmailSuccess() {
    // given
    UserDto userDto = getDefaultUserDtoRequest();
    User user = getUserFromDto(userDto);

    given(userRepository.findByEmail(USER_EMAIL))
        .willReturn(user);
    given(userMapper.userToUserDto(user))
        .willReturn(userDto);

    // when
    Optional<UserDto> resultUserDtoOptional = userService.findUserByEmail(USER_EMAIL);

    // then
    assertTrue(resultUserDtoOptional.isPresent());
    UserDto createdUserDto = resultUserDtoOptional.get();
    assertEquals(userDto, createdUserDto);
    assertEquals(0, createdUserDto.getCommunityIds().size());
    verify(userRepository).findByEmail(USER_EMAIL);
  }

  /**
   * Tests the retrieval of a user by email with associated community IDs. It mocks a
   * user repository to return a user object, then verifies the user service returns a
   * user DTO with the expected community IDs.
   */
  @Test
  void findUserByEmailSuccessWithCommunityIds() {
    // given
    UserDto userDto = getDefaultUserDtoRequest();
    User user = getUserFromDto(userDto);

    Community firstCommunity = TestUtils.CommunityHelpers.getTestCommunity(user);
    Community secCommunity = TestUtils.CommunityHelpers.getTestCommunity(user);

    Set<Community> communities =
        Stream.of(firstCommunity, secCommunity).collect(Collectors.toSet());

    Set<String> communitiesIds = communities
        .stream()
        .map(Community::getCommunityId)
        .collect(Collectors.toSet());

    given(userRepository.findByEmail(USER_EMAIL))
        .willReturn(user);
    given(userMapper.userToUserDto(user))
        .willReturn(userDto);

    // when
    Optional<UserDto> resultUserDtoOptional = userService.findUserByEmail(USER_EMAIL);

    // then
    assertTrue(resultUserDtoOptional.isPresent());
    UserDto createdUserDto = resultUserDtoOptional.get();
    assertEquals(userDto, createdUserDto);
    assertEquals(communitiesIds, createdUserDto.getCommunityIds());
    verify(userRepository).findByEmail(USER_EMAIL);
  }

  /**
   * Tests the `userService.findUserByEmail` method when a user with the specified email
   * is not found in the database. It verifies that the method returns an empty Optional
   * and that the `userRepository` is called with the correct email.
   */
  @Test
  void findUserByEmailNotFound() {
    // given
    given(userRepository.findByEmail(USER_EMAIL))
        .willReturn(null);

    // when
    Optional<UserDto> resultUserDtoOptional = userService.findUserByEmail(USER_EMAIL);

    // then
    assertFalse(resultUserDtoOptional.isPresent());
    verify(userRepository).findByEmail(USER_EMAIL);
  }

  /**
   * Requests a password reset for a user by creating a password reset token, sending
   * a recovery code via email, and updating the user's security tokens.
   */
  @Test
  void requestResetPassword() {
    // given
    ForgotPasswordRequest forgotPasswordRequest = getForgotPasswordRequest();
    User user = getDefaultUser();
    SecurityToken testSecurityToken =
        getSecurityToken(SecurityTokenType.RESET, TOKEN_LIFETIME, PASSWORD_RESET_TOKEN, null);
    given(securityTokenService.createPasswordResetToken(user))
        .willReturn(testSecurityToken);
    given(userRepository.findByEmailWithTokens(forgotPasswordRequest.getEmail()))
        .willReturn(Optional.of(user));
    given(mailService.sendPasswordRecoverCode(user, testSecurityToken.getToken()))
        .willReturn(true);

    // when
    boolean resetRequested = userService.requestResetPassword(forgotPasswordRequest);

    // then
    assertTrue(resetRequested);
    assertEquals(getUserSecurityToken(user, SecurityTokenType.RESET), testSecurityToken);
    verify(securityTokenService).createPasswordResetToken(user);
    verify(userRepository).findByEmailWithTokens(forgotPasswordRequest.getEmail());
    verify(userRepository).save(user);
    verify(mailService).sendPasswordRecoverCode(user, testSecurityToken.getToken());
  }

  /**
   * Tests the functionality of requesting a password reset when the user does not
   * exist. It verifies that no interactions occur with the security token service and
   * that the user repository is queried by email, but no user is saved.
   */
  @Test
  void requestResetPasswordUserNotExists() {
    // given
    ForgotPasswordRequest forgotPasswordRequest = getForgotPasswordRequest();
    User user = getDefaultUser();
    SecurityToken testSecurityToken =
        getSecurityToken(SecurityTokenType.RESET, TOKEN_LIFETIME, PASSWORD_RESET_TOKEN, user);
    given(securityTokenService.createPasswordResetToken(user))
        .willReturn(testSecurityToken);
    given(userRepository.findByEmailWithTokens(forgotPasswordRequest.getEmail()))
        .willReturn(Optional.empty());

    // when
    boolean resetRequested = userService.requestResetPassword(forgotPasswordRequest);

    // then
    assertFalse(resetRequested);
    assertNotEquals(getUserSecurityToken(user, SecurityTokenType.RESET), testSecurityToken);
    verifyNoInteractions(securityTokenService);
    verify(userRepository).findByEmailWithTokens(forgotPasswordRequest.getEmail());
    verify(userRepository, never()).save(user);
    verifyNoInteractions(mailService);
  }

  /**
   * Validates a user's request to reset their password, updates the user's encrypted
   * password, and sends a notification to the user that their password has been
   * successfully changed.
   */
  @Test
  void resetPassword() {
    // given
    ForgotPasswordRequest forgotPasswordRequest = getForgotPasswordRequest();
    User user = getDefaultUser();
    SecurityToken testSecurityToken =
        getSecurityToken(SecurityTokenType.RESET, TOKEN_LIFETIME, PASSWORD_RESET_TOKEN, user);
    user.getUserTokens().add(testSecurityToken);
    given(userRepository.findByEmailWithTokens(forgotPasswordRequest.getEmail()))
        .willReturn(Optional.of(user));
    given(passwordEncoder.encode(forgotPasswordRequest.getNewPassword()))
        .willReturn(forgotPasswordRequest.getNewPassword());
    when(userRepository.save(user))
        .then(returnsFirstArg());
    given(mailService.sendPasswordSuccessfullyChanged(user))
        .willReturn(true);
    given(securityTokenService.useToken(testSecurityToken))
        .willReturn(testSecurityToken);

    // when
    boolean passwordChanged = userService.resetPassword(forgotPasswordRequest);

    // then
    assertTrue(passwordChanged);
    assertEquals(user.getEncryptedPassword(), forgotPasswordRequest.getNewPassword());
    verify(userRepository).findByEmailWithTokens(forgotPasswordRequest.getEmail());
    verify(passwordEncoder).encode(forgotPasswordRequest.getNewPassword());
    verify(mailService).sendPasswordSuccessfullyChanged(user);
    verify(securityTokenService).useToken(testSecurityToken);
  }

  /**
   * Tests the behavior of resetting a user password when the user does not exist in
   * the system. It verifies that the password is not changed and that the correct
   * interactions occur with the repository and other services.
   */
  @Test
  void resetPasswordUserNotExists() {
    // given
    ForgotPasswordRequest forgotPasswordRequest = getForgotPasswordRequest();
    User user = getDefaultUser();
    SecurityToken testSecurityToken =
        getSecurityToken(SecurityTokenType.RESET, TOKEN_LIFETIME, PASSWORD_RESET_TOKEN, user);
    user.getUserTokens().add(testSecurityToken);
    ;
    given(userRepository.findByEmailWithTokens(forgotPasswordRequest.getEmail()))
        .willReturn(Optional.empty());

    // when
    boolean passwordChanged = userService.resetPassword(forgotPasswordRequest);

    // then
    assertFalse(passwordChanged);
    assertNotEquals(user.getEncryptedPassword(), forgotPasswordRequest.getNewPassword());
    verify(userRepository).findByEmailWithTokens(forgotPasswordRequest.getEmail());
    verifyNoInteractions(securityTokenRepository);
    verifyNoInteractions(passwordEncoder);
    verifyNoInteractions(mailService);
  }

  /**
   * Tests the functionality of resetting a password when the associated token has
   * expired. It verifies that the password change is unsuccessful and the user's
   * security token remains unused.
   */
  @Test
  void resetPasswordTokenExpired() {
    // given
    ForgotPasswordRequest forgotPasswordRequest = getForgotPasswordRequest();
    SecurityToken testSecurityToken = getExpiredTestToken();
    User user = getDefaultUser();
    user.getUserTokens().add(testSecurityToken);
    ;
    given(userRepository.findByEmailWithTokens(forgotPasswordRequest.getEmail()))
        .willReturn(Optional.of(user));

    // when
    boolean passwordChanged = userService.resetPassword(forgotPasswordRequest);

    // then
    assertFalse(passwordChanged);
    assertNotEquals(user.getEncryptedPassword(), forgotPasswordRequest.getNewPassword());
    assertFalse(getUserSecurityToken(user, SecurityTokenType.RESET).isUsed());
    verify(userRepository).findByEmailWithTokens(forgotPasswordRequest.getEmail());
    verifyNoInteractions(securityTokenRepository);
    verifyNoInteractions(passwordEncoder);
    verifyNoInteractions(mailService);
  }

  /**
   * Tests the functionality of resetting a password when the corresponding token does
   * not exist in the database. It verifies that the password remains unchanged, and
   * no interactions occur with the security token repository, password encoder, or
   * mail service.
   */
  @Test
  void resetPasswordTokenNotExists() {
    // given
    ForgotPasswordRequest forgotPasswordRequest = getForgotPasswordRequest();
    User user = getDefaultUser();
    given(userRepository.findByEmailWithTokens(forgotPasswordRequest.getEmail()))
        .willReturn(Optional.of(user));

    // when
    boolean passwordChanged = userService.resetPassword(forgotPasswordRequest);

    // then
    assertFalse(passwordChanged);
    assertNotEquals(user.getEncryptedPassword(), forgotPasswordRequest.getNewPassword());
    verify(userRepository).findByEmailWithTokens(forgotPasswordRequest.getEmail());
    verifyNoInteractions(securityTokenRepository);
    verifyNoInteractions(passwordEncoder);
    verifyNoInteractions(mailService);
  }

  /**
   * Tests the functionality of resetting a user's password when the provided token
   * does not match. It verifies that the password remains unchanged and no interactions
   * occur with the security token repository, password encoder, or mail service.
   */
  @Test
  void resetPasswordTokenNotMatches() {
    // given
    ForgotPasswordRequest forgotPasswordRequest = getForgotPasswordRequest();
    SecurityToken testSecurityToken =
        getSecurityToken(SecurityTokenType.RESET, TOKEN_LIFETIME, PASSWORD_RESET_TOKEN, null);
    testSecurityToken.setToken("wrong-token");
    User user = getDefaultUser();
    user.getUserTokens().add(testSecurityToken);
    ;
    given(userRepository.findByEmailWithTokens(forgotPasswordRequest.getEmail()))
        .willReturn(Optional.of(user));

    // when
    boolean passwordChanged = userService.resetPassword(forgotPasswordRequest);

    // then
    assertFalse(passwordChanged);
    assertNotEquals(user.getEncryptedPassword(), forgotPasswordRequest.getNewPassword());
    assertNotNull(getUserSecurityToken(user, SecurityTokenType.RESET));
    verify(userRepository).findByEmailWithTokens(forgotPasswordRequest.getEmail());
    verifyNoInteractions(securityTokenRepository);
    verifyNoInteractions(passwordEncoder);
    verifyNoInteractions(mailService);
  }

  /**
   * Constructs and returns a UserDto object with predefined user details. The object
   * is built with a user ID, name, email, and encrypted password, while community IDs
   * are initialized as an empty set.
   *
   * @returns a UserDto object with default values for user information.
   */
  private UserDto getDefaultUserDtoRequest() {
    return UserDto.builder()
        .userId(USER_ID)
        .name(USERNAME)
        .email(USER_EMAIL)
        .encryptedPassword(USER_PASSWORD)
        .communityIds(new HashSet<>())
        .build();
  }

  /**
   * Creates a new `User` object from the provided `UserDto` object. It copies the name,
   * user ID, email, and encrypted password from the `UserDto` to the `User` object.
   *
   * @param request UserDto object from which the User object is being created.
   *
   * Decompose.
   * Extract the properties of the input object: name, userId, email, encryptedPassword,
   * and two empty sets.
   *
   * @returns an instance of the `User` class.
   *
   * Comprise a User object with name, userId, email, active status, encrypted password,
   * a set of roles, and a set of permissions.
   */
  private User getUserFromDto(UserDto request) {
    return new User(
        request.getName(),
        request.getUserId(),
        request.getEmail(),
        false,
        request.getEncryptedPassword(),
        new HashSet<>(),
        new HashSet<>()
    );
  }

  /**
   * Returns the first security token of a specified type associated with a given user,
   * or null if no such token exists.
   * It uses a stream to filter user tokens by type and finds the first match.
   *
   * @param user entity from which a security token is to be retrieved.
   *
   * @param tokenType type of security token to be retrieved for the specified user.
   *
   * @returns a SecurityToken object if found, or null if no matching token is present.
   */
  private SecurityToken getUserSecurityToken(User user, SecurityTokenType tokenType) {
    return user.getUserTokens()
        .stream()
        .filter(token -> token.getTokenType() == tokenType)
        .findFirst()
        .orElse(null);
  }

  /**
   * Retrieves the default user by calling the `getDefaultUserDtoRequest` function to
   * obtain a request object, which is then used to retrieve the user from the data
   * transfer object.
   *
   * @returns a `User` object retrieved from the database through the `getUserFromDto`
   * method.
   */
  private User getDefaultUser() {
    return getUserFromDto(getDefaultUserDtoRequest());
  }

  /**
   * Creates a `ForgotPasswordRequest` object, populates its properties with email, new
   * password, and password reset token, and returns the object.
   *
   * @returns a `ForgotPasswordRequest` object with email, new password, and token
   * properties populated.
   */
  private ForgotPasswordRequest getForgotPasswordRequest() {
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail(USER_EMAIL);
    request.setNewPassword(NEW_USER_PASSWORD);
    request.setToken(PASSWORD_RESET_TOKEN);
    return request;
  }

  /**
   * Creates a SecurityToken object with a type of RESET, a password reset token, and
   * an expiration date. The token is set to be expired as it is created, with a validity
   * period equal to TOKEN_LIFETIME.
   *
   * @returns a SecurityToken object with an expired status and a reset token type.
   */
  private SecurityToken getExpiredTestToken() {
    return new SecurityToken(SecurityTokenType.RESET, PASSWORD_RESET_TOKEN, LocalDate.now(),
        LocalDate.now().minusDays(TOKEN_LIFETIME.toDays()), false, null);
  }

  /**
   * Creates a new `SecurityToken` instance, specifies its type, token, and expiration
   * date based on the provided lifetime, and associates it with a given `User`. The
   * token is valid from the current date.
   *
   * @param tokenType type of the security token being created.
   *
   * @param lifetime duration for which the security token is valid.
   *
   * @param token security token value.
   *
   * @param user owner of the security token, and is used to associate the token with
   * a specific user.
   *
   * @returns a `SecurityToken` object with a specified token type, token, and expiration
   * date.
   */
  private SecurityToken getSecurityToken(SecurityTokenType tokenType, Duration lifetime,
      String token, User user) {
    LocalDate expireDate = LocalDate.now().plusDays(lifetime.toDays());
    return new SecurityToken(tokenType, token, LocalDate.now(), expireDate, false, user);
  }

  /**
   * Generates a new `SecurityToken` object based on the provided `tokenType`, `token`,
   * and `user`. The token's expiration date is set to one day after the current date.
   *
   * @param tokenType type of security token being created.
   *
   * @param token security token being validated or created.
   *
   * @param user entity associated with the generated `SecurityToken`.
   *
   * @returns a `SecurityToken` object with specified properties, including token type,
   * token, expiration date, and user.
   */
  private SecurityToken getSecurityToken(SecurityTokenType tokenType, String token, User user) {
    LocalDate expireDate = LocalDate.now().plusDays(Duration.ofDays(1).toDays());
    return new SecurityToken(tokenType, token, LocalDate.now(), expireDate, false, user);
  }
}