package com.myhome.configuration.properties.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Is a configuration class that enables binding of Spring Boot application properties
 * related to mail configuration.
 *
 * - host (String): stores the host name of a mail server.
 *
 * - username (String): represents the username for mail authentication.
 *
 * - password (String): stores a mail account password.
 *
 * - port (int): represents the port number for mail communication.
 *
 * - protocol (String): stores the mail protocol.
 *
 * - debug (boolean): is a boolean flag indicating whether debug mode is enabled.
 *
 * - devMode (boolean): is a boolean flag indicating development mode.
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

