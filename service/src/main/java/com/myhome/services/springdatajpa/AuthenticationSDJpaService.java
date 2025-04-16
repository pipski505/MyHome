package com.myhome.services.springdatajpa;

import com.myhome.controllers.dto.UserDto;
import com.myhome.controllers.exceptions.CredentialsIncorrectException;
import com.myhome.controllers.exceptions.UserNotFoundException;
import com.myhome.domain.AuthenticationData;
import com.myhome.model.LoginRequest;
import com.myhome.security.jwt.AppJwt;
import com.myhome.security.jwt.AppJwtEncoderDecoder;
import com.myhome.services.AuthenticationService;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user authentication by verifying credentials and issuing a JWT token upon
 * successful login.
 * It integrates with other services to retrieve user data and encode/decode tokens.
 * The class adheres to the AuthenticationService interface.
 */
@Slf4j
@Service
public class AuthenticationSDJpaService implements AuthenticationService {

  private final Duration tokenExpirationTime;
  private final String tokenSecret;

  private final UserSDJpaService userSDJpaService;
  private final AppJwtEncoderDecoder appJwtEncoderDecoder;
  private final PasswordEncoder passwordEncoder;

  public AuthenticationSDJpaService(@Value("${token.expiration_time}") Duration tokenExpirationTime,
      @Value("${token.secret}") String tokenSecret,
      UserSDJpaService userSDJpaService,
      AppJwtEncoderDecoder appJwtEncoderDecoder,
      PasswordEncoder passwordEncoder) {
    this.tokenExpirationTime = tokenExpirationTime;
    this.tokenSecret = tokenSecret;
    this.userSDJpaService = userSDJpaService;
    this.appJwtEncoderDecoder = appJwtEncoderDecoder;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Authenticates a user by email and password, retrieves a JWT token, and returns an
   * `AuthenticationData` object containing the encoded token and user ID. It throws
   * exceptions for invalid email and incorrect credentials.
   *
   * @param loginRequest login request data, containing the user's email and password.
   *
   * Contain an email and a password.
   *
   * @returns an `AuthenticationData` object containing a JWT token and a user ID.
   *
   * The output is an instance of `AuthenticationData`, which has two attributes:
   * `encodedToken` and `userId`.
   */
  @Override
  public AuthenticationData login(LoginRequest loginRequest) {
    log.trace("Received login request");
    final UserDto userDto = userSDJpaService.findUserByEmail(loginRequest.getEmail())
        .orElseThrow(() -> new UserNotFoundException(loginRequest.getEmail()));
    if (!isPasswordMatching(loginRequest.getPassword(), userDto.getEncryptedPassword())) {
      throw new CredentialsIncorrectException(userDto.getUserId());
    }
    final AppJwt jwtToken = createJwt(userDto);
    final String encodedToken = appJwtEncoderDecoder.encode(jwtToken, tokenSecret);
    return new AuthenticationData(encodedToken, userDto.getUserId());
  }

  /**
   * Compares a provided password with its hashed counterpart stored in the database.
   * It uses a password encoder to verify the password.
   * The function returns a boolean indicating whether the passwords match.
   *
   * @param requestPassword password provided by the user for verification.
   *
   * @param databasePassword hashed password stored in the database that is being
   * compared to the provided request password.
   *
   * @returns a boolean value indicating whether the request password matches the
   * database password.
   */
  private boolean isPasswordMatching(String requestPassword, String databasePassword) {
    return passwordEncoder.matches(requestPassword, databasePassword);
  }

  /**
   * Generates an AppJwt object based on a UserDto object, specifying the user's ID and
   * a fixed expiration time calculated from the current time. The expiration time is
   * determined by a predefined token expiration time.
   *
   * @param userDto data transfer object containing the user's information, specifically
   * the `userId`, which is used to construct the JWT.
   *
   * @returns an AppJwt object containing a user ID and expiration time.
   */
  private AppJwt createJwt(UserDto userDto) {
    final LocalDateTime expirationTime = LocalDateTime.now().plus(tokenExpirationTime);
    return AppJwt.builder()
        .userId(userDto.getUserId())
        .expiration(expirationTime)
        .build();
  }
}
