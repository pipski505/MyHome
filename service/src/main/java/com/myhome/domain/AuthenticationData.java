package com.myhome.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a data container for authentication information, specifically storing
 * a JWT token and a user ID.
 *
 * - jwtToken (String): holds a string representing a JSON Web Token.
 *
 * - userId (String): stores a user identifier.
 */
@Getter
@RequiredArgsConstructor
public class AuthenticationData {
  private final String jwtToken;
  private final String userId;
}
