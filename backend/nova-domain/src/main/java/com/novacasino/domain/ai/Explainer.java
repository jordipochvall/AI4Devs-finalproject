package com.novacasino.domain.ai;

/**
 * Port for natural-language explanations of simulation results (AI explainability, HU-8). The
 * production adapter calls Anthropic's Claude; tests use a deterministic fake. Kept in
 * {@code nova-domain} so the use case depends on the abstraction, not on any SDK.
 */
public interface Explainer {

    /**
     * Answers a prompt (question + simulation metrics context).
     *
     * @param prompt the composed prompt
     * @return the answer and the model that produced it (for traceability)
     */
    Explanation explain(String prompt);
}
