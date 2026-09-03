#!/usr/bin/env sh
set -eu
VERSION="9.5.0"
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/teclado-wrapper"
HOME_DIR="$BASE/gradle-$VERSION"
if [ ! -x "$HOME_DIR/bin/gradle" ]; then
  mkdir -p "$BASE"
  ZIP="$BASE/gradle-$VERSION-bin.zip"
  curl -fsSL "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$ZIP"
  rm -rf "$HOME_DIR"
  unzip -q -o "$ZIP" -d "$BASE"
fi
exec "$HOME_DIR/bin/gradle" "$@"
