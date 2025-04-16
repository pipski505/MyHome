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
 * Configures Email Template Engine for Thymeleaf template rendering.
 */
@Configuration
@RequiredArgsConstructor
public class EmailTemplateConfig {

  private final EmailTemplateProperties templateProperties;
  private final EmailTemplateLocalizationProperties localizationProperties;

  /**
   * Configures and returns a `ResourceBundleMessageSource` instance, which is a bean
   * used to retrieve localized messages from a resource bundle. It sets the base name,
   * default locale, encoding, and cache seconds based on the provided properties.
   *
   * @returns a ResourceBundleMessageSource object configured with specified properties.
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
   * Configures and returns a SpringTemplateEngine instance, initializing it with a
   * Thymeleaf template resolver and a ResourceBundleMessageSource for email message resolution.
   *
   * @param emailMessageSource source of messages to be used for resolving messages in
   * the template engine.
   *
   * @returns a SpringTemplateEngine instance configured with a Thymeleaf template
   * resolver and email message source.
   */
  @Bean
  public SpringTemplateEngine thymeleafTemplateEngine(ResourceBundleMessageSource emailMessageSource) {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(thymeleafTemplateResolver());
    templateEngine.setTemplateEngineMessageSource(emailMessageSource);
    return templateEngine;
  }

  /**
   * Configures a Thymeleaf template resolver, setting its prefix, suffix, template
   * mode, character encoding, and cacheability based on properties from the
   * `templateProperties` object.
   *
   * @returns a configured Thymeleaf template resolver object.
   *
   * Set to prefix the template path with the file separator if it does not already end
   * with one.
   * Set to suffix the template with the specified format.
   * Set to template mode to the specified mode.
   * Set to character encoding to the specified encoding.
   * Set to cacheable to the specified cache value.
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
