# Nagato Android UI Patch Guide

## Goal
Replace the single-terminal TermuxActivity with a tabbed layout containing:
1. Terminal (original Termux experience)
2. Chat (WebView → http://localhost:8787)
3. TUI (Terminal session auto-running `hermes tui`)

## Files to Modify

### 1. app/build.gradle
Add ViewPager2 and TabLayout dependencies:
```gradle
dependencies {
    implementation "androidx.viewpager2:viewpager2:1.1.0"
    implementation "com.google.android.material:material:1.12.0"
    // ... keep existing deps
}
```

### 2. app/src/main/res/layout/activity_termux.xml
Replace entire content with TabLayout + ViewPager2. See design doc for layout.

### 3. app/src/main/java/com/termux/app/TermuxActivity.java
Major refactoring:
- Add `TabLayout` as class field
- Replace direct `TerminalView` ownership with fragment-based tabs
- In `onCreate()`:
  - inflate ViewPager2 + TabLayout
  - Create adapter with 3 fragments: TerminalFragment, ChatFragment, TuiFragment
  - After bootstrap install, auto-start `hermes agent` and `hermes webui` in bg

### New Files to Create:
- `app/src/main/java/com/termux/app/fragments/ChatFragment.java` — WebView wrapper
- `app/src/main/java/com/termux/app/fragments/TuiFragment.java` — Terminal session with auto-exec
- `app/src/main/java/com/termux/app/adapters/MainPagerAdapter.java` — ViewPager2 adapter

### ChatFragment.java
```java
public class ChatFragment extends Fragment {
    @Override
    public View onCreateView(...) {
        WebView webView = new WebView(requireContext());
        webView.loadUrl("http://localhost:8787");
        return webView;
    }
}
```

### TuiFragment.java
Same pattern as existing terminal session, but:
- Auto-exec `/usr/bin/hermes tui` via `TermuxSession`
- No new-session button in drawer
- Full terminal emulation

## Auto-Start Service
Modify `TermuxInstaller.java` -> `setupBootstrapIfNeeded()`: after bootstrap extraction completes, run:
```bash
pkg install python -y 2>/dev/null || true
pip3 install hermes-agent 2>/dev/null || true
nohup hermes agent &
nohup hermes webui &
```

Or better: create a startup script at `~/.nagato/boot.sh` that is executed by `TermuxService` on session creation.
