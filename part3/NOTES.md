# Part 3 — Notes

## What confidence threshold would you gate CI on?

I wouldn't gate CI on the LLM judge alone, and not from day one. Two layers:

1. **Hard gate — deterministic UI checks.** A rewrite must actually change the script
   and the editor must show the "Keep changes" result. These are objective and already
   covered by Part 2; they gate CI on their own.

2. **Quality signal — the LLM rubric.** I'd run it report-only first to collect a
   baseline of judge-vs-human agreement, then gate once agreement is high (~90%+).
   When gating, I'd use `overall_pass && confidence >= 0.80` as pass, treat
   `0.60–0.80` as **needs-review** (warning, not a hard fail), and `< 0.60` as fail.
   I'd also gate on the **aggregate** (e.g. fail the build only if fewer than ~75% of
   prompts pass) rather than any single prompt, since one non-deterministic rewrite
   shouldn't red-line the pipeline. Temperature 0 and a pinned model version keep the
   judge reproducible.

## How would you handle the LLM judge disagreeing with a human reviewer?

The human is the source of truth; the judge is a pre-filter, not the final word.

- Keep a small **golden set** of human-labelled examples and track judge agreement
  (precision/recall) over time — that number decides how much we trust it for gating.
- On disagreement, **log the case with the judge's reasoning** for quick human triage,
  and route low-confidence disagreements to **needs-review** instead of failing.
- Feed recurring disagreements back into the rubric/prompt (few-shot examples), and if
  a systematic bias shows up, adjust the threshold or switch model.
- Only tighten gating after agreement is consistently high; until then the judge
  informs reviewers rather than blocking merges.
