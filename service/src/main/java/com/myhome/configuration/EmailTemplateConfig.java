package com.myhome.configuration;

import com.myhome.configuration.properties.mail.EmailTemplateLocalizationProperties;
import com.myhome.configuration.properties.mail.EmailTemplateProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Locale;

/**
 * Configures email template settings for a Spring application using Thymeleaf.
 */
@Configuration
@RequiredArgsConstructor
public class EmailTemplateConfig {

  private final EmailTemplateProperties templateProperties;
  private final EmailTemplateLocalizationProperties localizationProperties;

  /**
   * Configures and returns a ResourceBundleMessageSource bean for internationalized
   * email messages.
   * It loads messages from a resource bundle based on properties from the
   * localizationProperties object.
   * It sets the default locale, encoding, and cache seconds accordingly.
   *
   * @returns a `ResourceBundleMessageSource` instance configured with specified properties.
   */
  @Bean
  public ResourceBundleMessageSource emailMessageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename(localizationProperties.getPath());
    messageSource.setDefaultLocale(Locale.ENGLISH);
    messageSource.setDefaultEncoding(localizationProperties.getEncoding());
    messageSource.setCacheSeconds(localizationProperties.getCacheSeconds());
    return messageSource;
  }

  /**
   * Configures a SpringTemplateEngine instance with a Thymeleaf template resolver and
   * sets the message source for the template engine to the provided ResourceBundleMessageSource
   * instance.
   *
   * @param emailMessageSource source of message values for the SpringTemplateEngine,
   * allowing it to resolve messages in Thymeleaf templates.
   *
   * @returns a SpringTemplateEngine object configured with a template resolver and
   * message source.
   */
  @Bean
  public SpringTemplateEngine thymeleafTemplateEngine(ResourceBundleMessageSource emailMessageSource) {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(thymeleafTemplateResolver());
    templateEngine.setTemplateEngineMessageSource(emailMessageSource);
    return templateEngine;
  }

  /**
   * Configures a ClassLoaderTemplateResolver for Thymeleaf templates, setting properties
   * such as prefix, suffix, template mode, character encoding, and cacheability based
   * on provided template properties.
   *
   * @returns a Thymeleaf template resolver object configured with specified properties.
   *
   * It is an instance of `ClassLoaderTemplateResolver`, a Thymeleaf template resolver.
   * It has a prefix set to the template path, possibly appended with a file separator.
   * Its suffix, template mode, character encoding, and cacheability are determined by
   * the `templateProperties`.
   */
  private ITemplateResolver thymeleafTemplateResolver() {
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

    String templatePath = templateProperties.getPath();
    String fileSeparator = System.getProperty("file.separator");
    templateResolver.setPrefix(templatePath.endsWith(fileSeparator) ? templatePath : templatePath + fileSeparator);

    templateResolver.setSuffix(templateProperties.getFormat());
    templateResolver.setTemplateMode(templateProperties.getMode());
    templateResolver.setCharacterEncoding(templateProperties.getEncoding());
    templateResolver.setCacheable(templateProperties.isCache());
    return templateResolver;
  }

}
