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

package com.myhome.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Enables cross-origin resource sharing (CORS) in Spring MVC applications.
 * It exposes specific HTTP headers and allows credentials to be sent with requests
 * from specified origins.
 * The configuration is applied to all mappings within the application.
 */
@Configuration
public class CorsConfig {

  @Value("${server.cors.allowedOrigins}")
  private String[] allowedOrigins;

  /**
   * Adds CORS (Cross-Origin Resource Sharing) configuration to enable cross-origin
   * requests. It allows any origin, method, and header for all mappings, while exposing
   * specific headers ("token" and "userId") and enabling credentials. This enables
   * browser-based requests to a different domain.
   *
   * @returns a custom CORS configuration.
   * It allows all origins to make requests to any path, method and header.
   * It exposes specific headers.
   *
   * The output is an instance of WebMvcConfigurer.
   * It has a single method, addCorsMappings, which configures CORS mappings for the
   * application. The method adds a mapping for all URLs (/**), allowing cross-origin
   * requests from specified origins and with specified headers, methods, and credentials.
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      /**
       * Configures CORS (Cross-Origin Resource Sharing) settings for a Spring application.
       * It adds a mapping for all URLs, allowing origins specified by `allowedOrigins`,
       * and exposes specific headers with credentials enabled. The '*' wildcard allows all
       * HTTP methods and headers.
       *
       * @param registry instance through which CORS mappings are defined and configured
       * for the application.
       */
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("*")
            .allowedHeaders("*")
            .exposedHeaders("token", "userId")
            .allowCredentials(true);
      }
    };
  }
}
