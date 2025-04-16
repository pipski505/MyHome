package com.myhome.configuration.properties.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Represents a Spring Boot configuration for mail properties.
 *
 * - host (String): represents the mail server host.
 *
 * - username (String): stores the username for mail operations.
 *
 * - password (String): stores a mail server password.
 *
 * - port (int): represents the mail port number.
 *
 * - protocol (String): represents the communication protocol used for mail transfer.
 *
 * - debug (boolean): is a boolean flag.
 *
 * - devMode (boolean): is a boolean flag indicating a development mode.
 */
@Data
@ConfigurationProperties(prefix = "spring.mail")
public class MailProperties {
  private String host;
  private String username;
  private String password;
  private int port;
  private String protocol;
  private boolean debug;
  private boolean devMode;
}

