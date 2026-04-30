# Nagato Agent — Full Architecture Plan

**Project**: Nagato Agent (Ghost)  
**Target Package**: `com.nagato.agent`  
**Target Device**: RedMagic 10 Pro (aarch64, Android 15)  
**Repos**: `Gaemghost20000/nagato-android` + `Gaemghost20000/nagato-packages`

---

## 1. Why This Architecture?

### The Coexistence Problem
The RedMagic 10 Pro already runs Termux (package `com.termux`). Any fork keeping that name cannot coexist. Changing the package name is mandatory.

### Why Not Native Python Embedding?
hermes-agent depends on:
- `pydantic` (Rust/C extension for v2)
- `cryptography` (C extension)
- `httpx`, `openai`, `anthropic`, `aiohttp` (pure Python but need venv infra)

Cross-compiling C/Rust extensions to Android with the NDK is an enormous rabbit hole. Termux's Docker-based build system already solves this for thousands of packages.

### Why Not Just Keep `com.termux`?
Conflicts with the existing Termux install. You'd have to uninstall, losing your existing environment. Not acceptable.

### The Winning Approach: Custom-Package Termux Fork
We rebuild the Termux bootstrap with `/data/data/com.nagato.agent/files/usr` as the prefix. Every package in the bootstrap gets this path baked in at compile time. The app installs, extracts, and runs completely independently of official Termux.

---

## 2. Repository Architecture

```
Gaemghost20000/nagato-android/
├── termux-shared/          # Constants (com.termux → com.nagato.agent)
├── terminal-emulator/      # Unchanged from upstream
├── terminal-view/          # Unchanged from upstream
├── app/
│   ├── build.gradle        # Disable bootstrap DL, point to our releases
│   ├── src/main/
│   │   ├── java/com/nagato/agent/app/
│   │   │   ├── NagatoActivity.java      # Renamed from TermuxActivity
│   │   │   ├── fragments/
│   │   │   │   ├── TerminalFragment.java # Existing terminal experience
│   │   │   │   ├── ChatFragment.java     # WebView → localhost:8787
│   │   │   │   └── TuiFragment.java      # Terminal session → hermes tui
│   │   │   └── adapter/
│   │   │       └── MainPagerAdapter.java # ViewPager2 adapter
│   │   └── res/layout/
│   │       ├── activity_nagato.xml       # TabLayout + ViewPager2
│   │       ├── fragment_chat.xml
│   │       └── fragment_tui.xml
│   └── cpp/
│       └── bootstrap-aarch64.zip         # Custom bootstrap artifact
└── tools/
    ├── setup-nagato-fork.sh
    └── build-nagato-bootstrap.sh

Gaemghost20000/nagato-packages/
├── scripts/
│   ├── properties.sh          # Custom package name + paths
│   └── build-bootstraps.sh    # Build from source (if not present, get from upstream)
└── packages/
    ├── python/build.sh        # Standard Termux python package
    ├── python-pip/build.sh    # pip
    ├── git/build.sh           # git
    ├── nodejs/build.sh        # node (for hermes-agent webui)
    ├── curl/build.sh          # curl
    └── openssl/build.sh       # OpenSSL
```

---

## 3. Package Name Change — The Mechanics

### A. `properties.sh` (nagato-packages)

These are the ONLY safe variables to change (per upstream docs):

```bash
# Safe to modify — line numbers reference latest master
TERMUX_APP__PACKAGE_NAME="com.nagato.agent"
TERMUX_APP__DATA_DIR="/data/data/com.nagato.agent"
TERMUX__ROOTFS="/data/data/com.nagato.agent/files"
TERMUX__PREFIX="/data/data/com.nagato.agent/files/usr"
TERMUX_ANDROID_HOME="/data/data/com.nagato.agent/files/home"
TERMUX__UNAME="nagato"
TERMUX__NAME="Nagato"
TERMUX__LNAME="nagato"
```

**Critical**: `TERMUX_REPO_BASE_URL` etc. must remain pointing to official Termux repos for `generate-bootstraps.sh`. If you want `apt` to work with your custom prefix, you must either:
- Use `build-bootstraps.sh` (builds everything from source)
- OR set up your own package mirror

### B. `TermuxConstants.java` (nagato-android)

Mass replace in `termux-shared/src/main/java/com/nagato/agent/shared/termux/TermuxConstants.java`:

```java
public static final String TERMUX_PACKAGE_NAME = "com.nagato.agent";

public static final String TERMUX_INTERNAL_PRIVATE_APP_DATA_DIR_PATH = "/data/data/com.nagato.agent";
public static final String TERMUX_FILES_DIR_PATH = "/data/data/com.nagato.agent/files";
public static final String TERMUX_PREFIX_DIR_PATH = "/data/data/com.nagato.agent/files/usr";
```

This file has ~100 references to `com.termux`. Automation script handles this.

### C. `AndroidManifest.xml`

```xml
android:sharedUserId="com.nagato.agent"
```

### D. `app/build.gradle`

```groovy
android {
    defaultConfig {
        applicationId "com.nagato.agent"
        // ... rest unchanged
    }
}
```

---

## 4. Bootstrap Build Options

### Option A: `generate-bootstraps.sh` + Local Apt Repo (RECOMMENDED for speed)

1. Build base packages with custom prefix via Docker
2. Host a local apt repo on the build machine
3. Run `generate-bootstraps.sh -r http://localhost:8080/...`
4. This pulls pre-built `.deb` files from your local repo

**Pros**: Reuses official `.deb` recipes, just needs rebuild for new prefix  
**Cons**: Requires local apt repo setup  
**Time**: ~2 hours for ~35 base packages

### Option B: `build-bootstraps.sh` (More Self-Contained)

Builds every package from source within the bootstrap itself.

**Pros**: Zero dependency on apt repos  
**Cons**: Reportedly unreliable in the community, may break on some packages  
**Time**: ~4-8 hours

### Option C: Hybrid — Base Bootstrap + Post-Install Script (FASTEST to implement)

1. Build a minimal bootstrap with just bash, coreutils, apt, python
2. On first app launch, run auto-setup:
   ```bash
   pkg update
   pkg install -y python python-pip git nodejs curl openssl
   pip install hermes-agent[web]
   ```

**Pros**: Bootstrap stays small (~60MB), everything else installs via apt  
**Cons**: First launch requires internet, ~2-3 min setup  
**Time**: Bootstrap builds in ~30 min

### Recommended: Option C for MVP, migrate to A later

---

## 5. Android UI Architecture

### Layout: `activity_nagato.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.google.android.material.tabs.TabLayout
        android:id="@+id/tab_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:tabMode="fixed"
        app:tabGravity="fill" />

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/view_pager"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

</LinearLayout>
```

### Fragments

#### TerminalFragment
- Wraps existing `DrawerLayout` + `TerminalView`
- Manages session lifecycle via `TermuxService`
- Left swipe opens session drawer (unchanged behavior)

#### ChatFragment
```java
public class ChatFragment extends Fragment {
    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        webView = new WebView(requireContext());
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://localhost:8787");
        return webView;
    }
}
```

#### TuiFragment
```java
public class TuiFragment extends Fragment {
    private TerminalView terminalView;
    private TermuxSession tuiSession;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        terminalView = view.findViewById(R.id.terminal_view);
        // Auto-create session running: hermes tui
        tuiSession = createSession("/data/data/com.nagato.agent/files/usr/bin/hermes tui");
        terminalView.attachSession(tuiSession);
    }
}
```

### Auto-Start Service

In `NagatoActivity.onServiceConnected()`, after bootstrap check:

```java
if (bootstrapJustInstalled) {
    runInBackground(() -> {
        String[] cmds = {
            "pkg install -y python python-pip git",
            "pip install hermes-agent[web]",
            "nohup hermes agent &",
            "nohup hermes webui &"
        };
        for (String cmd : cmds) execute(cmd);
    });
}
```

Better: Put a `start-hermes.sh` script in `~/.nagato/` that `TermuxService` runs on session creation.

---

## 6. Hermes-Agent Integration Strategy

### Approach: "Lazy Install" (fastest path)

The bootstrap contains:
- `python` + `pip` (from apt repo)
- `hermes-agent` NOT pre-installed

On first launch:
1. Bootstrap extracts (Termux standard)
2. Auto-setup script runs:
   ```bash
   #!/data/data/com.nagato.agent/files/usr/bin/bash
   export PATH=/data/data/com.nagato.agent/files/usr/bin:$PATH
   pip install --upgrade pip
   pip install hermes-agent[web]
   hermes agent &
   hermes webui &
   ```
3. WebView shows a "Setting up Nagato..." screen until `localhost:8787` responds

### Size Estimate

| Component | Size |
|-----------|------|
| Termux bootstrap base | ~25MB |
| Python (bootstrap-installed) | ~40MB |
| pip + hermes-agent | ~100MB |
| **Bootstrap zip** | ~65MB |
| **APK** (single-arch, compressed) | ~80MB |
| **Installed ($PREFIX)** | ~150MB |

Total user download: ~80MB. Reasonable.

---

## 7. Build Pipeline

### Phase 1: Configure (phone or VM)
```bash
# Clone repos
git clone https://github.com/Gaemghost20000/nagato-android.git
git clone https://github.com/Gaemghost20000/nagato-packages.git

# Run setup script
cd nagato-android
bash tools/setup-nagato-fork.sh  # Patches all package name refs
cd ../nagato-packages
bash tools/setup-nagato-packages.sh  # Patches properties.sh
```

### Phase 2: Build Bootstrap (VM or CI)
```bash
cd nagato-packages
docker run --rm -it   -v $(pwd):/home/builder/termux-packages   termux/package-builder:latest   bash -c "cd termux-packages && ./scripts/build-bootstraps.sh --architectures aarch64"

# Copy output
cp bootstrap-aarch64.zip ../nagato-android/app/src/main/cpp/
```

### Phase 3: Build APK (anywhere with Android SDK)
```bash
cd nagato-android
./gradlew assembleDebug  # or assembleRelease
# Output: app/build/outputs/apk/debug/nagato-agent-aarch64-debug.apk
```

### Phase 4: Install
```bash
adb install -r nagato-agent-aarch64-debug.apk
```

---

## 8. Testing Strategy

| Test | Method |
|------|--------|
| Coexistence with Termux | Install alongside `com.termux`, verify no conflicts |
| Bootstrap extraction | Fresh install, verify `$PREFIX` created at correct path |
| Python availability | Run `python --version` in terminal tab |
| hermes-agent install | Observe auto-setup progress, check `pip list` |
| Chat tab | Verify WebView loads `localhost:8787` |
| TUI tab | Verify `hermes tui` launches in dedicated session |
| Apt upgrades | Run `pkg upgrade`, verify updates download |

---

## 9. Windows Co-App (Post-MVP)

Same tabbed concept, Windows-native:
- **Terminal**: Windows Terminal control or conpty
- **Chat**: WebView2 → `http://localhost:8787`
- **TUI**: Launch `hermes tui` in embedded terminal

Install via `msix` or portable executable. Shared config sync via the agent's settings API.

---

## 10. Timeline Estimate

| Phase | Human Time | Compute Time | Delegatable? |
|-------|-----------|--------------|-------------|
| Setup repos + configure package names | 30 min | — | No |
| Build bootstrap (Option C: minimal) | 10 min | 30 min | **Yes (Nagato VM)** |
| Android UI refactoring (TabLayout + WebView) | 2-3 hours | — | No (code review) |
| Auto-start + hermes-agent wiring | 1 hour | — | No |
| Build APK + test on device | 30 min | 5 min | No |
| **Total** | **~5 hours** | **~35 min** | **Bootstrap only** |

The "weeks" perception comes from Option A (full rebuild of all packages). Option C cuts this dramatically.

---

## 11. Open Questions

1. Should the auto-setup script be silent or show progress in the Chat tab?
2. Should we bundle a default `hermes-agent` config (models, API keys placeholder)?
3. Should the TUI tab auto-restart `hermes tui` if it exits?
4. Should we support `TERMUX_PACKAGE_MANAGER=apt` or switch to `pacman`?
5. Do we need `termux-api` equivalent for Nagato-specific Android APIs?
