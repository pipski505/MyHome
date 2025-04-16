package com.myhome.services;

import com.myhome.domain.AuthenticationData;
import com.myhome.model.LoginRequest;

/**
 * Defines a contract for authenticating users by providing a login method that returns
 * authentication data.
 */
public interface AuthenticationService {
  AuthenticationData login(LoginRequest loginRequest);
}
