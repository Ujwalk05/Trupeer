package io.trupeer.automation.aivalidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class OpenAiJudge {

    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
            You are a strict QA reviewer evaluating an AI feature that rewrites a video
            script based on a user's instruction. Judge only what the evidence supports.
            Respond with JSON only.""";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;
    private final String model;

    public OpenAiJudge(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public JudgeResult evaluate(String original, String userPrompt, String modified) {
        try {
            String requestBody = buildRequest(original, userPrompt, modified);
            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("OpenAI API returned " + response.statusCode()
                        + ": " + response.body());
            }
            String content = mapper.readTree(response.body())
                    .at("/choices/0/message/content").asText();
            return parseVerdict(mapper.readTree(content));
        } catch (Exception e) {
            throw new RuntimeException("LLM judge call failed: " + e.getMessage(), e);
        }
    }

    private String buildRequest(String original, String userPrompt, String modified) {
        String instruction = """
                Evaluate the MODIFIED script against the ORIGINAL and the USER PROMPT using these criteria:
                  - reflects_intent: the modified script reflects the intent of the user's prompt
                  - coherent: the output is coherent and grammatically correct
                  - preserves_core: it preserves the core information/message of the original
                  - meaningfully_different: it is a meaningful change, not a trivial reword (or no change)

                Return JSON with exactly this shape:
                {
                  "criteria": [
                    {"name": "reflects_intent", "pass": true, "reasoning": "<short>"},
                    {"name": "coherent", "pass": true, "reasoning": "<short>"},
                    {"name": "preserves_core", "pass": true, "reasoning": "<short>"},
                    {"name": "meaningfully_different", "pass": true, "reasoning": "<short>"}
                  ],
                  "confidence": 0.0,
                  "overall_pass": true,
                  "summary": "<one sentence>"
                }
                "confidence" (0-1) is how sure you are of this verdict. "overall_pass" is true only if every criterion passes.

                USER PROMPT:
                %s

                ORIGINAL SCRIPT:
                %s

                MODIFIED SCRIPT:
                %s
                """.formatted(userPrompt, original, modified);

        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0);
        body.set("response_format", mapper.createObjectNode().put("type", "json_object"));
        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", SYSTEM_PROMPT);
        messages.addObject().put("role", "user").put("content", instruction);
        return body.toString();
    }

    private JudgeResult parseVerdict(JsonNode json) {
        List<JudgeResult.Criterion> criteria = new ArrayList<>();
        for (JsonNode c : json.path("criteria")) {
            criteria.add(new JudgeResult.Criterion(
                    c.path("name").asText(),
                    c.path("pass").asBoolean(),
                    c.path("reasoning").asText("")));
        }
        return new JudgeResult(
                criteria,
                json.path("confidence").asDouble(0),
                json.path("overall_pass").asBoolean(false),
                json.path("summary").asText(""));
    }
}
