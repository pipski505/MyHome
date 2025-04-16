package com.myhome.controllers.exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * Extends the AuthenticationException Class to represent a custom exception for
 * incorrect credentials.
 */
@Slf4j
public class CredentialsIncorrectException extends AuthenticationException {
  public CredentialsIncorrectException(String userId) {
    super();
    log.info("Credentials are incorrect for userId: " + userId);
  }
}
