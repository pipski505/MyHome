package com.myhome.services.unit;

import com.myhome.configuration.properties.mail.MailProperties;
import com.myhome.domain.SecurityToken;
import com.myhome.domain.User;
import com.myhome.services.springdatajpa.MailSDJpaService;
import helpers.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

/**
 * Provides unit tests for the MailSDJpaService class using Mockito for mocking
 * dependencies and JUnit for assertions.
 */
class MailSDJpaServiceTest {

  @Mock
  private JavaMailSender mailSender;
  @Mock
  private ITemplateEngine emailTemplateEngine;
  @Mock
  private ResourceBundleMessageSource messageSource;
  private MockHttpServletRequest mockRequest;

  private MailSDJpaService mailSDJpaService;

  private MailProperties mailProperties = TestUtils.MailPropertiesHelper.getTestMailProperties();

  /**
   * Initializes the test environment by setting up mock objects and attributes, and
   * injecting dependencies into the `MailSDJpaService` instance. It also sets up the
   * request context and attributes for the test.
   */
  @BeforeEach
  private void init() {
    MockitoAnnotations.initMocks(this);

    mockRequest = new MockHttpServletRequest();
    mockRequest.setContextPath("http://localhost:8080");
    ServletRequestAttributes attrs = new ServletRequestAttributes(mockRequest);
    RequestContextHolder.setRequestAttributes(attrs);

    mailSDJpaService = new MailSDJpaService(emailTemplateEngine, mailSender, messageSource, mailProperties);
  }

  /**
   * Tests the functionality of sending a password recovery code via email when a
   * `MailSendException` occurs. It verifies that the `mailSent` flag remains false in
   * the event of a mail sending exception.
   */
  @Test
  void sendPasswordRecoverCodeMailException() {
    // given
    MimeMessage mimeMessage = new MimeMessage((Session)null);
    User user = getTestUser();
    given(emailTemplateEngine.process(eq(""), any(Context.class)))
        .willReturn("HTML");
    given(mailSender.createMimeMessage())
        .willReturn(mimeMessage);
    doThrow(MailSendException.class).when(mailSender).send(mimeMessage);

    // when
    boolean mailSent = mailSDJpaService.sendPasswordRecoverCode(user, "test-token");

    // then
    assertFalse(mailSent);
  }

  /**
   * Tests the functionality of sending a password successfully changed email, but it
   * is expected to fail due to a MailSendException when the mail is sent.
   */
  @Test
  void sendPasswordSuccessfullyChangedMailException() {
    // given
    MimeMessage mimeMessage = new MimeMessage((Session)null);
    User user = getTestUser();
    given(emailTemplateEngine.process(eq(""), any(Context.class)))
        .willReturn("HTML");
    given(mailSender.createMimeMessage())
        .willReturn(mimeMessage);
    doThrow(MailSendException.class).when(mailSender).send(mimeMessage);

    // when
    boolean mailSent = mailSDJpaService.sendPasswordSuccessfullyChanged(user);

    // then
    assertFalse(mailSent);
  }

  /**
   * Tests the functionality of sending an email when a MailSendException occurs. It
   * verifies that the mail is not sent in this case.
   */
  @Test
  void sendEmailConfirmedMailException() {
    // given
    MimeMessage mimeMessage = new MimeMessage((Session)null);
    User user = getTestUser();
    given(emailTemplateEngine.process(eq(""), any(Context.class)))
        .willReturn("HTML");
    given(mailSender.createMimeMessage())
        .willReturn(mimeMessage);
    doThrow(MailSendException.class).when(mailSender).send(mimeMessage);

    // when
    boolean mailSent = mailSDJpaService.sendAccountConfirmed(user);

    // then
    assertFalse(mailSent);
  }

  /**
   * Tests the functionality of sending an account created email when a mail send
   * exception occurs. It verifies that the `mailSDJpaService` returns `false` when an
   * exception is thrown during mail sending.
   */
  @Test
  void sendEmailCreatedMailException() {
    // given
    SecurityToken token = new SecurityToken();
    token.setToken("token");
    MimeMessage mimeMessage = new MimeMessage((Session)null);
    User user = getTestUser();
    given(emailTemplateEngine.process(eq(""), any(Context.class)))
        .willReturn("HTML");
    given(mailSender.createMimeMessage())
        .willReturn(mimeMessage);
    doThrow(MailSendException.class).when(mailSender).send(mimeMessage);

    // when
    boolean mailSent = mailSDJpaService.sendAccountCreated(user, token);

    // then
    assertFalse(mailSent);
  }

  /**
   * Creates a User object, initializes its email field with the string "test-email",
   * and returns the object. This function appears to be used for testing purposes.
   *
   * @returns A `User` object with an email address of "test-email".
   */
  private User getTestUser() {
    User user = new User();
    user.setEmail("test-email");
    return user;
  }

}