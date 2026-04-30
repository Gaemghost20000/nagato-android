# Nagato Android — Build Checklist

## Status Legend
 - [x] Done
 - [ ] Needed

## Java Source Files
- [x] NagatoActivity.java / NagatoActivityV2.java
- [x] NagatoForegroundService.java (agent keepalive)
- [x] NagatoBubbleService.java (chat heads)
- [x] NagatoJSBridge.java (WebView bridge)
- [x] BottomNavAdapter.java (ViewPager2 adapter)
- [x] ChatFragment.java / ChatFragmentV2.java (WebView hero)
- [x] TuiFragment.java (TerminalView for hermes tui)
- [x] DebugFragment.java (logs + controls)
- [x] MeFragment.java (settings + project mgmt)
- [ ] **MANUAL: Merge TermuxActivity → NagatoActivity** (termux-app fork originals)
- [ ] **MANUAL: Wire TermuxService or replace with NagatoForegroundService**
- [ ] **MANUAL: Rename package directories** (com/termux → com/nagato/agent)

## Resources
- [x] activity_nagato.xml (BottomNav + ViewPager2)
- [x] fragment_chat.xml (WebView + loading)
- [x] fragment_tui.xml (TerminalView + project bar)
- [x] fragment_debug.xml (agent controls + logs)
- [x] fragment_me.xml (settings list)
- [x] nagato_bottom_nav.xml (menu: Chat/TUI/Debug/Me)
- [x] colors_nagato.xml (#0D0B14 theme)
- [x] ic_nagato.xml (purple chat icon)
- [x] ic_terminal.xml (terminal icon)
- [x] ic_bug.xml (debug icon)
- [ ] ic_person.xml (profile icon for Me tab)
- [ ] ic_chat.xml (chat icon for Chat tab)
- [ ] project_chip_active.xml / project_chip_inactive.xml (done but may not be used)

## AndroidManifest
- [ ] Add <service> entries for NagatoForegroundService + NagatoBubbleService
- [ ] Add <uses-permission> for FOREGROUND_SERVICE, POST_NOTIFICATIONS
- [ ] Update <activity> android:name to NagatoActivity
- [ ] Set foregroundServiceType="specialUse"
- [ ] See docs/MANIFEST_ADDITIONS.xml

## Build Configuration
- [x] tools/setup-nagato-fork.sh (automated com.termux → com.nagato.agent rename)
- [ ] **MANUAL: app/build.gradle** — add ViewPager2 + TabLayout dependencies
- [ ] **MANUAL: app/build.gradle** — change applicationId to com.nagato.agent
- [ ] **MANUAL: app/build.gradle** — disable downloadBootstraps + use local bootstrap zip
- [ ] **MANUAL: app/build.gradle** — minimum SDK 26 (Android 8+)

## Bootstrap (Building on Nagato VM right now)
- [ ] bootstrap-aarch64.zip (being built — ~2 hours remaining)
- [ ] Place in app/src/main/cpp/bootstrap-aarch64.zip

## Hermes-Agent Integration
- [x] Auto-start script documented (docs/AUTO_START.md)
- [ ] **MANUAL: Wire auto-start into NagatoActivity.onAgentReady() or NagatoForegroundService**
- [ ] **MANUAL: Add first-run detection** (check ~/.nagato_ready file)

## Testing
- [ ] Build APK: cd nagato-android && ./gradlew assembleDebug
- [ ] Install: adb install -r app/build/outputs/apk/debug/*.apk
- [ ] Verify: Bootstrap extracts to /data/data/com.nagato.agent/files/usr
- [ ] Verify: WebView loads http://localhost:8787
- [ ] Verify: Foreground notification appears
- [ ] Verify: Bubble shows on message (Android 11+)
- [ ] Verify: Coexists with com.termux without conflicts
