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

package com.myhome.security.jwt;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Is a test class containing a single test method to validate the creation of an
 * AppJwt object using its builder pattern. The test creates an instance of AppJwt
 * with specific parameters and prints it to the console. This test ensures that the
 * AppJwt object can be successfully created with valid user information and expiration
 * date.
 */
class AppJwtParamTest {

  /**
   * Creates an instance of the `AppJwt` class using a builder pattern, specifying a
   * user ID and expiration date set to the current time. The resulting object is then
   * printed to the console.
   */
  @Test
  void testParamCreationBuilder() {
    AppJwt param = AppJwt.builder().userId("test-user-id").expiration(LocalDateTime.now()).build();
    System.out.println(param);
  }
}