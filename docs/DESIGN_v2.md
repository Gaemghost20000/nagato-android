# Nagato Agent App — Revised Design Document v2.0
## Status: Chat-First, Project-Based, Bubble Notifications, Landscape Multi-Pane

---

## 1. Product Identity

| Item | Value |
|------|-------|
| **Display Name** | Nagato |
| **Package** | `com.nagato.agent` |
| **Codename** | Ghost |
| **Identity** | Independent of Termux — not a "terminal app with extras", but a **native Android AI agent client** |
| **Tagline** | "Your agents, everywhere." |

> The app does not expose Termux to the user. The terminal is a debug/developer feature. The primary experience is a **multi-project chat interface** native to Android, powered by hermes-agent.

---

## 2. Mental Model: From Tabs to Projects

The app is **not** Terminal / Chat / TUI.

It is **Projects**: each project is an independent hermes-agent instance with:
- **Chat** — WebView chatting with that agent
- **TUI** — `hermes tui` running for that project (connects to the same agent)
- **Files** — Workspace files for this project
- **Settings** — Model config, API keys, plugins for this project
- **Logs** — Verbose debug output from this agent

These are **not tabs within the app**. They are **contexts within a project**, surfaced through the project's UI.

---

## 3. Navigation: Bottom Navigation Bar

```
┌──────────────────────────────────────────┐
│                                          │
│         Main Content Area                │
│   (varies per destination/content)       │
│                                          │
│                                          │
├──────────────────────────────────────────┤
│ [💬 Home ] [⚙️ TUI ] [🖥️ Debug] [👤 Me]  │
└──────────────────────────────────────────┘
```

| Nav Item | What It Is |
|----------|-----------|
| **Home** | Project list + active project chat (default). This is the hero experience. |
| **TUI** | Quick-access to any project's hermes tui session. Full-screen terminal, immersive. |
| **Debug** | System-wide logs, verbose output, agent status, crash info. For developers. |
| **Me** | Profile, settings, theme, about, agent management (create/delete/switch projects). |

Bottom nav auto-hides when keyboard opens. Swipe gestures within Home show project drawer.

---

## 4. Home Screen: Project-Centric Chat

### Portrait Mode
```
┌──────────────────────────────────────────┐
│ ≡ Projects  Nagato          🔔 +         │  ← Top app bar
├──────────────────────────────────────────┤
│ [P1] [P2] [P3] [+New]                    │  ← Horizontal project strip
├──────────────────────────────────────────┤
│                                          │
│         Chat WebView                     │
│         (hermes-agent webui)             │
│         Active project loaded            │
│                                          │
│                                          │
└──────────────────────────────────────────┘
│ [💬] [⚙️] [🖥️] [👤]                     │  ← Bottom nav
└──────────────────────────────────────────┘
```

- **Top bar**: "Projects" drawer toggle, app name, notifications, add project
- **Project strip**: Horizontally scrollable. Tapping switches the active project's chat into the WebView below.
- **WebView**: Loads `http://localhost:8787` for the currently selected project. Each project runs its own agent process on a different port? OR: a single agent instance with project switching via URL params.
- **Bottom nav**: As described above.

### Landscape Mode (Phone + Tablet)
```
┌──────────────────┬──────────────────┬──────────────┐
│                  │                  │              │
│  Project List    │  Chat WebView    │ File Browser │
│  (selectable)    │  (active project) │ (active prj) │
│                  │                  │              │
│ [P1]             │                  │ src/         │
│ [P2]             │                  │ config.yaml  │
│ [P3] NEW         │                  │ .hermes/     │
│                   │                  │              │
├──────────────────┴──────────────────┴──────────────┤
│ [💬 Home] [⚙️ TUI] [🖥️ Debug] [👤 Me]               │
└──────────────────────────────────────────────────────┘
```

- **Three-pane**: Project List (left) | Chat (center) | Files/Workspace (right)
- **User-configurable pane positions**: Settings allow reordering or hiding panes
- **On tablet**: Always three-pane. On landscape phone: Three-pane or two-pane depending on screen width breakpoint.

---

## 5. TUI Screen

```
┌──────────────────────────────────────────┐
│ ≡ Projects  TUI              ≡ Select    │
├──────────────────────────────────────────┤
│                                          │
│                                          │
│         TerminalView                     │
│         Running `hermes tui`             │
│         for Project P1                   │
│                                          │
│                                          │
│                                          │
└──────────────────────────────────────────┘
│ [Extra Keys Toolbar]                     │
└──────────────────────────────────────────┘
│ [💬] [⚙️] [🖥️] [👤]                     │
└──────────────────────────────────────────┘
```

- Title bar shows which project the TUI belongs to
- "Select" button opens a dialog to switch TUI between projects
- Bottom nav still visible (auto-hides when typing)
- Returns to Home screen with Back button

---

## 6. Debug Screen

```
┌──────────────────────────────────────────┐
│ ≡  Debug Logs              [Share] [🗑️] │
├──────────────────────────────────────────┤
│ Agent P1: ● running | P2: ○ stopped      │
│ [Agent Logs] [System Logs] [Boot Logs]   │
├──────────────────────────────────────────┤
│                                          │
│ verbose: Starting agent P1...            │
│ debug: Loaded config /data/...           │
│ info: Connected to Ollama                │
│ trace: Received message: hello           │
│                                          │
│                                          │
└──────────────────────────────────────────┘
│ [💬] [⚙️] [🖥️] [👤]                     │
└──────────────────────────────────────────┘
```

- **Agent status bar**: Running/stopped per project with kill/restart buttons
- **Log tabs**: Agent verbose | System (Android logcat) | Bootstrap/first-run logs
- **Log view**: Colored by level (INFO green, DEBUG blue, ERROR red, TRACE gray)
- **Actions**: Share logs, clear logs, filter by project

This is the replacement for "Terminal tab" — it's not a shell for the user, it's a diagnostic dashboard.

---

## 7. Notification + Bubble System

### Persistent Foreground Notification
Required to keep agent alive on Android 12+ (phantom process killing).

```
┌────────────────────────┐
│ 🔗  Nagato Agent       │
│ 3 projects active       │
│ Tap to open            │
└────────────────────────┘
```
- Shows number of active projects
- Cannot be swiped away
- Tapping opens Home screen
- Long-press → "Stop service" (kills all agents)

### Chat Bubble Notifications
Per-project floating bubbles (like Messenger):

```
┌────────────────────────────────┐
│              ●                 │  ← Bubble floats over other apps
│            ╱  ╲                │
│           ╱ ✉️ ╲               │  (shows project avatar + unread count)
          ╰──────╯

Tap to expand:
┌──────────────┐
│ Proj:Website │  ← Mini chat window
│ ──────────── │
│ Bot: Done.   │
│              │
│ [Type here…] │
└──────────────┘
```

- Appears when a project receives a message and user is in another app
- Swipe bubble to dismiss for this conversation
- Auto-hides when that project's agent is not active
- Optional: "Do not disturb" mode in settings

### Android Bubbles API
- Uses Android 11+ notification bubbles (`BubbleMetadata`)
- Fallback: Custom floating window service using `SYSTEM_ALERT_WINDOW` permission on older Android (or use third-party library like Floating-Bubble-View)
- On Android 15 (RedMagic): Bubbles API is fully supported

---

## 8. Project Lifecycle

### Creating a Project
```
Home → Me → New Project
┌────────────────────────┐
│ Name:  [__________]   │
│ Model: [Ollama ▼]     │
│ Path:  [~/projects/   │
│         __________]    │
│ Plugins: [x] web      │
│          [x] github    │
│                        │
│ [Create]               │
└────────────────────────┘
```
- Spawns a new hermes-agent instance with isolated config
- Appears in project strip
- Auto-starts agent in background

### Switching Projects
- Tap project in strip → switches WebView to that project's chat
- Or tap notification bubble → opens that project's chat

### Deleting a Project
- Long-press project in strip → "Delete / Archive"
- Archives logs and config; optionally deletes workspace files

---

## 9. Auto-Start + Service Architecture

```
NagatoForegroundService (always running when any agents active)
    ├── Manages agent process pool
    ├── Each project spawns:
    │   ├── hermes agent --project <name> --port <unique>
    │   └── hermes webui --project <name> --port <unique>
    ├── Keeps WebSocket connections alive
    └── Reports status to UI via binder

NagatoBubbleService (runs when bubbles visible)
    └── Creates/destroys floating windows
```

### Process Model
- **Main process**: Android UI (Home, TUI, Debug, Me)
- **Termux-service process**: Terminal sessions (carried over from termux-app)
- **Agent processes**: One per project (hermes-agent backend)
- **WebUI processes**: One per project (serves WebView content on unique ports)

Each project agent listens on its own port (e.g., P1 = :8787, P2 = :8788, P3 = :8789) OR a single agent handles multi-project routing (requires hermes-agent support).

---

## 10. Color Scheme: Dark Mode Purple (Gaem.moe rebrand)

| Token | Hex | Usage |
|-------|-----|-------|
| **Background** | `#0D0B14` | Main app background, nav bar |
| **Surface** | `#16121F` | Cards, drawers, dialogs |
| **Surface Variant** | `#211B2E` | Elevated cards, input fields |
| **Primary** | `#7E57C2` | Active nav item, project accent |
| **Primary Variant** | `#9D7FE8` | Hover states, ripple |
| **Secondary** | `#00BCD4` | Links, interactive elements |
| **Accent** | `#FF6B6B` | Errors, notifications, important actions |
| **Text Primary** | `#E8E0F0` | Headings, main text |
| **Text Secondary** | `#9E94AD` | Captions, descriptions |
| **Divider** | `#2A2438` | Separators, borders |
| **Terminal BG** | `#0D0B14` | Terminal and TUI background (matches app) |
| **Terminal FG** | `#B8B0C8` | Terminal text |

---

## 11. Termux Relationship (Implementation Detail)

To the user, Termux is **invisible**. Implementation-wise:

| Component | How Termux Helps |
|-----------|----------------|
| Linux userspace | Bootstrap provides bash, coreutils, apt, python |
| Agent execution | `hermes agent` runs inside Termux $PREFIX |
| Package management | `pkg install` works for additional tools |
| Terminal emulation | `TerminalView` from termux-app reused for TUI and Debug |

The Termux fork is **only** for:
1. Correct `$PREFIX` (`/data/data/com.nagato.agent/files/usr`)
2. Bootstrap extraction on first run
3. `TerminalView` widget for terminal rendering
4. Foreground service model (adapted to Nagato needs)

---

## 12. Windows Co-App Parallels

The Windows "Nagato Desktop" mirrors this architecture:
- **Home**: System tray icon opens project selector + chat window
- **TUI**: `hermes tui` in Windows Terminal (or embedded conpty)
- **Debug**: Dedicated log window with agent status
- **Bubbles**: Native Windows notification toasts (or custom overlay)
- **Landscape**: Resizable panes (project list | chat | file tree)

Shared: project configs, themes, session sync.

---

## 13. Remaining Work Breakdown

### Critical Path (Blocking first APK)
- [ ] Bootstrap build with `com.nagato.agent` prefix (or Option C: minimal bootstrap)
- [ ] NagatoActivity.java with bottom nav + ViewPager2
- [ ] HomeFragment (project strip + WebView for active project)
- [ ] TuiFragment (TerminalView for selected project)
- [ ] DebugFragment (logcat-style viewer, agent status)
- [ ] MeFragment (settings, project management)
- [ ] NagatoForegroundService (keeps agents alive)
- [ ] Auto-start hermes-agent per project on bootstrap

### Nice-to-Have (Post-MVP)
- [ ] Notification bubbles (Android 11+ Bubbles API)
- [ ] Landscape three-pane layout
- [ ] Per-project file browser in right pane
- [ ] Persistent notification styling
- [ ] Custom themes / color override
- [ ] Import/export projects
- [ ] Plugin management UI

---

## 14. Changed Files from Termux Fork

### Completely New
- `NagatoActivity.java` (replaces TermuxActivity)
- `HomeFragment.java`, `TuiFragment.java`, `DebugFragment.java`, `MeFragment.java`
- `NagatoForegroundService.java` (adapted from TermuxService)
- `ProjectManager.java` (manages multi-project agent pool)
- `NagatoBubbleService.java` (floating chat bubbles)
- All layout XMLs (new UI design)

### Heavily Modified from Termux
- `TermuxService.java` → `NagatoForegroundService.java`
- `TermuxInstaller.java` (add post-bootstrap auto-setup)
- `app/build.gradle` (new deps, new package, bootstrap handling)
- `AndroidManifest.xml` (new permissions, bubbles, foreground service)

### Mostly Unchanged from Termux
- `TerminalView.java` (terminal emulator widget)
- `terminal-emulator/` library
- `termux-shared/` (constants updated for new package name)
- Bootstrap extraction logic

---

## 15. Decision Log

| Decision | Chosen | Rationale |
|----------|--------|-----------|
| Default screen | **Chat (Home)** | Primary use case is interacting with agents, not terminal |
| Terminal role | **Debug only** | Relegated — developers need it, users don't see it by default |
| TUI behavior | **Dedicated, project-bound** | Each project's TUI is a separate session. TUI nav item is a quick-launcher |
| Notifications | **Persistent foreground + chat bubbles** | Required for Android 12+ process survival; bubbles match messaging UX |
| Landscape | **Three-pane** | Project list \| Chat \| Files. Configurable positions |
| Color scheme | **Dark purple (#0D0B14 base)** | Matches gaem.moe brand; easy on OLED eyes |
| Multi-project | **Yes, core to design** | Each project = isolated agent instance |
| Termux visibility | **Invisible to users** | It's the backend, not the product |
