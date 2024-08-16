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
 * Is an entry point for Spring Boot application. It enables auto-configuration and
 * property scanning and provides a bean definition for password encoding using BCrypt
 * algorithm. The class serves as the primary configuration class for the Spring Boot
 * application.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class MyHomeServiceApplication {

  /**
   * Initializes and runs a Spring-based application instance by calling the `run`
   * method of `SpringApplication`. This method starts the application's embedded web
   * server and initializes its dependencies. The `MyHomeServiceApplication` class is
   * passed as an argument to specify the main application configuration.
   *
   * @param args array of command-line arguments passed to the Spring Boot application
   * when it is launched.
   */
  public static void main(String[] args) {
    SpringApplication.run(MyHomeServiceApplication.class, args);
  }

  /**
   * Returns an instance of a `BCryptPasswordEncoder`, which is a password encoder used
   * to store and verify passwords securely. It uses the Blowfish encryption algorithm
   * with a work factor, providing strong password hashing and salting capabilities.
   *
   * @returns an instance of a BCrypt password encoder.
   */
  @Bean
  public PasswordEncoder getPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
