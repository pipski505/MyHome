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

package com.myhome.security.jwt.impl;

import com.myhome.security.jwt.AppJwt;
import com.myhome.security.jwt.AppJwtEncoderDecoder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Concrete implementation of {@link AppJwtEncoderDecoder}.
 */
@Component
@Profile("default")
public class SecretJwtEncoderDecoder implements AppJwtEncoderDecoder {

  /**
   * Verifies a JWT token using the provided secret key, extracts the user ID and
   * expiration date from the token's claims, and returns an `AppJwt` object containing
   * this information.
   *
   * @param encodedJwt JWT token to be decoded and its contents are used to extract the
   * user ID and expiration date.
   *
   * @param secret secret key used for HMAC SHA-256 signature verification of the
   * provided JWT.
   *
   * @returns an `AppJwt` object containing a user ID and a local date and time of expiration.
   *
   * The output returned by the `decode` function is an instance of `AppJwt`. It has
   * two main attributes: `userId` and `expiration`.
   */
  @Override public AppJwt decode(String encodedJwt, String secret) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
        .build()
        .parseClaimsJws(encodedJwt)
        .getBody();
    String userId = claims.getSubject();
    Date expiration = claims.getExpiration();
    return AppJwt.builder()
        .userId(userId)
        .expiration(expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
        .build();
  }

  /**
   * Generates a JSON Web Token (JWT) based on the provided `AppJwt` object and secret
   * key. It sets the subject to the user ID and expiration date, then signs the token
   * using the HMAC SHA-512 algorithm.
   *
   * @param jwt AppJwt object, which contains the user ID and expiration time used to
   * construct the encoded JWT.
   *
   * @param secret secret key used for HMAC SHA-512 signature generation.
   *
   * @returns a JSON Web Token (JWT) in compact form, signed with HS512 algorithm.
   */
  @Override public String encode(AppJwt jwt, String secret) {
    Date expiration = Date.from(jwt.getExpiration().atZone(ZoneId.systemDefault()).toInstant());
    return Jwts.builder()
        .setSubject(jwt.getUserId())
        .setExpiration(expiration)
        .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512).compact();
  }
}
