package io.trupeer.automation.aivalidation;

import io.trupeer.automation.core.config.ConfigurationManager;
import io.trupeer.automation.core.driver.DriverManager;
import io.trupeer.automation.services.AuthService;
import io.trupeer.automation.services.EditorService;

import java.util.List;
import java.util.Optional;


public class ScriptValidationRunner {

    // The prompts we exercise the feature with.
    private static final List<PromptCase> PROMPTS = List.of(
            new PromptCase("More professional", "Make this more professional"),
            new PromptCase("Add call to action", "Add a call to action at the end"),
            new PromptCase("Translate to Spanish", "Translate to Spanish"),
            new PromptCase("More concise", "Make this script more concise"));

    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Set OPENAI_API_KEY before running (see part3/README.md).");
            System.exit(1);
        }
        String model = System.getenv().getOrDefault("OPENAI_MODEL", "gpt-4o-mini");
        ConfigurationManager cfg = ConfigurationManager.get();

        DriverManager.startScenario();
        try {
            AuthService auth = new AuthService();
            EditorService editor = new EditorService();
            OpenAiJudge judge = new OpenAiJudge(apiKey, model);

            // Go straight to the login page (/auth?tab=login) and authenticate.
            auth.openLoginPage();
            if (auth.isLoginFormVisible()) {
                auth.login(cfg.email(), cfg.password());
            }
            if (!auth.isOnDashboard()) {
                throw new IllegalStateException("Login failed - dashboard did not load.");
            }

            editor.openFirstVideo();
            editor.editorLoaded();

            String original = editor.currentScript();
            System.out.println(header(model));
            System.out.println("Original script:\n" + snippet(original) + "\n");

            int passed = 0;
            double confidenceSum = 0;
            int judged = 0;

            for (int i = 0; i < PROMPTS.size(); i++) {
                PromptCase pc = PROMPTS.get(i);
                System.out.printf("[%d] %s  ->  \"%s\"%n", i + 1, pc.label(), pc.prompt());

                Optional<String> modified = editor.rewriteWithPrompt(pc.prompt(), cfg.aiTimeoutMs());
                if (modified.isEmpty()) {
                    System.out.println("    UI returned no modified script (rate limit / no AI minutes?) - FAIL\n");
                    continue;
                }

                JudgeResult verdict = judge.evaluate(original, pc.prompt(), modified.get());
                printVerdict(modified.get(), verdict);

                judged++;
                confidenceSum += verdict.confidence();
                if (verdict.overallPass()) {
                    passed++;
                }
            }

            printSummary(passed, judged, confidenceSum);
        } finally {
            DriverManager.teardown();
        }
    }

    private static void printVerdict(String modified, JudgeResult verdict) {
        System.out.println("    output: " + snippet(modified));
        for (JudgeResult.Criterion c : verdict.criteria()) {
            System.out.printf("    %-24s %-4s - %s%n", c.name(), c.pass() ? "PASS" : "FAIL", c.reasoning());
        }
        System.out.printf("    confidence: %.2f   OVERALL: %s%n%n",
                verdict.confidence(), verdict.overallPass() ? "PASS" : "FAIL");
    }

    private static void printSummary(int passed, int judged, double confidenceSum) {
        double avgConfidence = judged == 0 ? 0 : confidenceSum / judged;
        System.out.println("------------------------------------------------------------------------");
        System.out.printf(" SUMMARY: %d/%d prompts passed | avg confidence %.2f%n",
                passed, PROMPTS.size(), avgConfidence);
        System.out.println("------------------------------------------------------------------------");
    }

    private static String header(String model) {
        return """
                ========================================================================
                 Trupeer "Modify Script with AI" - LLM-as-Judge Validation
                 Judge model: %s
                ========================================================================
                """.formatted(model);
    }

    private static String snippet(String text) {
        String oneLine = text.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= 160 ? oneLine : oneLine.substring(0, 160) + " ...";
    }
}
