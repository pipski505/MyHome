package com.myhome.configuration.properties.mail;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Is a Spring Boot configuration class for email template localization properties.
 *
 * - path (String): stores a string path.
 *
 * - encoding (String): specifies the character encoding used for email templates.
 *
 * - cacheSeconds (int): stores an integer value.
 */
@Data
@Component
@ConfigurationProperties(prefix = "email.location")
public class EmailTemplateLocalizationProperties {
  private String path;
  private String encoding;
  private int cacheSeconds;
}
