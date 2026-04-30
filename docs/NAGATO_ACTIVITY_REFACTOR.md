# NagatoActivity.java Refactor Guide

## What We're Doing

Rename `TermuxActivity.java` → `NagatoActivity.java` and refactor its `onCreate()`
method from a single-content terminal to a TabLayout + ViewPager2 with three tabs:

1. Terminal (existing terminal + session drawer)
2. Chat (WebView → localhost:8787)
3. TUI (Terminal session auto-running `hermes tui`)

## File Changes

### 1. Rename class declaration

```java
// OLD:
public final class TermuxActivity extends AppCompatActivity implements ServiceConnection {

// NEW:
public final class NagatoActivity extends AppCompatActivity implements ServiceConnection {
```

### 2. Add Tab/ViewPager fields

```java
// Add to class fields (after mExtraKeysView):
private TabLayout mTabLayout;
private ViewPager2 mViewPager;
private MainPagerAdapter mPagerAdapter;
```

### 3. Change setContentView in onCreate()

```java
// OLD (~line 216):
setContentView(R.layout.activity_termux);

// NEW:
setContentView(R.layout.activity_nagato);
mTabLayout = findViewById(R.id.tab_layout);
mViewPager = findViewById(R.id.view_pager);
mPagerAdapter = new MainPagerAdapter(this);
mViewPager.setAdapter(mPagerAdapter);
mViewPager.setUserInputEnabled(true); // Allow swipe between tabs

new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
    switch (position) {
        case 0: tab.setText("Terminal"); tab.setIcon(R.drawable.ic_terminal); break;
        case 1: tab.setText("Chat"); tab.setIcon(R.drawable.ic_chat); break;
        case 2: tab.setText("TUI"); tab.setIcon(R.drawable.ic_tui); break;
    }
}).attach();
```

### 4. Move terminal initialization into attachTerminalView() method

The TerminalFragment will call back to NagatoActivity to attach its TerminalView.
Extract the terminal session creation code into:

```java
public void attachTerminalView(TerminalView terminalView) {
    mTerminalView = terminalView;
    // Wire up all existing terminal initialization code:
    // - mTerminalView.setOnKeyListener(...)
    // - mTerminalView.setTextSelectionListener(...)
    // - mTerminalView.setBackground(...)
    // - ExtraKeysView etc.
    // (This is existing code from onCreate() ~lines 260-550)

    // After service connection, set the active session
    if (mTermuxService != null) {
        setupTerminalSession(terminalView);
    }
}

public void attachTuiView(TerminalView tuiView, String cmd) {
    // Create a new session for hermes tui
    // Use TermuxService.createTermuxSession(cmd, ...)
    // Attach to tuiView
}
```

### 5. Preserve all existing TermuxActivity logic

Everything else stays the same:
- Service binding to TermuxService
- onServiceConnected() session management
- Extra keys toolbar (visible only on Terminal tab)
- Drawer session list
- Context menus
- Settings activity launch
- Theme handling
- Broadcast receivers

### 6. Add auto-start after bootstrap

In `onServiceConnected()`, after bootstrap setup completes, add:

```java
TermuxInstaller.setupBootstrapIfNeeded(NagatoActivity.this, () -> {
    // Existing: add default session
    mTermuxTerminalSessionActivityClient.addNewSession(launchFailsafe, null);

    // NEW: After bootstrap AND if this is first run, auto-install hermes-agent
    if (isFirstBootstrap()) {
        runAutoSetup();
    }
});
```

### 7. Auto-setup method

```java
private void runAutoSetup() {
    new Thread(() -> {
        try {
            // Wait for bootstrap to fully extract before running commands
            Thread.sleep(2000);

            // Create auto-setup script in home directory
            String homeDir = TermuxConstants.TERMUX_FILES_DIR_PATH + "/home";
            File setupScript = new File(homeDir, ".nagato_setup.sh");
            try (FileWriter fw = new FileWriter(setupScript)) {
                fw.write("#!/data/data/com.nagato.agent/files/usr/bin/bash\n");
                fw.write("export PATH=/data/data/com.nagato.agent/files/usr/bin:$PATH\n");
                fw.write("pkg update -y\n");
                fw.write("pkg install -y python python-pip git curl openssl nodejs\n");
                fw.write("pip install --upgrade pip\n");
                fw.write("pip install hermes-agent[web]\n");
                fw.write("nohup hermes agent &\n");
                fw.write("nohup hermes webui &\n");
                fw.write("echo 'Nagato ready' > ~/.nagato_ready\n");
            }
            setupScript.setExecutable(true);

            // Execute in background terminal session
            mTermuxService.createTermuxSession(setupScript.getAbsolutePath(),
                new String[]{}, null, homeDir, false);

        } catch (Exception e) {
            Logger.logError(LOG_TAG, "Auto-setup failed: " + e.getMessage());
        }
    }).start();
}
```

## Critical Notes

- **Do NOT delete `TermuxActivity.java` until `NagatoActivity.java` compiles**
- Keep all existing activity lifecycle, permissions, and theme logic
- The tab switcher is additive — it wraps the existing terminal experience
- Consider whether Terminal tab should always be swipeable or locked when drawer is open
- The `EXTRA_FAILSAFE_SESSION` intent handling belongs on Terminal tab
- Settings button from drawer should still open SettingsActivity
