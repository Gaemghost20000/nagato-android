#!/bin/bash
# ============================================================================
# setup-nagato-fork.sh
# Run this on Nagato VM after cloning both nagato-android & nagato-packages
# ============================================================================
set -euo pipefail

NAGATO_PACKAGE="com.nagato.agent"
NAGATO_UNAME="nagato"
NAGATO_NAME="Nagato"
NAGATO_ROOTFS="/data/data/com.nagato.agent/files"
NAGATO_PREFIX="/data/data/com.nagato.agent/files/usr"
NAGATO_HOME="/data/data/com.nagato.agent/files/home"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== [1/5] Configuring nagato-packages properties.sh ==="
cd nagato-packages/scripts

# Use python to patch properties.sh robustly (avoids sed escaping hell)
python3 - "$NAGATO_PACKAGE" "$NAGATO_ROOTFS" "$NAGATO_PREFIX" "$NAGATO_HOME" "${NAGATO_UNAME}" "${NAGATO_NAME}" 2>/dev/null || python - "$NAGATO_PACKAGE" "$NAGATO_ROOTFS" "$NAGATO_PREFIX" "$NAGATO_HOME" "${NAGATO_UNAME}" "${NAGATO_NAME}" 2>/dev/null || true

echo "=== [2/5] Verify no com.termux references remain in key paths ==="
grep -n "com.termux" properties.sh | grep -v "TERMUX_REPO" | head -20 || echo "Looks clean"

cd "$SCRIPT_DIR"

echo "=== [3/5] Configuring nagato-android TermuxConstants ==="
cd nagato-android

# Mass replace com.termux → com.nagato.agent in Java source
find termux-shared -type f -name "*.java" -exec sed -i "s/com\.termux/com.nagato.agent/g" {} +

# But keep plugin package names configurable (API, Boot, etc.)
# For now we rename everything including plugin references
echo "Note: Plugin names (com.termux.api etc.) will also be renamed to com.nagato.agent.*"
echo "If you want separate plugins later, update TermuxConstants.java manually"

echo "=== [4/5] Replace bootstrap download URL ==="
cd app
# Disable the downloadBootstraps task by commenting it out or replacing URLs
# For now we just add a note in build.gradle
sed -i "s|https://github.com/termux/termux-packages/releases/download/bootstrap|https://github.com/Gaemghost20000/nagato-packages/releases/download/bootstrap|g" build.gradle || true

cd "$SCRIPT_DIR"

echo "=== [5/5] Stage changes for commit ==="
git -C nagato-packages add -A
git -C nagato-android add -A

echo "Done. Review with:"
echo "  cd nagato-packages && git diff --stat"
echo "  cd nagato-android && git diff --stat"
echo ""
echo "Then commit and push:"
echo "  git -C nagato-packages commit -m 'Customize package name to ${NAGATO_PACKAGE}'"
echo "  git -C nagato-android   commit -m 'Rebrand for Nagato agent (${NAGATO_PACKAGE})'"
