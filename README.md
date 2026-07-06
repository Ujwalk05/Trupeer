# Part 2 — Trupeer E2E Automation (Playwright + Java + Cucumber)

A BDD suite covering the core Trupeer flows — **login/logout**, **open the editor**,
**Modify Script with AI**, and an **editor interaction (change background)** — built
with **Playwright for Java** driven by **Cucumber**, using a Page Object Model with a
locator catalog, reusable action helpers, structured logging (log4j2) and Extent reporting.

> Part 3 (AI-augmented testing) lives in the same project and reuses this framework —
> see [`part3/README.md`](part3/README.md).

## Prerequisites

- JDK 17+ (`java -version`)
- Maven 3.8+ (`mvn -version`)
- A Trupeer account with a recorded video that has a script (from Part 1)
- Internet on first run — Playwright downloads its Chromium driver automatically

## Setup — credentials via env vars (never hardcoded)

Read from environment variables and interpolated into `src/test/resources/config/qa.properties`:

```powershell
# PowerShell
$env:TRUPEER_EMAIL="you@example.com"; $env:TRUPEER_PASSWORD="your-password"
```
```bash
# bash / zsh
export TRUPEER_EMAIL="you@example.com"
export TRUPEER_PASSWORD="your-password"
```

## Run

```bash
mvn test                                          # whole suite (headed, maximized)
mvn test -Dheadless=true                          # headless (CI)
mvn test "-Dcucumber.filter.tags=@login"          # one feature
mvn test "-Dcucumber.filter.tags=@ai and not @ai-generate"   # AI negatives (no AI-minute spend)
mvn test "-Dcucumber.filter.tags=@negative"       # only negative tests
```

Credentials can also be passed inline instead of env vars:

```bash
mvn test "-Dtrupeer.email=you@example.com" "-Dtrupeer.password=..."
```

## Reports & logs (after a run)

- `target/reports/Spark.html` — Extent (Spark) report; screenshots embedded on failure
- `target/cucumber-reports/report.html` / `report.json` — Cucumber reports
- `target/logs/execution.log`, `error.log` — structured run logs
- `target/screenshots/<run-id>/` — failure screenshots

## Architecture

```
src/main/java/io/trupeer/automation/
├── core/
│   ├── config/     ConfigurationManager (env .properties + ${ENV_VAR}), FrameworkConstants
│   ├── driver/     PlaywrightFactory, DriverManager (thread-local, browser.reuse)
│   ├── locator/    Loc, LocatorDefinition, LocatorRegistry, LocatorStrategy
│   └── exceptions/ FrameworkException, LocatorNotFoundException
├── actions/        AbstractActionHelper + Click / Input / Validation helpers
├── pages/          LoginPage, DashboardPage, EditorPage (locator catalogs) + Pages
├── services/       AuthService, EditorService (login + editor flows, shared with Part 3)
├── observability/  StructuredLogger (log4j2)
└── reporting/      ScreenshotService

src/test/
├── java/io/trupeer/automation/
│   ├── hooks/            ScenarioHooks (@Before / @After / @AfterAll)
│   ├── runners/          TestRunner (JUnit Platform + Cucumber)
│   └── stepdefinitions/  LoginSteps, EditorSteps, ScriptAiSteps
└── resources/
    ├── config/qa.properties      environment config (creds via ${ENV})
    ├── features/*.feature        login, editor, modify_script_ai
    ├── junit-platform.properties  glue + report plugins
    ├── Log4j2.xml, extent.properties
```

**How it fits together:** Page classes are pure locator catalogs — each `Loc.define(...)`
self-registers in the `LocatorRegistry`. The login and editor flows live in `AuthService`
and `EditorService`; step definitions are thin and delegate to them (the same services
Part 3 reuses). Action helpers centralise the primary→fallback selector retry, logging and
on-failure screenshots. `DriverManager` holds one browser per run (`browser.reuse=true`) so
a login persists across scenarios.

## Browser / session lifecycle

One Chromium instance opens on the first scenario, is reused across the run, and closes once
in `@AfterAll`. Feature Backgrounds use an idempotent `the user is logged in` step, so a
feature flows in a single session without bouncing back to the login screen. Scenarios tagged
`@loggedout` (all of `login.feature`) wipe cookies + storage first so they start signed-out.

## Test coverage

| Feature | Scenario | Type |
|---|---|---|
| `login.feature` | Successful login → dashboard → logout | positive |
| `login.feature` | Invalid credentials rejected ("Wrong email or password.") | **negative** |
| `editor.feature` | Editor loads (timeline, preview, script panel) | positive |
| `editor.feature` | Change background (Visuals → Background) applies | interaction |
| `modify_script_ai.feature` | AI returns a rewritten script (`@ai-generate`) | positive |
| `modify_script_ai.feature` | Empty prompt → Rewrite Script disabled | **negative** |
| `modify_script_ai.feature` | Prompt capped at 300 characters | **negative** |

> `@ai-generate` triggers a real AI rewrite and consumes the account's AI minutes.
> Skip it with `-Dcucumber.filter.tags="not @ai-generate"`.

## Selectors

Verified against Trupeer's live DOM and centralised in the Page catalogs — stable hooks over
hashed CSS-module names: sidebar `#trupeer-nav-library`, video tile `a[href*='/video/edit']`,
Slate script blocks `[data-slate-editor='true']`, the AI prompt `textarea[maxlength='300']` +
`Rewrite Script` button, background swatches (`[role='radio']`, selected = the one with the
checkmark `:has(svg)`), and text/XPath anchors for the timeline and WebGL preview player.
