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
 * Is an annotation-based configuration class used to enable cross-origin resource
 * sharing (CORS) for a web application. It allows specific origins to access the
 * application's resources. The configuration defines the allowed origins, methods,
 * and headers for CORS requests.
 */
@Configuration
public class CorsConfig {

  @Value("${server.cors.allowedOrigins}")
  private String[] allowedOrigins;

  /**
   * Enables Cross-Origin Resource Sharing (CORS) for a Spring-based web application,
   * allowing resources to be accessed from any origin, with all HTTP methods and headers
   * supported, and exposing specific headers (`token`, `userId`).
   *
   * @returns a configuration for cross-origin resource sharing (CORS).
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      /**
       * Defines a mapping for CORS (Cross-Origin Resource Sharing) to allow cross-origin
       * requests from specified origins, permitting all HTTP methods and headers, exposing
       * specific response headers (`token` and `userId`), and allowing credentials.
       *
       * @param registry registry of CORS mappings, allowing the configuration of specific
       * URL patterns and settings for cross-origin resource sharing.
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
