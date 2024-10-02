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
 * Configures Cross-Origin Resource Sharing (CORS) in a Spring application.
 */
@Configuration
public class CorsConfig {

  @Value("${server.cors.allowedOrigins}")
  private String[] allowedOrigins;

  /**
   * Configures CORS (Cross-Origin Resource Sharing) settings for the application,
   * allowing all origins to access all methods and headers, with credentials enabled
   * and exposing specific headers.
   *
   * @returns a WebMvcConfigurer object that enables CORS for all origins and methods
   * with specified exposed headers.
   *
   * Allow all origins, methods, and headers. Exposed headers include 'token' and
   * 'userId'. Credentials are allowed.
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      /**
       * Configures CORS (Cross-Origin Resource Sharing) for the application. It enables
       * cross-origin requests from any origin, allowing any HTTP method and all headers,
       * and exposes specific headers ("token" and "userId") while allowing credentials to
       * be sent.
       *
       * @param registry registry where CORS mappings are stored and managed.
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
