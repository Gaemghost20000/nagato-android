# Nagato Agent for Android

A native Android AI agent client powered by [hermes-agent](https://github.com/NousResearch/hermes-agent).

- **Package**: `com.nagato.agent`  
- **Codename**: Ghost  
- **Target**: RedMagic 10 Pro (aarch64, Android 15)  
- **Coexists with**: Official Termux (separate package name, separate app)

## Architecture

```
┌──────────────────────────────────────────┐
│          WebView (fullscreen)            │
│          hermes-agent webui              │
│          (handles projects, chat,        │
│           TUI, file browser, settings)   │
│                                          │
├──────────────────────────────────────────┤
│ [💬 Chat] [⚙️ Debug]                     │ ← Bottom nav
└──────────────────────────────────────────┘
```

### Native Features
- **JS Bridge** — Webui calls native Android (notifications, bubbles, agent restart)
- **Foreground Service** — Persistent notification keeps agents alive on Android 12+
- **Bubble Notifications** — Android 11+ chat heads per project
- **Battery Exemption** — Prevents background killing
- **First-Run Onboarding** — Model selection + project setup

### Backend
Forked from [termux-app](https://github.com/termux/termux-app) — provides the Linux userspace where hermes-agent runs. Bootstrap contains python, git, nodejs, openssl, and the full `apt` package manager.

## Build

See [BUILD_CHECKLIST.md](docs/BUILD_CHECKLIST.md) for step-by-step compilation.

```bash
# 1. Build bootstrap (on VM with Docker)
cd nagato-packages
docker run --rm --privileged -v $(pwd):/home/builder/termux-packages \
    termux/package-builder:latest \
    bash -c "./scripts/build-bootstraps.sh --architectures aarch64"

# 2. Place bootstrap in APK
cp bootstrap-aarch64.zip nagato-android/app/src/main/cpp/

# 3. Build APK
cd nagato-android
./gradlew assembleDebug
```

## Docs

| Document | What |
|----------|------|
| [DESIGN_v2.md](docs/DESIGN_v2.md) | Full architecture + design decisions |
| [NATIVE_BRIDGE.md](docs/NATIVE_BRIDGE.md) | WebView ↔ JavaScript bridge API |
| [BUILD_CHECKLIST.md](docs/BUILD_CHECKLIST.md) | What's done, what's needed |
| [NAGATO_THEME.css](docs/NAGATO_THEME.css) | CSS to rebrand hermes-webui |

## Companion Repo

[nagato-packages](https://github.com/Gaemghost20000/nagato-packages) — Custom Termux bootstrap builder for `com.nagato.agent`
