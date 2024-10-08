package com.myhome.configuration.properties.mail;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Is a configuration class that holds email template properties.
 *
 * - path (String): stores a string representing a path.
 *
 * - format (String): represents a string value specifying the format.
 *
 * - encoding (String): stores the encoding type.
 *
 * - mode (String): specifies a mode.
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
