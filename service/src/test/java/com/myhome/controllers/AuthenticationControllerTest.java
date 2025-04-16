package com.myhome.controllers;

import com.myhome.domain.AuthenticationData;
import com.myhome.model.LoginRequest;
import com.myhome.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Is a unit test class designed to verify the functionality of the AuthenticationController
 * class. It uses Mockito for mocking dependencies and JUnit for testing.
 */
public class AuthenticationControllerTest {

  private static final String TEST_ID = "1";
  private static final String TEST_EMAIL = "email@mail.com";
  private static final String TEST_PASSWORD = "password";
  private static final String TOKEN = "token";

  @Mock
  private AuthenticationService authenticationService;
  @InjectMocks
  private AuthenticationController authenticationController;

  /**
   * Initializes Mockito annotations for the test class, enabling mock objects to be
   * created automatically. This function is annotated with `@BeforeEach`, indicating
   * it runs before each test method in the class.
   */
  @BeforeEach
  private void init() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Tests the login functionality of an authentication controller. It sends a login
   * request, verifies the response status code and headers, and checks that the
   * authentication service is called correctly.
   */
  @Test
  void loginSuccess() {
    // given
    LoginRequest loginRequest = getDefaultLoginRequest();
    AuthenticationData authenticationData = getDefaultAuthenticationData();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("userId", authenticationData.getUserId());
    httpHeaders.add("token", authenticationData.getJwtToken());
    given(authenticationService.login(loginRequest))
        .willReturn(authenticationData);

    // when
    ResponseEntity<Void> response = authenticationController.login(loginRequest);

    // then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(response.getHeaders().size(), 2);
    assertEquals(response.getHeaders(), httpHeaders);
    verify(authenticationService).login(loginRequest);
  }

  /**
   * Returns a pre-configured `LoginRequest` object with the email address `TEST_EMAIL`
   * and password `TEST_PASSWORD`. The function creates a new instance of `LoginRequest`
   * and sets its email and password properties using the `email` and `password` methods.
   *
   * @returns a `LoginRequest` object with email `TEST_EMAIL` and password `TEST_PASSWORD`.
   */
  private LoginRequest getDefaultLoginRequest() {
    return new LoginRequest().email(TEST_EMAIL).password(TEST_PASSWORD);
  }

  /**
   * Returns an instance of `AuthenticationData` with a token named `TOKEN` and a test
   * ID named `TEST_ID`. This instance represents default authentication data. The
   * function creates a new instance every time it is called.
   *
   * @returns an instance of `AuthenticationData` with `TOKEN` as its type and `TEST_ID`
   * as its value.
   */
  private AuthenticationData getDefaultAuthenticationData() {
    return new AuthenticationData(TOKEN, TEST_ID);
  }
}
