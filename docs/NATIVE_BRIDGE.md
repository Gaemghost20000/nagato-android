# Nagato Native Bridge Design

## Overview

The hermes-agent webui runs inside a WebView. To make it feel native, we expose
Android APIs (notifications, bubbles, process control) to the webui via JavaScript.

## Architecture

```
 hermes-webui (JavaScript)          Nagato Android (Java)
 ┌─────────────────────┐            ┌──────────────────────────┐
 │                     │            │                          │
 │  Nagato.notify(...) │ ──bridge──→│  NagatoJSBridge.java     │
 │  Nagato.ping()      │ ←─bridge──│  @JavascriptInterface    │
 │  Nagato.getDevice() │            │                          │
 │  Nagato.onReady()   │            │                          │
 │                     │            │  NagatoForegroundService │
 │                     │            │   ├── agent processes    │
 │                     │            │   ├── persistent notif   │
 │                     │            │   └── bubble API calls   │
 └─────────────────────┘            └──────────────────────────┘
```

## Available JS Bridge Methods

The bridge is exposed as `window.Nagato` in the webui:

| Method | Params | What It Does |
|--------|--------|-------------|
| `Nagato.notifyMessage(title, body, projectId)` | 3 strings | Shows native Android notification + optional bubble |
| `Nagato.restartAgent(projectId)` | string | Kills and restarts agent for this project |
| `Nagato.killAgent(projectId)` | string | Kills agent process for this project |
| `Nagato.getDeviceInfo()` | none | Returns JSON: model, Android version, SDK, app version |
| `Nagato.getTheme()` | none | Returns "dark" or "light" |
| `Nagato.onAgentReady()` | none | Signals the app that webui finished loading |
| `Nagato.ping()` | none | Returns "pong" (connection test) |

## WebUI Detection Pattern

The webui checks if the bridge exists:

```javascript
if (typeof Nagato !== 'undefined' && Nagato.ping() === 'pong') {
    console.log('[Nagato] Native bridge active');
    // Customize UI for native: hide redundant nav, show mobile optimizations
    Nagato.onAgentReady();
} else {
    console.log('[Nagato] Running in browser — standard mode');
}
```

## Theme Injection

The ChatFragment injects Nagato brand CSS variables on page load:

```css
:root {
    --nagato-bg: #0D0B14;
    --nagato-surface: #16121F;
    --nagato-primary: #7E57C2;
    --nagato-accent: #9D7FE8;
    --nagato-text: #E8E0F0;
}
```

The webui can opt-in to these variables for native-branded theming.

## Foreground Service Flow

```
App opens → NagatoForegroundService.startForeground()
         → Persistent notification: "Nagato Agent • 1 project active"
         → Agent process spawned for each project:
             hermes agent --port 8787 &
             hermes webui &
         → WebView loads http://localhost:8787
         → WebUI calls Nagato.onAgentReady()
         → App hides loading spinner

User leaves app → Service keeps agent alive
Android tries to kill → START_STICKY restarts service
User taps notification → Returns to Chat tab

App force-closed → Service.onDestroy() → Process.destroyForcibly() for all agents
```

## Integration Checklist

- [x] NagatoJSBridge.java written
- [x] ChatFragmentV2.java wired with bridge + theme injection
- [x] NagatoForegroundService.java handles agent lifecycle
- [x] NagatoActivityV2.java starts foreground service on launch
- [ ] AndroidManifest.xml: declare service + permissions
- [ ] ic_nagato.png/vector drawable for notification icon
- [ ] Build test on device
