# SlothMode — Roadmap

> Let the sloth take the wheel. You've earned a nap.

This roadmap is ordered by **risk first**. The pieces most likely to be wrong (the undismissable lockout overlay, the calendar write-back, the model fallback) get proven before we build anything comfortable on top of them. Every milestone ends with a **demoable artifact** and a **test suite that passes**.

- [ ] = not started
- [~] = in progress
- [x] = done

---

## Guiding principles

1. **Prove the scary thing first.** The full-screen lockout is the whole product. If we can't make it calm *and* unskippable, nothing else matters.
2. **Every pattern gets a test.** If a design pattern doesn't have a dedicated unit test, it isn't implemented — it's sketched.
3. **Local-only, always.** No milestone is allowed to introduce a network call except the first `ollama pull`.
4. **Forkable seams.** `ModelFactory`, `BreakSource`, `BreakStrategy`, `MergeStrategy` are the extension points. Keep them small and documented.
5. **No feature without a README update.** If a user can't read how to use it, it isn't shipped.

---

## M0 — Scaffold & "Hello, Sloth" *(week 1)*

**Goal:** A runnable JavaFX app that opens a window and logs a heartbeat. Nothing clever.

- [x] `pom.xml` with Java 21, JavaFX 21, JUnit 5, AssertJ, Mockito, Logback
- [x] `SlothModeApp` (FACADE) — the single `main` entry point
- [x] `ConfigManager` (SINGLETON) — loads `~/.slothmode/config.toml`, creates the dir if missing
- [x] Logback config writing to `~/.slothmode/logs/slothmode.log`
- [x] GitHub Actions: `mvn verify` on push (JDK 21, Temurin)
- [x] `LICENSE` (MIT) committed
- [x] README "Getting started" updated to real commands

**Demo:** `mvn javafx:run` opens a window with a sloth emoji and a "SlothMode is idle" label.

**Tests:** `ConfigManagerTest` — singleton returns the same instance, reads a test TOML, survives a missing file.

---

## M1 — The Lockout Overlay *(week 2)* — **riskiest piece first**

**Goal:** Prove that a full-screen, undismissable, calm overlay is achievable in JavaFX on all three platforms.

- [x] `LockoutAbstraction` (BRIDGE abstraction) + `LockoutImplementor` interface
- [x] `FullscreenLockout` (ConcreteImplementor) — `Stage` in `StageStyle.UNDECORATED`, `fullScreenExitHint = ""`, `setFullScreenExitKeyCombination(KeyCombination.NO_MATCH)`, `alwaysOnTop = true`
- [x] `GentleNudgeLockout` (ConcreteImplementor) — a non-fullscreen toast for users who pick "gentle mode"
- [x] Calm overlay UI: sloth animation (JavaFX `Timeline`), countdown ring, "you're on a break" copy
- [x] Escape hatch for emergencies: hold a key combo for 5 seconds → overlay dismisses but logs a "skipped break" event *(this is deliberate — forcing a break shouldn't trap a user in an emergency)*
- [x] `LockoutAbstractionTest` (Mockito) — the abstraction delegates to the implementor
- [x] Manual test checklist on Windows / macOS / Linux (documented in `docs/lockout-manual-test.md`)

**Demo:** A button in the main window triggers a 10-second lockout. You cannot click, type, or `Alt+F4` out of it. It counts down and dismisses itself.

**Risk retired:** if JavaFX can't hold an undismissable overlay on some WM (looking at you, some Linux tiling WMs), we find out in week 2, not week 10.

---

## M2 — Session State Machine *(week 3)*

**Goal:** The session lifecycle exists as explicit objects, not if-else spaghetti.

- [x] `SessionState` as a **sealed interface** (Java 21) — only `Idle`, `Focus`, `Lockout`, `Reward` can implement it
- [x] `SessionContext` — holds the current state, delegates to it
- [x] Transitions: `Idle → Focus` (start), `Focus → Lockout` (timer fires), `Lockout → Reward` (break completed), `Reward → Idle` (points awarded)
- [x] `SessionSubject` (OBSERVER Subject) — `Attach`, `Detach`, `Notify`
- [x] `SessionHub` (MEDIATOR) — coordinates the timer, the overlay, and the state machine without them knowing each other
- [x] `SessionStateTest` — every legal transition, every illegal transition throws
- [x] `SessionHubTest` (Mockito) — hub notifies the right observer on each transition

**Demo:** Start a focus session, watch it move Focus → Lockout → Reward → Idle in real time in the UI.

---

## M3 — Command + Memento: the save-work round trip *(week 4)*

**Goal:** The undoable, crash-safe command history. This is the piece the README promises: *save work before the lockout hits*.

- [x] `Command` interface — `Execute`, `Unexecute`, `isReversible`
- [x] `Memento` interface — `GetState`, plus a `SaveWorkMemento` record
- [x] `SaveWorkCommand` — snapshots open documents (for v0: a stub `DocumentRegistry` that tracks "open files") before the lockout
- [x] `LockoutCommand` — wraps the overlay trigger so it's undoable in tests
- [x] `CommandHistory` — a `List<Command>` traversed by an **Iterator** (`First`, `Next`, `IsDone`, `CurrentItem`)
- [x] `CommandHistoryTest` — execute 3 commands, undo 2, redo 1, verify the history cursor is where the book says it should be (p. 62–64)
- [x] `SaveWorkCommandTest` — Execute snapshots, Unexecute restores exactly

**Demo:** A "simulate lockout" button snapshots the fake document registry, shows the overlay, and undo restores the registry to its pre-lockout state.

**Why Memento before any AI:** the README's "save-work safeguard" claim is only honest if it's tested. This milestone makes it honest.

---

## M4 — Calendar Layer: read, write, watch *(week 5–6)*

**Goal:** The calendar is the system of record. Manual breaks write to `.ics`, external edits are picked up live.

- [ ] `BreakSource` (ABSTRACT FACTORY) interface — `ListBreaks`, `AddBreak`, `RemoveBreak`
- [ ] `CalendarSource` (ConcreteFactory) using `ical4j` — reads/writes `VEVENT`s with `CATEGORIES:SLOTHMODE`
- [ ] `ManualSource` (ConcreteFactory) — in-memory only, used by tests and by users who don't want a calendar file
- [ ] `HybridSource` (ConcreteFactory) — merges calendar + manual
- [ ] `BreakRepository` (OBSERVER) — caches `List<Break>`, invalidates on write, notifies `SessionSubject`
- [ ] `AddBreakCommand` — uses `CalendarSource.CreateMemento()` → write → `BreakRepository.Invalidate()` → `Notify()` → `SessionHub.Replan()` (the sequence diagram from the design discussion)
- [ ] `.ics` file watcher via `WatchService` — external edits fire the same `Invalidate() → Notify()` chain
- [ ] `MergeStrategy` (STRATEGY) — `CalendarWins` (default), `ManualWins`, `AskUser` (v0: just logs the conflict)
- [ ] `CalendarSourceTest` — round-trip a `.ics` with `ical4j`; verify timezones survive
- [ ] `AddBreakCommandTest` — the full sequence from the sequence diagram: memento → write → invalidate → notify → replan
- [ ] `AddBreakCommandFailureTest` — simulate disk-full, verify the Memento restores the file byte-for-byte and **no observer is notified**

**Demo:** Add a break in the UI → open the `.ics` in Thunderbird/Apple Calendar → the break is there → edit it externally → SlothMode re-plans live.

**Risk retired:** `.ics` round-tripping is a known trap. If `ical4j` can't do what we need, we find out in week 5–6, not after the UI is built.

---

## M5 — Model Layer: Ollama behind a factory *(week 7)*

**Goal:** Local open-weight inference, swapable, with a mock for tests.

- [ ] `ModelFactory` (ABSTRACT FACTORY) — `CreateModel()`, `CreateAgent()`
- [ ] `OllamaModel` (ConcreteProduct) using `ollama4j` — wraps `llama3.2:3b`
- [ ] `MockModel` (ConcreteProduct) — returns canned responses; used in every test
- [ ] `LlamaCppModel` (stub ConcreteProduct) — proves the factory seam works; not implemented in v0
- [ ] First real use: `SessionSummaryAgent` — reads the `EventIterator` (ITERATOR) and asks the local model for a two-line morning digest
- [ ] `EventVisitor` (VISITOR) — `VisitBreak`, `VisitFocus`, `VisitReward`; `SummaryVisitor` is the first concrete visitor
- [ ] `ModelFactoryTest` — `MockModel` is returned in tests, `OllamaModel` is returned in production
- [ ] `SummaryVisitorTest` — canned events → expected digest text (with `MockModel`)

**Demo:** Run a focus session, trigger 3 breaks, click "morning digest" → two lines of calm text generated locally.

**The open-source claim, proven:** unplug the network. The digest still generates. That's the paragraph in the README made real.

---

## M6 — Rewards: low-stakes, offline, for resting *(week 8)*

**Goal:** The gamified rewards system, built on DECORATOR + COMPOSITE + STRATEGY.

- [ ] `RewardComponent` (COMPOSITE) — `SlothHat`, `SlothHammock`, `AmbientSoundscape` as leaves; `RewardBundle` as composite
- [ ] `RewardDecor` (DECORATOR) — adds glow/animation at runtime, no subclass explosion
- [ ] `BreakStrategy` (STRATEGY) — `FixedPoints`, `GentleSlothPoints` (default); `StreakPoints` exists but is off by default and documented as "not the point"
- [ ] `AwardRewardCommand` — the reward award is undoable (why not — it's already a Command)
- [ ] `RewardSystemTest` — a bundle of 2 leaves + 1 decorator renders and sums points correctly
- [ ] `BreakStrategyTest` — each strategy returns the documented points
- [ ] README "Rewards" section updated with a screenshot

**Demo:** Complete a break → sloth gets a hat → points increment → no streak pressure, no leaderboard.

---

## M7 — Polish & Native Packaging *(week 9)*

**Goal:** Ship it as something a non-developer can double-click.

- [ ] `jpackage` config in `pom.xml` — `.msi` (Windows), `.dmg` (macOS), `.deb` (Linux)
- [ ] GitHub Releases workflow — on tag push, build all three installers
- [ ] Tray icon (JavaFX `SystemTray`) — shows next scheduled break
- [ ] `~/.slothmode/` directory layout documented in README
- [ ] First-run wizard: pick calendar file, pull model, choose lockout style
- [ ] Full README pass — every section reflects real, shipped behavior

**Demo:** Download a `.dmg`, install, run — no "install Java first" step.

---

## M8 — Stretch / post-v0

Not in the first release. Tracked so contributors know where the seams are.

- [ ] `CalDAVSource` (ConcreteFactory) — Nextcloud, Fastmail
- [ ] `GoogleCalendarSource` — *only* if the user explicitly opts in; must remain local-file-first
- [ ] `LlamaCppModel` real implementation (raw `llama.cpp` bindings)
- [ ] Ambient soundscape generator via a second open-weight audio model
- [ ] "Sloth companion" ASCII/JavaFX animation that reacts to session state
- [ ] Plugin API — third-party `BreakSource` and `BreakStrategy` loaded from a `~/.slothmode/plugins/` dir

---

## Definition of Done (per milestone)

A milestone is done when **all** of these are true:

1. The demoable artifact works on Windows, macOS, and Linux.
2. Every new design pattern has a dedicated unit test that passes in CI.
3. `mvn verify` is green on JDK 21.
4. The README reflects what the user can actually do.
5. No new network call was introduced (except the one-time `ollama pull`).
6. A forker could swap `ModelFactory`, `BreakSource`, `BreakStrategy`, or `MergeStrategy` without touching the session core.

---

## How to contribute

Pick a milestone, open an issue titled `[M<n>] <task>`, and reference this file. Large PRs should be discussed first so we can agree on which seam the change belongs behind — see the "Contributing" section of the README.

The scariest pieces are already scheduled first (M1 overlay, M4 calendar round-trip). If you want to help, start there — that's where a second pair of eyes buys the most.

---

## Status legend

| Milestone | Status | Target |
|---|---|---|
| M0 — Scaffold | [x] | Week 1 |
| M1 — Lockout overlay | [x] | Week 2 |
| M2 — State machine | [x] | Week 3 |
| M3 — Command + Memento | [x] | Week 4 |
| M4 — Calendar layer | [~] | Week 5–6 |
| M5 — Model layer | [~] | Week 7 |
| M6 — Rewards | [~] | Week 8 |
| M7 — Packaging | [ ] | Week 9 |
| M8 — Stretch | [ ] | post-v0 |
