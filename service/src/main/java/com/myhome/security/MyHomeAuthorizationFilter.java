/*
 * Copyright 2020 Prathab Murugan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myhome.security;

import com.myhome.security.jwt.AppJwt;
import com.myhome.security.jwt.AppJwtEncoderDecoder;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Extends the BasicAuthenticationFilter class to enable authentication filtering for
 * incoming HTTP requests, primarily using JSON Web Tokens (JWT).
 */
public class MyHomeAuthorizationFilter extends BasicAuthenticationFilter {

  private final Environment environment;
  private final AppJwtEncoderDecoder appJwtEncoderDecoder;

  public MyHomeAuthorizationFilter(
      AuthenticationManager authenticationManager,
      Environment environment,
      AppJwtEncoderDecoder appJwtEncoderDecoder) {
    super(authenticationManager);
    this.environment = environment;
    this.appJwtEncoderDecoder = appJwtEncoderDecoder;
  }

  /**
   * Verifies the presence and validity of an authorization token in the HTTP request
   * headers. If the token is valid, it sets the authentication context and proceeds
   * with the filter chain; otherwise, it skips authentication and continues with the
   * chain.
   *
   * @param request HttpServletRequest object that contains the HTTP request data,
   * including headers and parameters.
   *
   * Exposes the HTTP headers, such as `authHeaderName`, and provides access to the
   * HTTP request parameters and attributes.
   *
   * @param response HTTP response sent to the client.
   *
   * Include.
   * It is an instance of `HttpServletResponse` with the following properties:
   * - `ServletResponse` interface methods
   * - `ServletResponseWrapper` methods
   * - `httpServletResponse` object with properties such as `status`, `outputStream`,
   * `writer`
   *
   * @param chain sequence of filters that will be executed after the current filter.
   *
   * Passed as a parameter, `chain` is an instance of `FilterChain` interface, which
   * extends `Chain` interface.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    String authHeaderName = environment.getProperty("authorization.token.header.name");
    String authHeaderPrefix = environment.getProperty("authorization.token.header.prefix");

    String authHeader = request.getHeader(authHeaderName);
    if (authHeader == null || !authHeader.startsWith(authHeaderPrefix)) {
      chain.doFilter(request, response);
      return;
    }

    UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    chain.doFilter(request, response);
  }

  /**
   * Validates an authentication token in the HTTP request header, decodes the token
   * using a secret key, and returns a `UsernamePasswordAuthenticationToken` object if
   * the token is valid and contains a user ID.
   *
   * @param request HTTP request object from which the authentication token is extracted.
   *
   * Obtain the value of the HTTP request header with the specified name.
   * Retrieve the value of the HTTP request header with the specified name if it exists,
   * otherwise return null.
   * Extract the token value from the obtained header value by removing the specified
   * prefix.
   * Decode the token using the specified secret key.
   *
   * @returns a `UsernamePasswordAuthenticationToken` containing a user ID or null if
   * authentication fails.
   *
   * The output is a `UsernamePasswordAuthenticationToken` object. It has a `principal`
   * property, which is the user ID in this case, and an `authenticated` property, which
   * is `false` by default.
   */
  private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    String authHeader =
        request.getHeader(environment.getProperty("authorization.token.header.name"));
    if (authHeader == null) {
      return null;
    }

    String token =
        authHeader.replace(environment.getProperty("authorization.token.header.prefix"), "");
    AppJwt jwt = appJwtEncoderDecoder.decode(token, environment.getProperty("token.secret"));

    if (jwt.getUserId() == null) {
      return null;
    }
    return new UsernamePasswordAuthenticationToken(jwt.getUserId(), null, Collections.emptyList());
  }
}
