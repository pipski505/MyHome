package com.myhome.controllers.exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * Extends AuthenticationException to represent a custom exception for user not found
 * scenarios.
 */
@Slf4j
public class UserNotFoundException extends AuthenticationException {
  public UserNotFoundException(String userEmail) {
    super();
    log.info("User not found - email: " + userEmail);
  }
}
