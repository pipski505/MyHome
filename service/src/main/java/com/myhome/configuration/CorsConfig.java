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
 * Configures Cross-Origin Resource Sharing (CORS) for a Spring application.
 * It allows specified origins to make requests to the application.
 * It exposes specific headers and enables credentials.
 */
@Configuration
public class CorsConfig {

  @Value("${server.cors.allowedOrigins}")
  private String[] allowedOrigins;

  /**
   * Configures CORS for the application, allowing all origins to make requests to any
   * endpoint with any method and headers, while exposing specific headers and allowing
   * credentials.
   *
   * @returns a configuration for enabling CORS (Cross-Origin Resource Sharing) across
   * all API endpoints.
   *
   * Allow all origins, methods, and headers.
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      /**
       * Configures CORS (Cross-Origin Resource Sharing) for a web application. It adds a
       * mapping for all resources ("/**") and allows requests from any origin, with all
       * methods, headers, and credentials. Exposed headers include "token" and "userId".
       *
       * @param registry configuration for CORS (Cross-Origin Resource Sharing) mappings,
       * allowing customization of allowed origins, methods, headers, and credentials for
       * cross-origin requests.
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
