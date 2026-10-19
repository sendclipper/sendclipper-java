#!/usr/bin/env bash
# Publish sdks/java to its public GitHub repository.
#
# The monorepo (GitLab, private) is the source of truth. This pushes ONLY the
# sdks/java subtree, with its history, to github.com/sendclipper/sendclipper-java.
# Never push the monorepo itself there.
#
#   sdks/java/scripts/split-to-github.sh            # push main
#   sdks/java/scripts/split-to-github.sh v0.1.0     # push main and tag v0.1.0
#
# A tag IS the release: .github/workflows/release.yml signs the jars and
# uploads them to Maven Central, where a version is permanent (it can never be
# replaced or deleted). Let CI go green on main first, then tag.
#
# REMOTE defaults to the `github-sendclipper` SSH alias (~/.ssh/config), which
# authenticates as the sendclipper GitHub account.
set -euo pipefail

REMOTE="${REMOTE:-git@github-sendclipper:sendclipper/sendclipper-java.git}"
TAG="${1:-}"
ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

if [ -n "$(git status --porcelain -- sdks/java)" ]; then
  echo "sdks/java has uncommitted changes; commit them first." >&2
  exit 1
fi

if [ -n "$TAG" ]; then
  version="$(sed -n 's/.*VERSION = "\(.*\)";/\1/p' sdks/java/src/main/java/com/sendclipper/Version.java)"
  if [ "v$version" != "$TAG" ]; then
    echo "tag $TAG does not match Version.VERSION $version" >&2
    exit 1
  fi
  if ! grep -q "<version>$version</version>" sdks/java/pom.xml; then
    echo "pom.xml does not declare version $version" >&2
    exit 1
  fi
  if git ls-remote --exit-code --tags "$REMOTE" "refs/tags/$TAG" >/dev/null 2>&1; then
    echo "tag $TAG already exists on $REMOTE; Maven Central versions are permanent, bump the version instead." >&2
    exit 1
  fi
fi

SPLIT="$(git subtree split --prefix=sdks/java HEAD 2>/dev/null)"
echo "split commit: $SPLIT"
git push "$REMOTE" "$SPLIT:refs/heads/main"

if [ -n "$TAG" ]; then
  git push "$REMOTE" "$SPLIT:refs/tags/$TAG"
fi
