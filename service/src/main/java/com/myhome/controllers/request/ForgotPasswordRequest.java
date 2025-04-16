package com.myhome.controllers.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

/**
 * Represents a data model for a password recovery request.
 *
 * - email (String): is annotated with @Email for validation.
 *
 * - token (String): is a string field.
 *
 * - newPassword (String): is a string to store a new password.
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
