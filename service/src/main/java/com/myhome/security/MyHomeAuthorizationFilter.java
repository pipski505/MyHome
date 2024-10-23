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
 * Implements an authorization filter that verifies authentication tokens in HTTP requests.
 * It uses environment properties to determine the token header name and prefix, then
 * extracts and decodes the token using an encoder-decoder service.
 * If the token is valid, it sets the corresponding authentication object in the
 * security context.
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
   * Authenticates incoming requests by checking a custom authorization header.
   * It skips authentication if the header is missing or does not match the expected prefix.
   * If authentication is successful, it sets the security context and allows the request
   * to proceed.
   *
   * @param request HttpServletRequest object that is being processed by the filter.
   *
   * Pass a `HttpServletRequest` object with several key properties including
   * - getHeader(String name) to retrieve HTTP headers by name
   * - getProperty(String name) is not applicable for HttpServletRequest; used here
   * from an external environment.
   *
   * @param response HttpServletResponse object that contains the HTTP response sent
   * to the client and is passed through the filter chain but not modified within the
   * function.
   *
   * Send response headers and send response body if appropriate to output stream.
   *
   * @param chain sequence of filters that are to be executed after authentication has
   * been successfully completed or when it is not required.
   *
   * * chain: an object implementing FilterChain interface, containing a list of filters
   * to be executed.
   *     Properties:
   *       - FilterChain filterChain;
   *       - List<Filter> filters.
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
   * Authenticates a user by retrieving an authorization token from the HTTP request
   * header, decodes and verifies it, and returns a UsernamePasswordAuthenticationToken
   * if valid with the decoded user ID as its principal. It returns null otherwise.
   * Authentication involves token validation against a secret key.
   *
   * @param request HTTP request object from which an authentication token is extracted
   * to perform authentication and verification.
   *
   * Get its `header` property to obtain the HTTP request headers as a `Map<String, String>`.
   *
   * This map contains various HTTP header names and values.
   *
   * @returns a `UsernamePasswordAuthenticationToken` object or null.
   *
   * The `UsernamePasswordAuthenticationToken` object has two main attributes. It is
   * constructed with `jwt.getUserId()` as its principal and an empty collection as its
   * authorities. The token's password attribute is set to null.
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
