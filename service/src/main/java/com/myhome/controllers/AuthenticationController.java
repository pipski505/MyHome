package com.myhome.controllers;

import com.myhome.api.AuthenticationApi;
import com.myhome.domain.AuthenticationData;
import com.myhome.model.LoginRequest;
import com.myhome.services.AuthenticationService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles authentication requests by validating login credentials and returning a
 * response with authentication tokens. It relies on the AuthenticationService to
 * perform the actual authentication logic. The controller implements the AuthenticationApi
 * interface.
 */
@RequiredArgsConstructor
@RestController
public class AuthenticationController implements AuthenticationApi {

  private final AuthenticationService authenticationService;

  /**
   * Handles a login request by calling the `authenticationService` to authenticate the
   * user and returns a successful response with authentication headers.
   *
   * @param loginRequest login credentials submitted by the user.
   *
   * @returns a ResponseEntity containing an HTTP OK status code with custom headers.
   */
  @Override
  public ResponseEntity<Void> login(@Valid LoginRequest loginRequest) {
    final AuthenticationData authenticationData = authenticationService.login(loginRequest);
    return ResponseEntity.ok()
        .headers(createLoginHeaders(authenticationData))
        .build();
  }

  /**
   * Constructs HttpHeaders objects with userId and JWT token from AuthenticationData,
   * enabling secure login requests. The function creates a new HttpHeaders instance,
   * adds userId and JWT token, and returns the resulting HttpHeaders object. It is
   * likely used for API authentication.
   *
   * @param authenticationData authentication data containing the user ID and JWT token,
   * which are used to construct the HTTP headers.
   *
   * @returns a HttpHeaders object with userId and token headers.
   */
  private HttpHeaders createLoginHeaders(AuthenticationData authenticationData) {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("userId", authenticationData.getUserId());
    httpHeaders.add("token", authenticationData.getJwtToken());
    return httpHeaders;
  }
}
