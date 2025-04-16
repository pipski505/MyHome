package com.myhome.services;

import com.myhome.domain.SecurityToken;
import com.myhome.domain.User;

/**
 * Defines a contract for managing security tokens, specifically for email confirmation,
 * password reset, and token usage.
 */
public interface SecurityTokenService {

  SecurityToken createEmailConfirmToken(User owner);

  SecurityToken createPasswordResetToken(User owner);

  SecurityToken useToken(SecurityToken token);
}
