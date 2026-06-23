package com.novacasino.application.math.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigValidatorTest {

    private final ObjectMapper om = new ObjectMapper();
    private final ConfigValidator validator = new ConfigValidator();

    private static final String VALID = """
            {
              "grid": { "cols": 3, "rows": 3 },
              "symbols": [
                { "id": "A",  "kind": "REGULAR" },
                { "id": "SC", "kind": "SCATTER" },
                { "id": "W",  "kind": "WILD" }
              ],
              "reels": [
                ["A","SC","W"],
                ["A","A","SC"],
                ["A","W","SC"]
              ],
              "paylines": [ [0,0,0], [1,1,1] ],
              "paytable": [ { "symbol": "A", "payouts": { "2": 1, "3": 5 } } ],
              "scatterPays": { "SC": { "2": 1, "3": 3 } },
              "bonus": { "freeSpins": { "triggerSymbol": "SC", "minTriggerCount": 3,
                                        "award": { "3": 8, "4": 12, "5": 20 } } }
            }
            """;

    private JsonNode json(String s) {
        try { return om.readTree(s); } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    void validConfig_passes() {
        assertThatCode(() -> validator.validate(json(VALID))).doesNotThrowAnyException();
    }

    @Test
    void paytableSymbolNotFound_fails() {
        String bad = VALID.replace("\"symbol\": \"A\"", "\"symbol\": \"ZZ\"");
        assertThatThrownBy(() -> validator.validate(json(bad)))
                .isInstanceOf(ConfigValidationException.class);
    }

    @Test
    void paylineOutOfRange_fails() {
        String bad = VALID.replace("[0,0,0]", "[0,0,3]"); // row 3 out of 0..2
        assertThatThrownBy(() -> validator.validate(json(bad)))
                .isInstanceOf(ConfigValidationException.class);
    }

    @Test
    void triggerSymbolNotScatter_fails() {
        String bad = VALID.replace("\"triggerSymbol\": \"SC\"", "\"triggerSymbol\": \"A\"");
        assertThatThrownBy(() -> validator.validate(json(bad)))
                .isInstanceOf(ConfigValidationException.class);
    }

    @Test
    void paymentGap_fails() {
        String bad = VALID.replace("\"payouts\": { \"2\": 1, \"3\": 5 }",
                                   "\"payouts\": { \"2\": 1, \"4\": 5 }"); // missing count 3
        assertThatThrownBy(() -> validator.validate(json(bad)))
                .isInstanceOf(ConfigValidationException.class);
    }

    @Test
    void reelSymbolNotFound_fails() {
        String bad = VALID.replace("[\"A\",\"SC\",\"W\"]", "[\"A\",\"SC\",\"XX\"]");
        assertThatThrownBy(() -> validator.validate(json(bad)))
                .isInstanceOf(ConfigValidationException.class);
    }
}
