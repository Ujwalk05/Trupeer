# Part 3 — AI-Augmented Testing (LLM-as-Judge)

Validates Trupeer's **"Modify Script with AI"** feature. The rewrite output is
non-deterministic, so instead of string-matching we drive the feature with several
prompts, capture each rewritten script from the UI, and have an **LLM judge (OpenAI)**
grade the rewrite against a rubric — then print a pass/fail summary per prompt.

## Reuses Part 2

This is **not** a separate project. The Part 3 code lives in the Part 2 Maven module and
reuses its Playwright plumbing — `DriverManager`, `ConfigurationManager`, the page-object
locators, and especially the shared `AuthService` (login) and `EditorService` (open a video
+ the Modify-Script-with-AI flow). The same `EditorService.rewriteWithPrompt(...)` used here
backs the Part 2 Cucumber steps.

Part 3 classes (`src/main/java/io/trupeer/automation/aivalidation/`):

| Class | Responsibility |
|---|---|
| `ScriptValidationRunner` | Entry point: login → open editor → per-prompt rewrite → judge → summary |
| `OpenAiJudge` | Calls the OpenAI chat API with the rubric, parses the JSON verdict |
| `PromptCase` | A prompt to exercise (label + text) |
| `JudgeResult` | The structured verdict (criteria + confidence + overall pass) |

## The rubric

For each prompt the judge grades four criteria (per-criterion pass/fail + reasoning):

| Criterion | Question |
|---|---|
| `reflects_intent` | Does the modified script reflect the intent of the user's prompt? |
| `coherent` | Is the output coherent and grammatically correct? |
| `preserves_core` | Does it preserve the core information of the original? |
| `meaningfully_different` | Is it a meaningful change, not a trivial reword? |

It also returns a **confidence** (0–1) and an **overall_pass** (true only if every criterion
passes). The call uses `temperature: 0` and a JSON response format so the verdict is
reproducible and strictly parseable.

## Prerequisites

- JDK 17+, Maven 3.8+
- The same Trupeer account (with a video) used by Part 2
- An OpenAI API key

## Setup (env vars)

```powershell
# PowerShell
$env:TRUPEER_EMAIL="you@example.com"
$env:TRUPEER_PASSWORD="your-password"
$env:OPENAI_API_KEY="sk-..."
# optional: $env:OPENAI_MODEL="gpt-4o-mini"   (default)
```
```bash
# bash
export TRUPEER_EMAIL="you@example.com"
export TRUPEER_PASSWORD="your-password"
export OPENAI_API_KEY="sk-..."
```

## Run (single command)

From the Part 2 project root. Note it's **`test-compile`** (not `compile`) — the runner uses
`classpathScope=test` so it can read `config/qa.properties`, a test resource:

```bash
mvn -q test-compile exec:java
```

Capture a sample run to a file:

```bash
mvn -q test-compile exec:java > part3/sample-run.txt 2>&1
```

Run headless:

```bash
mvn -q test-compile exec:java -Dheadless=true
```

## How it works (flow)

1. Log in (`AuthService`) and open the first video in the editor (`EditorService`).
2. Capture the **original** script once.
3. For each prompt: open the AI dialog, submit the prompt, wait for the rewrite, read the
   modified script from the UI, then **Discard changes** (revert) so the base script stays
   constant and the account isn't permanently modified.
4. Send `(original, prompt, modified)` + the rubric to `OpenAiJudge`.
5. Print per-criterion PASS/FAIL, confidence and overall per prompt, then an overall summary.

## Notes

- Each prompt triggers a real AI rewrite and **consumes the account's Trupeer AI minutes**
  (4 prompts → 4 rewrites) — watch the remaining balance.
- If the UI returns no modified script (rate limit / no AI minutes), that prompt is recorded
  as a FAIL with a note rather than crashing the run.
- `NOTES.md` answers the two questions from the brief (CI confidence threshold, handling
  judge-vs-human disagreement). `sample-run.txt` holds output over 4 prompts.
