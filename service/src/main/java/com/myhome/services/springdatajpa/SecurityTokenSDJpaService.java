package com.myhome.services.springdatajpa;

import com.myhome.domain.SecurityTokenType;
import com.myhome.domain.SecurityToken;
import com.myhome.domain.User;
import com.myhome.repositories.SecurityTokenRepository;
import com.myhome.services.SecurityTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Provides functionality for managing security tokens, including creation and usage.
 * It utilizes Spring Data JPA for database operations and relies on external
 * configuration for token expiration times.
 * The class implements the SecurityTokenService interface.
 */
@Service
@RequiredArgsConstructor
public class SecurityTokenSDJpaService implements SecurityTokenService {

  private final SecurityTokenRepository securityTokenRepository;

  @Value("${tokens.reset.expiration}")
  private Duration passResetTokenTime;
  @Value("${tokens.email.expiration}")
  private Duration emailConfirmTokenTime;

  /**
   * Generates a unique security token, sets its expiration date based on a specified
   * live time, assigns an owner, and saves the token to a repository.
   *
   * @param tokenType type of security token being created, which is used to initialize
   * a `SecurityToken` object.
   *
   * @param liveTimeSeconds duration in seconds after which the created security token
   * expires.
   *
   * @param tokenOwner owner of the newly created security token and is set as the owner
   * using the `setTokenOwner` method.
   *
   * @returns a saved `SecurityToken` object with a unique token ID and assigned token
   * owner.
   */
  private SecurityToken createSecurityToken(SecurityTokenType tokenType, Duration liveTimeSeconds, User tokenOwner) {
    String token = UUID.randomUUID().toString();
    LocalDate creationDate = LocalDate.now();
    LocalDate expiryDate = getDateAfterDays(LocalDate.now(), liveTimeSeconds);
    SecurityToken newSecurityToken = new SecurityToken(tokenType, token, creationDate, expiryDate, false, null);
    newSecurityToken.setTokenOwner(tokenOwner);
    newSecurityToken = securityTokenRepository.save(newSecurityToken);
    return newSecurityToken;
  }

  /**
   * Generates a security token for email confirmation.
   * It calls another function, `createSecurityToken`, to create the token.
   * The token is associated with the given `tokenOwner` user and has a specific type
   * (`EMAIL_CONFIRM`) and expiration time (`emailConfirmTokenTime`).
   *
   * @param tokenOwner user associated with the email confirmation security token being
   * generated.
   *
   * @returns a `SecurityToken` object of type `EMAIL_CONFIRM`.
   */
  @Override
  public SecurityToken createEmailConfirmToken(User tokenOwner) {
    return createSecurityToken(SecurityTokenType.EMAIL_CONFIRM, emailConfirmTokenTime, tokenOwner);
  }

  /**
   * Generates a security token for password reset.
   * It calls another function `createSecurityToken` to create the token.
   *
   * @param tokenOwner user for whom the password reset token is being created.
   *
   * @returns a SecurityToken object.
   */
  @Override
  public SecurityToken createPasswordResetToken(User tokenOwner) {
    return createSecurityToken(SecurityTokenType.RESET, passResetTokenTime, tokenOwner);
  }

  /**
   * Marks a SecurityToken as used, saves the updated token to the repository, and
   * returns the saved token.
   *
   * @param token SecurityToken to be marked as used and updated in the repository.
   *
   * @returns a saved SecurityToken object with its used status set to true.
   */
  @Override
  public SecurityToken useToken(SecurityToken token) {
    token.setUsed(true);
    token = securityTokenRepository.save(token);
    return token;
  }

  /**
   * Calculates a date by adding a specified number of days to a given date, where the
   * number of days is derived from a duration. The duration is converted to days before
   * being added to the date. The resulting date is returned.
   *
   * @param date starting date from which a specified duration is added.
   *
   * @param liveTime duration of time for which the date is to be incremented.
   *
   * @returns a `LocalDate` representing the input date plus the specified duration in
   * days.
   */
  private LocalDate getDateAfterDays(LocalDate date, Duration liveTime) {
    return date.plusDays(liveTime.toDays());
  }
}
