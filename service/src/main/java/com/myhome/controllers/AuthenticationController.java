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
 * Handles authentication requests by implementing the AuthenticationApi interface.
 * It provides a login endpoint to authenticate users and returns a successful response
 * with user ID and JWT token headers.
 */
@RequiredArgsConstructor
@RestController
public class AuthenticationController implements AuthenticationApi {

  private final AuthenticationService authenticationService;

  /**
   * Handles user authentication by calling the `login` method of the `authenticationService`
   * with the provided `loginRequest`.
   * It then returns a successful HTTP response with authentication headers based on
   * the authentication data.
   *
   * @param loginRequest request data for the login operation, which is validated and
   * then passed to the `authenticationService` for authentication.
   *
   * @returns a ResponseEntity with HTTP status 200 OK and custom headers.
   */
  @Override
  public ResponseEntity<Void> login(@Valid LoginRequest loginRequest) {
    final AuthenticationData authenticationData = authenticationService.login(loginRequest);
    return ResponseEntity.ok()
        .headers(createLoginHeaders(authenticationData))
        .build();
  }

  /**
   * Constructs a set of HTTP headers containing user ID and JWT token, which can be
   * used for authentication purposes. It takes an `AuthenticationData` object as input
   * and returns a `HttpHeaders` object. The function uses the user ID and JWT token
   * from the input object to populate the headers.
   *
   * @param authenticationData authentication data used to populate the HTTP headers
   * with a user ID and JWT token.
   *
   * @returns a HttpHeaders object containing a userId and a token.
   */
  private HttpHeaders createLoginHeaders(AuthenticationData authenticationData) {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("userId", authenticationData.getUserId());
    httpHeaders.add("token", authenticationData.getJwtToken());
    return httpHeaders;
  }
}
