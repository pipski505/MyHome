package com.myhome.services.springdatajpa;

import com.myhome.domain.SecurityToken;
import com.myhome.domain.User;
import com.myhome.services.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

/**
 * This class implements the MailService interface and provides email functionality
 * for development mode. It handles password recovery, account confirmation, password
 * change, and account creation notifications.
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "spring.mail.dev-mode", havingValue = "true", matchIfMissing = true)
public class DevMailSDJpaService implements MailService {

  /**
   * Logs a message indicating a password recover code has been sent to a user and
   * returns true to indicate successful operation.
   *
   * @param user user whose password recovery code is being sent.
   *
   * @param randomCode password recovery code sent to the user.
   *
   * @returns a boolean value indicating success, set to `true`.
   */
  @Override
  public boolean sendPasswordRecoverCode(User user, String randomCode) throws MailSendException {
    log.info(String.format("Password recover code sent to user with id= %s, code=%s", user.getUserId()), randomCode);
    return true;
  }

  /**
   * Logs a confirmation message when an account is confirmed, then returns true to
   * indicate success.
   *
   * @param user user account for which an account confirmation message is being sent.
   *
   * @returns a boolean value indicating success, always returning true.
   */
  @Override
  public boolean sendAccountConfirmed(User user) {
    log.info(String.format("Account confirmed message sent to user with id=%s", user.getUserId()));
    return true;
  }

  /**
   * Logs a message indicating that a password change notification has been sent to a
   * user and returns a boolean value indicating success.
   *
   * @param user user whose password has been successfully changed, and is used to log
   * a message with the user's ID.
   *
   * @returns a boolean value of true indicating successful execution.
   */
  @Override
  public boolean sendPasswordSuccessfullyChanged(User user) {
    log.info(String.format("Password successfully changed message sent to user with id=%s", user.getUserId()));
    return true;
  }


  /**
   * Sends a notification when a user account is created, logging the event and returning
   * true.
   *
   * @param user user object for whom an account creation message is being sent.
   *
   * @param emailConfirmToken security token used for email confirmation, but it is not
   * used within the function.
   *
   * @returns a boolean true value indicating the message was sent successfully.
   */
  @Override
  public boolean sendAccountCreated(User user, SecurityToken emailConfirmToken) {
    log.info(String.format("Account created message sent to user with id=%s", user.getUserId()));
    return true;
  }


}
