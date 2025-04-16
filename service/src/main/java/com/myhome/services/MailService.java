package com.myhome.services;

import com.myhome.domain.SecurityToken;
import com.myhome.domain.User;

/**
 * Defines a set of methods for sending email notifications related to user account
 * management and password recovery.
 */
public interface MailService {

  boolean sendPasswordRecoverCode(User user, String randomCode);

  boolean sendAccountCreated(User user, SecurityToken emailConfirmToken);

  boolean sendPasswordSuccessfullyChanged(User user);

  boolean sendAccountConfirmed(User user);
}
