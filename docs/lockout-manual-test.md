# Lockout Overlay — Manual Test Checklist

> Run this checklist on each platform before closing the M1 milestone.
> If a row fails, M1 is not done.

## Setup

```bash
mvn javafx:run
```

The main window opens. Click **"Take a 10-second break"**. A full-screen overlay appears.

## Checklist

| # | Test | Expected | Windows | macOS | Linux |
|---|---|---|---|---|---|
| 1 | Click "Take a 10-second break" | Full-screen overlay appears, countdown starts at 10 | ☐ | ☐ | ☐ |
| 2 | Try to click behind the overlay | Click has no effect; the overlay stays | ☐ | ☐ | ☐ |
| 3 | Try to type | Keystrokes are ignored (except ESC hatch) | ☐ | ☐ | ☐ |
| 4 | Press `Esc` briefly (under 1s) | Nothing happens; countdown continues | ☐ | ☐ | ☐ |
| 5 | Press `Alt+F4` / `Cmd+Q` | Overlay stays; main app does not quit | ☐ | ☐ | ☐ |
| 6 | Press `F11` (full-screen toggle) | Overlay stays full-screen | ☐ | ☐ | ☐ |
| 7 | Wait 10 seconds | Overlay dismisses itself cleanly | ☐ | ☐ | ☐ |
| 8 | After dismissal, main window | Still shows "SlothMode is idle"; button works again | ☐ | ☐ | ☐ |
| 9 | Escape hatch: trigger again, hold `Esc` for 5 seconds | Overlay dismisses; log shows `Escape hatch fired` | ☐ | ☐ | ☐ |
| 10 | Gentle mode: set `lockout.mode = gentle` in `~/.slothmode/config.toml`, restart, trigger | A small toast appears top-right, does not block input | ☐ | ☐ | ☐ |
| 11 | Gentle mode toast dismisses after 10s | Toast disappears; no log errors | ☐ | ☐ | ☐ |

## Log checks

After running test 9, tail the log:

```bash
tail -20 ~/.slothmode/logs/slothmode.log | grep -i 'lockout'
```

Expected: a line containing `Escape hatch fired` and one with `Enforcing lockout for 10s`.

## Known platform quirks

- **Linux (some tiling WMs):** `setFullScreen(true)` may respect WM full-screen hints that differ from the "always on top" overlay. If the overlay doesn't cover the screen, report it — this is a JavaFX+WM limitation and worth documenting per-WM.
- **macOS:** System Spaces may place the overlay on the *current* Space, not all Spaces. That's acceptable for M1; M7 can revisit if users complain.
- **Windows:** `Alt+Tab` may switch away from the overlay. That's acceptable for M1 (the user explicitly chose to leave the break); a full kiosk lockout is out of scope.
