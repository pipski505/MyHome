package com.myhome.services.springdatajpa;

import com.myhome.configuration.properties.mail.MailProperties;
import com.myhome.configuration.properties.mail.MailTemplatesNames;
import com.myhome.domain.SecurityToken;
import com.myhome.domain.User;
import com.myhome.services.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides email sending functionality for various user-related events such as
 * password recovery, account creation, and confirmation.
 */
@Service
@ConditionalOnProperty(value = "spring.mail.devMode", havingValue = "false", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class MailSDJpaService implements MailService {

  private final ITemplateEngine emailTemplateEngine;
  private final JavaMailSender mailSender;
  private final ResourceBundleMessageSource messageSource;
  private final MailProperties mailProperties;

  /**
   * Sends a password recovery email to a user with a unique recovery code. It populates
   * a template model with the user's name and recovery code, then uses a mail service
   * to send the email. The function returns a boolean indicating whether the email was
   * sent successfully.
   *
   * @param user recipient of the password recovery email, with attributes such as name
   * used in the email template.
   *
   * @param randomCode recovery code generated for the specified user, which is included
   * in the email template sent to the user's email address.
   *
   * @returns a boolean indicating whether an email was successfully sent to the user.
   */
  @Override
  public boolean sendPasswordRecoverCode(User user, String randomCode) {
    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("username", user.getName());
    templateModel.put("recoverCode", randomCode);
    String passwordRecoverSubject = getLocalizedMessage("locale.EmailSubject.passwordRecover");
    boolean mailSent = send(user.getEmail(), passwordRecoverSubject,
        MailTemplatesNames.PASSWORD_RESET.filename, templateModel);
    return mailSent;
  }

  /**
   * Sends an email notification to a user when their password is changed. It uses a
   * template model with the user's name, sends a mail with the password changed subject,
   * and returns a boolean indicating whether the mail was sent successfully.
   *
   * @param user user for whom the password change notification email is being sent.
   *
   * @returns a boolean value indicating whether the password change email was sent successfully.
   */
  @Override
  public boolean sendPasswordSuccessfullyChanged(User user) {
    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("username", user.getName());
    String passwordChangedSubject = getLocalizedMessage("locale.EmailSubject.passwordChanged");
    boolean mailSent = send(user.getEmail(), passwordChangedSubject,
        MailTemplatesNames.PASSWORD_CHANGED.filename, templateModel);
    return mailSent;
  }

  /**
   * Generates an email to a user upon account creation, including a link to confirm
   * their email address and their username. The email is sent if the send operation
   * is successful. The email subject and template are retrieved from the application's
   * resources.
   *
   * @param user user account for which an email confirming account creation is being
   * sent.
   *
   * Extract its name.
   *
   * @param emailConfirmToken security token used for email confirmation, passed to the
   * `getAccountConfirmLink` method to generate a confirmation link.
   *
   * Destructure
   * - emailConfirmToken: a SecurityToken object
   *
   * Properties
   * - emailConfirmToken: its main properties are not explicitly mentioned, but it is
   * likely to have a token value and possibly a token expiration time.
   *
   * @returns a boolean value indicating whether the email was successfully sent.
   */
  @Override
  public boolean sendAccountCreated(User user, SecurityToken emailConfirmToken) {
    Map<String, Object> templateModel = new HashMap<>();
    String emailConfirmLink = getAccountConfirmLink(user, emailConfirmToken);
    templateModel.put("username", user.getName());
    templateModel.put("emailConfirmLink", emailConfirmLink);
    String accountCreatedSubject = getLocalizedMessage("locale.EmailSubject.accountCreated");
    boolean mailSent = send(user.getEmail(), accountCreatedSubject,
        MailTemplatesNames.ACCOUNT_CREATED.filename, templateModel);
    return mailSent;
  }

  /**
   * Sends an email to a user confirming their account.
   *
   * @param user account owner for whom the email confirmation is being sent, providing
   * information such as their name.
   *
   * @returns a boolean value indicating whether the confirmation email was sent successfully.
   */
  @Override
  public boolean sendAccountConfirmed(User user) {
    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("username", user.getName());
    String accountConfirmedSubject = getLocalizedMessage("locale.EmailSubject.accountConfirmed");
    boolean mailSent = send(user.getEmail(), accountConfirmedSubject,
        MailTemplatesNames.ACCOUNT_CONFIRMED.filename, templateModel);
    return mailSent;
  }

  /**
   * Compose and sends an HTML email message to a specified recipient using a MimeMessage
   * and MimeMessageHelper, with the sender's email address, subject, and HTML body.
   *
   * @param to recipient's email address to which the message will be sent.
   *
   * @param subject title of the email message being sent.
   *
   * @param htmlBody content of the email message to be sent in HTML format.
   */
  private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    helper.setFrom(mailProperties.getUsername());
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlBody, true);
    mailSender.send(message);
  }

  /**
   * Templates and sends an email to the specified recipient using a Thymeleaf engine
   * for HTML processing, returning true upon success and false upon failure.
   *
   * @param emailTo recipient's email address to which the email is to be sent.
   *
   * @param subject subject of the email to be sent.
   *
   * @param templateName name of the Thymeleaf template to be processed and used for
   * email content.
   *
   * @param templateModel data that is used to populate placeholders in the email template.
   *
   * Extract its key-value pairs.
   *
   * @returns a boolean value indicating success (true) or failure (false) of the email
   * sending process.
   */
  private boolean send(String emailTo, String subject, String templateName, Map<String, Object> templateModel) {
    try {
      Context thymeleafContext = new Context(LocaleContextHolder.getLocale());
      thymeleafContext.setVariables(templateModel);
      String htmlBody = emailTemplateEngine.process(templateName, thymeleafContext);
      sendHtmlMessage(emailTo, subject, htmlBody);
    } catch (MailException | MessagingException mailException) {
      log.error("Mail send error!", mailException);
      return false;
    }
    return true;
  }

  /**
   * Generates a URL for email confirmation by combining the base URL, user's ID, and
   * security token. It uses the `ServletUriComponentsBuilder` to construct the base
   * URL. The resulting URL is formatted as a string for return.
   *
   * @param user user whose email confirmation link is being generated.
   *
   * @param token SecurityToken object containing a unique token used for email confirmation.
   *
   * @returns a URL for email confirmation, composed of base URL, user ID, and token.
   */
  private String getAccountConfirmLink(User user, SecurityToken token) {
    String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
        .replacePath(null)
        .build()
        .toUriString();
    return String.format("%s/users/%s/email-confirm/%s", baseUrl, user.getUserId(), token.getToken());
  }

  /**
   * Retrieves a localized message from a message source based on the provided property.
   * If an exception occurs, it returns a default error message with the property name.
   * The function returns the localized message or the default error message.
   *
   * @param prop property key used to retrieve a localized message from the message source.
   *
   * @returns a localized message or a fallback string with a localization error message.
   */
  private String getLocalizedMessage(String prop) {
    String message = "";
    try {
      message = messageSource.getMessage(prop, null, LocaleContextHolder.getLocale());
    } catch (Exception e) {
      message = prop + ": localization error";
    }
    return message;
  }

}
