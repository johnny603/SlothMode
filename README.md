# SlothMode 🦥

> Let the sloth take the wheel. You've earned a nap.

**SlothMode is a local-first AI productivity companion that enforces breaks for you — so you don't have to.**

It tracks your focus sessions, locks you out when you've been going too hard, and saves your work before the break hits. Breaks live in **your** calendar (a local `.ics` file), not a server. Rewards are low-stakes, offline, and earned for *resting* — not for grinding.

Built as an open-source response to the "Slow and Steady" build theme: open-weight models, local inference, and a calendar that belongs to you.

---

## Why open innovation matters here

SlothMode is designed so that **the open pieces are what make it work**, not a nice-to-have:

- **Runs on a laptop with no internet.** The model is a small open-weight model (via [Ollama](https://ollama.com) or `llama.cpp`) running locally. Break timers, save-work snapshots, and calendar sync all work offline.
- **Your data stays on your machine.** Session logs, saved-work mementos, and break schedules never touch a server you don't control. Breaks are read from and written to a **local `.ics` file** — your calendar is the system of record.
- **Swap the model, swap the calendar backend.** `ModelFactory` and `BreakSource` are pluggable: Ollama ↔ llama.cpp, `.ics` ↔ CalDAV ↔ JSON. Fork it and change one class.
- **Costs nothing to run.** No subscription, no API bill. Set it up once.

> A closed productivity app would phone home, charge for "premium breaks," and own your schedule. SlothMode does the opposite.

---

## What it does

| Feature | Behavior |
|---|---|
| **Focus sessions** | You start a session; SlothMode tracks active time locally. |
| **Scheduled lockouts** | After a configurable stretch, the session becomes unusable for a set period. A full-screen overlay blocks work — you *cannot* dismiss it early. |
| **Save-work safeguard** | Before the lockout, a local check snapshots open documents, prompts to save, and logs your context so you can resume exactly where you left off. |
| **Calendar-synced breaks** | Breaks are read from a local `.ics` file. Manual breaks added in-app are **written back to your calendar**. |
| **Gamified rest rewards** | You earn points for *completing breaks*, not for working more. Points unlock cosmetic sloth upgrades — hats, hammocks, ambient soundscapes. No streaks, no leaderboards, nothing to lose. |

---

## Status

**M0 complete — the scaffold runs.** SlothMode launches, reads its local config, writes logs, and shows its idle window. The lockout overlay, session state machine, calendar layer, model integration, and rewards system are next; the roadmap tracks them milestone by milestone.

See [ROADMAP.md](ROADMAP.md) for the build order. M0 is closed; M1 (the lockout overlay) is the next milestone.

---

## Getting started

**Requires:** JDK 21 ([Temurin](https://adoptium.net/) recommended) and Maven 3.9+.

Verify your toolchain:

```bash
java -version   # should print 21
mvn -version    # should print 3.9.x
```

Clone and run:

```bash
git clone https://github.com/johnny603/SlothMode.git
cd SlothMode
mvn javafx:run
```

You should see a window with a sloth 🦥 and the line **"SlothMode is idle"**. The first run creates `~/.slothmode/` with a default `config.toml` and a log file.

Run the tests:

```bash
mvn verify
```

### Where SlothMode stores data

On first run, SlothMode creates:

```
~/.slothmode/
├── config.toml      ← hand-editable
├── logs/            ← local, never uploaded
├── data/            ← session history (reserved for M3)
└── calendar/        ← your .ics lives here by default
```

Delete `~/.slothmode/` to reset. Nothing leaves your machine.

### The config file

`~/.slothmode/config.toml` looks like this after first run:

```toml
calendar.path = /Users/you/.slothmode/calendar/slothmode.ics
lockout.mode = fullscreen
model.name = llama3.2:3b
```

- `calendar.path` — where your breaks live. A local `.ics` file; your calendar app can open it.
- `lockout.mode` — `fullscreen` (undismissable overlay) or `gentle` (toast nudge).
- `model.name` — any open-weight model you've pulled with `ollama pull`.

> **Note on the model:** M0 doesn't call the model yet. The Ollama integration lands in M5; until then the field is read but unused.

---

## How it works (architecture)

SlothMode is deliberately built on the classic [Gang of Four design patterns](https://en.wikipedia.org/wiki/Design_Patterns) so the code stays forkable and testable.

```
┌──────────────────────────────────────────────────────────────────────┐
│                         SLOTHMODE APPLICATION                        │
│                                                                      │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐     │
│  │  FACADE      │   │  SINGLETON   │   │  ABSTRACT FACTORY    │     │
│  │ SlothModeApp │──▶│ ConfigMgr    │   │ ModelFactory         │     │
│  └──────┬───────┘   └──────────────┘   └──────────┬───────────┘     │
│         │                                          │                 │
│         ▼                                          ▼                 │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐     │
│  │  MEDIATOR    │   │  OBSERVER    │   │  BRIDGE              │     │
│  │ SessionHub   │◀─▶│ SessionSubject│──▶│ LockoutAbstraction  │     │
│  └──────┬───────┘   └──────┬───────┘   └──────────┬───────────┘     │
│         │                  │                       │                 │
│         ▼                  ▼                       ▼                 │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐     │
│  │  STATE       │   │  COMMAND     │   │  STRATEGY            │     │
│  │ SessionState │   │ CommandQueue │   │ BreakStrategy        │     │
│  └──────────────┘   └──────┬───────┘   │ MergeStrategy        │     │
│                            │           └──────────────────────┘     │
│                            ▼                                         │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              BREAK SOURCE LAYER                              │   │
│  │  ┌──────────────────┐   ┌──────────────────┐                │   │
│  │  │ ABSTRACT FACTORY │   │  OBSERVER        │                │   │
│  │  │ BreakSource      │──▶│ BreakRepository  │                │   │
│  │  └────────┬─────────┘   └──────────────────┘                │   │
│  │           │                                                  │   │
│  │    ┌──────┴──────┬──────────────┐                           │   │
│  │    ▼             ▼              ▼                           │   │
│  │ CalendarSource ManualSource  HybridSource                    │   │
│  │    │                                                          │   │
│  │    ▼                                                          │   │
│  │  Local .ics  ◀── read + write (COMMAND + MEMENTO)            │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐     │
│  │  DECORATOR   │   │  COMPOSITE   │   │  ITERATOR + VISITOR  │     │
│  │ RewardDecor  │   │ SlothTree    │   │ EventTraversal       │     │
│  └──────────────┘   └──────────────┘   └──────────────────────┘     │
└──────────────────────────────────────────────────────────────────────┘
```

### Patterns in use

| Pattern | Where | Why |
|---|---|---|
| **Facade** | `SlothModeApp` | One entry point; subsystem internals stay swappable |
| **Singleton** | `ConfigManager` | One local config, no server |
| **Abstract Factory** | `ModelFactory`, `BreakSource` | Swap model or calendar backend without touching core |
| **Bridge** | `LockoutAbstraction` | Fullscreen lockout vs. gentle nudge vary independently |
| **Mediator** | `SessionHub` | Widgets never talk directly — easy to replace one |
| **Observer** | `SessionSubject`, `BreakRepository` | Add a new offline UI without editing the core |
| **State** | `SessionState` | Idle/Focus/Lockout/Reward as explicit objects |
| **Command + Memento** | Command history | Undoable manual break adds; file snapshots for safe rollback |
| **Iterator + Visitor** | Event traversal + analysis | Add a new "morning digest" without touching event classes |
| **Strategy** | `BreakStrategy`, `MergeStrategy` | Reward math and calendar-conflict policy are pluggable |
| **Decorator + Composite** | Reward system | Glow/animation at runtime; single vs. bundle of rewards |

### The manual break round trip

Adding a break by hand writes it to your local calendar and re-plans future lockouts:

```
UI → AddBreakCommand.Execute()
   → CalendarSource.CreateMemento()   (snapshot .ics)
   → CalendarSource.AddBreak(b)       (write VEVENT)
   → BreakRepository.Invalidate()
   → SessionSubject.Notify()
   → SessionHub.Replan()
   → UI refresh
```

If the write fails, the Memento restores the `.ics` byte-for-byte — observers are never notified about a break that doesn't exist. Undo works the same way in reverse.

---

## Design decisions

- **Local `.ics`, not a database.** The calendar is the API. Your breaks survive a SlothMode uninstall, and they work with whatever calendar app you already use.
- **Manual breaks are *proposed* entries.** They're written into the calendar, where you can edit or delete them. `CalendarWins` is the default merge strategy; forks can change it to `ManualWins` or `AskUser`.
- **Rewards for resting, not grinding.** Points are earned by *completing* breaks. No streaks, no leaderboards — the game rewards rest.
- **Memento before mutation.** Every calendar write snapshots first. Failed writes and undos restore the file exactly.

---

## Contributing

Fork it. The pluggable seams are `BreakSource` (calendar backend), `ModelFactory` (model backend), `BreakStrategy` (reward math), and `MergeStrategy` (calendar conflict policy). If you want a different behavior, implement one of those — you shouldn't need to touch the session core.

Open an issue before a large PR so we can talk about which seam it belongs behind.

The [ROADMAP](ROADMAP.md) lists every milestone with a demoable artifact. M0 is closed; M1 (lockout overlay) is the riskiest piece and the natural next place to help.

---

## License

MIT — see [LICENSE](LICENSE).

---

## Acknowledgments

- Built for the **Slow and Steady** build week, honoring [International Sloth Day](https://en.wikipedia.org/wiki/International_Sloth_Day) (Oct 20).
- Architecture inspired by *Design Patterns: Elements of Reusable Object-Oriented Software* (Gamma, Helm, Johnson, Vlissides).
- Open-weight models via [Ollama](https://ollama.com) and [llama.cpp](https://github.com/ggerganov/llama.cpp).
