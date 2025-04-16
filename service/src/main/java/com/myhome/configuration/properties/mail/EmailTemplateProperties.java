package com.myhome.configuration.properties.mail;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Represents configuration properties for email templates.
 *
 * - path (String): stores a string representing a path.
 *
 * - format (String): Stores the format of email templates.
 *
 * - encoding (String): represents the character encoding used for the email template.
 *
 * - mode (String): represents a string value.
 *
 * - cache (boolean): is a boolean flag indicating whether caching is enabled.
 */
@Data
@Component
@ConfigurationProperties(prefix = "email.template")
public class EmailTemplateProperties {
  private String path;
  private String format;
  private String encoding;
  private String mode;
  private boolean cache;
}
