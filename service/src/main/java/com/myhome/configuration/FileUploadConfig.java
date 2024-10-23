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

import javax.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

/**
 * Configures file upload settings for a Spring application.
 * It defines a maximum file size and request size.
 * Settings are based on a configuration property.
 */
@Configuration
public class FileUploadConfig {

  @Value("${files.maxSizeKBytes}")
  private int maxSizeKBytes;

  /**
   * Configures multipart request settings for a web application. It sets the maximum
   * file size and maximum request size for uploaded files. The maximum size is specified
   * in kilobytes.
   *
   * @returns a `MultipartConfigElement` object with specified max file and request sizes.
   */
  @Bean
  public MultipartConfigElement multipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();
    factory.setMaxFileSize(DataSize.ofKilobytes(maxSizeKBytes));
    factory.setMaxRequestSize(DataSize.ofKilobytes(maxSizeKBytes));
    return factory.createMultipartConfig();
  }
}
