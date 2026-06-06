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
 * AC3: the key-coverage test fails if any translation is missing in either language.
 * Compares the key sets of the two API bundles.
 */
class MessagesI18nParityTest {

    private Properties load(String path) throws IOException {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(in).as("bundle not found: %s", path).isNotNull();
            props.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return props;
    }

    @Test
    void esAndEnBundlesHaveIdenticalKeySets() throws IOException {
        Set<String> es = new TreeSet<>(load("i18n/messages.properties").stringPropertyNames());
        Set<String> en = new TreeSet<>(load("i18n/messages_en.properties").stringPropertyNames());

        assertThat(en)
                .as("keys present in ES but missing in EN: %s", diff(es, en))
                .containsExactlyInAnyOrderElementsOf(es);
    }

    @Test
    void noBundleHasBlankValues() throws IOException {
        for (String bundle : new String[]{"i18n/messages.properties", "i18n/messages_en.properties"}) {
            Properties p = load(bundle);
            for (String key : p.stringPropertyNames()) {
                assertThat(p.getProperty(key))
                        .as("blank value for %s in %s", key, bundle)
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
