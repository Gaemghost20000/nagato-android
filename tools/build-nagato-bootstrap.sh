#!/bin/bash
# ============================================================================
# build-nagato-bootstrap.sh
# Run inside nagato-packages Docker container or with Docker wrapper
# ============================================================================
set -euo pipefail

ARCH="aarch64"  # RedMagic 10 Pro = aarch64
SCRIPTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPTS_DIR"

echo "=== Building Nagato bootstrap for $ARCH ==="

# Option A: Using generate-bootstraps.sh with local apt repo
# (Only works after building packages locally)

# Option B: Using build-bootstraps.sh (build from source)
# This is the correct approach for custom package names
echo "Running build-bootstraps.sh..."
if [ -f "scripts/build-bootstraps.sh" ]; then
    ./scripts/build-bootstraps.sh --architectures "$ARCH"
elif [ -f "scripts/generate-bootstraps.sh" ]; then
    echo "WARNING: generate-bootstraps.sh pulls from official apt repo."
    echo "         For custom package names, you MUST use build-bootstraps.sh"
    echo "         or build a local apt repo first."
fi

echo ""
echo "If build-bootstraps.sh is not present, download from:"
echo "  https://github.com/termux/termux-packages/blob/master/scripts/build-bootstraps.sh"
echo ""
echo "Bootstrap zip should appear as: bootstrap-${ARCH}.zip"
