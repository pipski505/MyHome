package com.myhome.controllers.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

/**
 * Represents a request object for handling password reset operations, encapsulating
 * user email, token, and new password details.
 *
 * - email (String): is an email address.
 *
 * - token (String): is a string.
 *
 * - newPassword (String): is the new password to be set.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ForgotPasswordRequest {
  @Email
  public String email;
  public String token;
  public String newPassword;
}
