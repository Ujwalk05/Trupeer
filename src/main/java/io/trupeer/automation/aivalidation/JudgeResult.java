package io.trupeer.automation.aivalidation;

import java.util.List;

/** The LLM judge's structured verdict for one prompt. */
public record JudgeResult(List<Criterion> criteria,
                          double confidence,
                          boolean overallPass,
                          String summary) {

    /** Per-criterion pass/fail from the rubric. */
    public record Criterion(String name, boolean pass, String reasoning) {
    }
}
