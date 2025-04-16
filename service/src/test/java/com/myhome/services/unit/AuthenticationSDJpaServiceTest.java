package com.myhome.services.unit;

import com.myhome.controllers.dto.UserDto;
import com.myhome.controllers.exceptions.CredentialsIncorrectException;
import com.myhome.controllers.exceptions.UserNotFoundException;
import com.myhome.domain.AuthenticationData;
import com.myhome.model.LoginRequest;
import com.myhome.security.jwt.AppJwt;
import com.myhome.security.jwt.AppJwtEncoderDecoder;
import com.myhome.services.springdatajpa.AuthenticationSDJpaService;
import com.myhome.services.springdatajpa.UserSDJpaService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Provides unit tests for the AuthenticationSDJpaService class, covering successful
 * login, user not found, and incorrect credentials scenarios.
 */
public class AuthenticationSDJpaServiceTest {

  private final String USER_ID = "test-user-id";
  private final String USERNAME = "test-user-name";
  private final String USER_EMAIL = "test-user-email";
  private final String USER_PASSWORD = "test-user-password";
  private final String REQUEST_PASSWORD = "test-request-password";
  private final Duration TOKEN_LIFETIME = Duration.ofDays(1);
  private final String SECRET = "secret";

  @Mock
  private final UserSDJpaService userSDJpaService = mock(UserSDJpaService.class);
  @Mock
  private final AppJwtEncoderDecoder appJwtEncoderDecoder = mock(AppJwtEncoderDecoder.class);
  @Mock
  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private final AuthenticationSDJpaService authenticationSDJpaService =
      new AuthenticationSDJpaService(TOKEN_LIFETIME, SECRET, userSDJpaService, appJwtEncoderDecoder,
          passwordEncoder);

  /**
   * Verifies a successful login process. It simulates a valid user login by encoding
   * a JWT token and authenticating the user's credentials. The test asserts that the
   * login service returns the user's ID and the encoded JWT token.
   */
  @Test
  void loginSuccess() {
    // given
    LoginRequest request = getDefaultLoginRequest();
    UserDto userDto = getDefaultUserDtoRequest();
    AppJwt appJwt = getDefaultJwtToken(userDto);
    String encodedJwt = appJwtEncoderDecoder.encode(appJwt, SECRET);
    given(userSDJpaService.findUserByEmail(request.getEmail()))
        .willReturn(Optional.of(userDto));
    given(passwordEncoder.matches(request.getPassword(), userDto.getEncryptedPassword()))
        .willReturn(true);
    given(appJwtEncoderDecoder.encode(appJwt, SECRET))
        .willReturn(encodedJwt);

    // when
    AuthenticationData authenticationData = authenticationSDJpaService.login(request);

    // then
    assertNotNull(authenticationData);
    assertEquals(authenticationData.getUserId(), userDto.getUserId());
    assertEquals(authenticationData.getJwtToken(), encodedJwt);
    verify(userSDJpaService).findUserByEmail(request.getEmail());
    verify(passwordEncoder).matches(request.getPassword(), userDto.getEncryptedPassword());
    verify(appJwtEncoderDecoder).encode(appJwt, SECRET);
  }

  /**
   * Tests the login functionality when a user with the provided email is not found in
   * the database.
   * It checks if a `UserNotFoundException` is thrown when the `login` method is called.
   * The test simulates a scenario where the user does not exist.
   */
  @Test
  void loginUserNotFound() {
    // given
    LoginRequest request = getDefaultLoginRequest();
    given(userSDJpaService.findUserByEmail(request.getEmail()))
        .willReturn(Optional.empty());

    // when and then
    assertThrows(UserNotFoundException.class,
        () -> authenticationSDJpaService.login(request));
  }

  /**
   * Tests the authentication service's behavior when login credentials are incorrect.
   * It uses Mockito to mock the user service and password encoder, returning a user
   * with an incorrect password. The test asserts that a `CredentialsIncorrectException`
   * is thrown when attempting to login with the incorrect credentials.
   */
  @Test
  void loginCredentialsAreIncorrect() {
    // given
    LoginRequest request = getDefaultLoginRequest();
    UserDto userDto = getDefaultUserDtoRequest();
    given(userSDJpaService.findUserByEmail(request.getEmail()))
        .willReturn(Optional.of(userDto));
    given(passwordEncoder.matches(request.getPassword(), userDto.getEncryptedPassword()))
        .willReturn(false);

    // when and then
    assertThrows(CredentialsIncorrectException.class,
        () -> authenticationSDJpaService.login(request));
  }

  /**
   * Returns a pre-configured `LoginRequest` object with email set to `USER_EMAIL` and
   * password set to `REQUEST_PASSWORD`.
   * This function initializes a new `LoginRequest` object and sets its properties
   * before returning the object.
   *
   * @returns a `LoginRequest` object with email and password set to `USER_EMAIL` and
   * `REQUEST_PASSWORD` respectively.
   */
  private LoginRequest getDefaultLoginRequest() {
    return new LoginRequest().email(USER_EMAIL).password(REQUEST_PASSWORD);
  }

  /**
   * Returns a UserDto object with predefined attributes, including userId, name, email,
   * and encrypted password, and an empty set of communityIds. The values are likely
   * defined as constants (USER_ID, USERNAME, USER_EMAIL, USER_PASSWORD). The object
   * is created using a builder pattern.
   *
   * @returns a `UserDto` object with default user data, including a user ID, name,
   * email, and encrypted password.
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
   * Generates a default JWT token based on a provided `UserDto`. It sets the token's
   * expiration time to the current time plus a predefined `TOKEN_LIFETIME`. The token
   * is then built with the user's ID.
   *
   * @param userDto user data, specifically containing the user ID, which is used to
   * construct the JWT token.
   *
   * @returns an AppJwt object with a user ID and an expiration time.
   */
  private AppJwt getDefaultJwtToken(UserDto userDto) {
    final LocalDateTime expirationTime = LocalDateTime.now().plus(TOKEN_LIFETIME);
    return AppJwt.builder()
        .userId(userDto.getUserId())
        .expiration(expirationTime)
        .build();
  }
}
