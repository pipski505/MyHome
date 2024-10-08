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
 * Configures cross-origin resource sharing for a Spring application.
 */
@Configuration
public class CorsConfig {

  @Value("${server.cors.allowedOrigins}")
  private String[] allowedOrigins;

  /**
   * Configures cross-origin resource sharing for all web requests by allowing any
   * origin, method, and header, and exposing specific headers, enabling credentials
   * to be sent.
   *
   * @returns a WebMvcConfigurer object that enables CORS for all origins and methods.
   *
   * Enable CORS for all URLs with wildcard mapping "/**".
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      /**
       * Configures CORS (Cross-Origin Resource Sharing) settings for a web application.
       * It adds a mapping for all URLs, allowing origins specified by `allowedOrigins`,
       * and exposes specific headers.
       *
       * @param registry configuration object for handling CORS (Cross-Origin Resource
       * Sharing) mappings.
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
