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

package com.myhome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initializes and configures a Spring Boot application with security features. It
 * defines a main method to start the application and registers a password encoder
 * bean using BCryptPasswordEncoder. The class also enables configuration properties
 * scanning.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class MyHomeServiceApplication {

  /**
   * Launches an application using the Spring Boot framework. It runs a class named
   * `MyHomeServiceApplication`, passing command-line arguments to it. This initializes
   * and starts the application.
   *
   * @param args command-line arguments passed to the application when it is run.
   */
  public static void main(String[] args) {
    SpringApplication.run(MyHomeServiceApplication.class, args);
  }

  /**
   * Defines a bean to configure password encoding using the BCrypt algorithm. It returns
   * an instance of `BCryptPasswordEncoder`, which can be used to hash and verify
   * passwords securely. This implementation provides a strong password hashing mechanism
   * for authentication purposes.
   *
   * @returns a new instance of `BCryptPasswordEncoder`.
   */
  @Bean
  public PasswordEncoder getPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
