package com.novacasino.api.i18n;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AC3: el test de cobertura de claves falla si falta alguna traducción en cualquiera
 * de los dos idiomas. Compara los conjuntos de claves de los dos bundles de la API.
 */
class MessagesI18nParityTest {

    private Properties load(String path) throws IOException {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(in).as("bundle no encontrado: %s", path).isNotNull();
            props.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return props;
    }

    @Test
    void esAndEnBundlesHaveIdenticalKeySets() throws IOException {
        Set<String> es = new TreeSet<>(load("i18n/messages.properties").stringPropertyNames());
        Set<String> en = new TreeSet<>(load("i18n/messages_en.properties").stringPropertyNames());

        assertThat(en)
                .as("claves presentes en ES pero ausentes en EN: %s", diff(es, en))
                .containsExactlyInAnyOrderElementsOf(es);
    }

    @Test
    void noBundleHasBlankValues() throws IOException {
        for (String bundle : new String[]{"i18n/messages.properties", "i18n/messages_en.properties"}) {
            Properties p = load(bundle);
            for (String key : p.stringPropertyNames()) {
                assertThat(p.getProperty(key))
                        .as("valor en blanco para %s en %s", key, bundle)
                        .isNotBlank();
            }
        }
    }

    private Set<String> diff(Set<String> a, Set<String> b) {
        Set<String> d = new TreeSet<>(a);
        d.removeAll(b);
        return d;
    }
}
