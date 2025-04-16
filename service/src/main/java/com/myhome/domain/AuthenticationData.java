package com.myhome.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents authentication data containing a JWT token and a user ID.
 *
 * - jwtToken (String): stores a JWT token.
 *
 * - userId (String): represents a unique identifier for a user.
 */
@Getter
@RequiredArgsConstructor
public class AuthenticationData {
  private final String jwtToken;
  private final String userId;
}
