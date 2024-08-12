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
 * Is responsible for configuring email templates using Thymeleaf and
 * ResourceBundleMessageSource. It provides beans for message source and template
 * engine, which can be used to render email templates based on locale and encoding
 * settings.
 */
@Configuration
@RequiredArgsConstructor
public class EmailTemplateConfig {

  private final EmailTemplateProperties templateProperties;
  private final EmailTemplateLocalizationProperties localizationProperties;

  /**
   * Initializes a `ResourceBundleMessageSource` bean, setting its properties from the
   * `localizationProperties`. It specifies the resource bundle file name, default
   * locale, encoding, and cache seconds for message retrieval. The function returns
   * the configured `ResourceBundleMessageSource` instance.
   * 
   * @returns a ResourceBundleMessageSource bean.
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
   * Configures a Spring Template Engine for Thymeleaf, setting up a resolver for
   * templates and an engine message source from ResourceBundleMessageSource.
   * 
   * @param emailMessageSource ResourceBundleMessageSource used to resolve messages and
   * text for Thymeleaf templates by the SpringTemplateEngine.
   * 
   * @returns an instance of `SpringTemplateEngine`.
   */
  @Bean
  public SpringTemplateEngine thymeleafTemplateEngine(ResourceBundleMessageSource emailMessageSource) {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(thymeleafTemplateResolver());
    templateEngine.setTemplateEngineMessageSource(emailMessageSource);
    return templateEngine;
  }

  /**
   * Creates and configures a Thymeleaf template resolver instance. It sets various
   * properties such as prefix, suffix, template mode, character encoding, and cacheability
   * based on input values from the `templateProperties`. The configured resolver is
   * then returned for use.
   * 
   * @returns a configured Thymeleaf template resolver object.
   * 
   * Set prefix specifies the directory where templates are located.
   * Suffix is set to a specific format for the template files.
   * Template mode determines how templates will be processed.
   * Character encoding specifies the character set used in the template files.
   * Cacheable property controls whether or not the resolved templates can be cached.
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
