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
 * Configures cross-origin resource sharing (CORS) for a web application. It defines
 * a bean that enables CORS support and specifies allowed origins, methods, headers,
 * and exposed headers. The class also allows credentials and applies the configuration
 * to all endpoints.
 */
@Configuration
public class CorsConfig {

  @Value("${server.cors.allowedOrigins}")
  private String[] allowedOrigins;

  /**
   * Enables CORS (Cross-Origin Resource Sharing) for a web application, allowing
   * requests from any origin, with all HTTP methods, and exposing specific headers
   * (`token` and `userId`) to handle cross-origin authentication.
   * 
   * @returns a configuration for CORS (Cross-Origin Resource Sharing) in Spring MVC.
   * 
   * Define a mapping with any path pattern by using the addMapping method. The
   * allowedOrigins property allows access from a specific set of origins. The
   * allowedMethods and allowedHeaders methods allow all HTTP methods and headers
   * respectively. ExposedHeaders is used to specify which headers can be accessed.
   * AllowCredentials is set to true for enabling credentials in the request.
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      /**
       * Defines CORS (Cross-Origin Resource Sharing) settings for a web application. It
       * enables cross-origin requests by mapping all URLs ("/**") and allowing specific
       * origins, methods, headers, and exposed headers. Additionally, it allows credentials
       * to be sent with the request.
       * 
       * @param registry instance responsible for managing CORS mappings, which are used
       * to define the configuration for Cross-Origin Resource Sharing.
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
