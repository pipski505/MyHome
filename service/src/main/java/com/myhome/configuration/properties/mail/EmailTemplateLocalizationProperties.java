package com.myhome.configuration.properties.mail;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Represents configuration properties for email template localization.
 *
 * - path (String): stores a string representing a path.
 *
 * - encoding (String): specifies the encoding of email templates.
 *
 * - cacheSeconds (int): represents a cache expiration time in seconds.
 */
@Data
@Component
@ConfigurationProperties(prefix = "email.location")
public class EmailTemplateLocalizationProperties {
  private String path;
  private String encoding;
  private int cacheSeconds;
}
