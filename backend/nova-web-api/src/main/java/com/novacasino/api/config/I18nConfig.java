package com.novacasino.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Resolves the request locale from the {@code Accept-Language} header.
 * Supported languages: es (default) and en. The API is stateless: no session or cookie
 * is used to persist the language.
 */
@Configuration
public class I18nConfig {

    /** Locale resolver based on Accept-Language, defaulting to Spanish. */
    @Bean
    LocaleResolver localeResolver() {
        final AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("es"));
        resolver.setSupportedLocales(List.of(
                Locale.forLanguageTag("es"),
                Locale.forLanguageTag("en")
        ));
        return resolver;
    }
}
