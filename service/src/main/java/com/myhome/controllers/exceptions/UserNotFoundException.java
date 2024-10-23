package com.myhome.controllers.exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents a custom exception for handling user not found scenarios in authentication,
 * providing a log entry for the email that was searched for.
 */
@Slf4j
public class UserNotFoundException extends AuthenticationException {
  public UserNotFoundException(String userEmail) {
    super();
    log.info("User not found - email: " + userEmail);
  }
}
