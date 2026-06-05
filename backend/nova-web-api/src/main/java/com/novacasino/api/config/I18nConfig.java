package com.novacasino.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Configura la resolución de idioma a partir de la cabecera {@code Accept-Language}.
 * Idiomas soportados: es (defecto) y en.
 * La API es stateless: no usa sesión ni cookie para persistir el idioma.
 */
@Configuration
public class I18nConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("es"));
        resolver.setSupportedLocales(List.of(
                Locale.forLanguageTag("es"),
                Locale.forLanguageTag("en")
        ));
        return resolver;
    }
}
