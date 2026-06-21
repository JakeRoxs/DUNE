#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel)"
cd "$ROOT_DIR"

UPSTREAM_REMOTE="${UPSTREAM_REMOTE:-jellyfin}"
UPSTREAM_BRANCH="${UPSTREAM_BRANCH:-master}"
SYNC_PREFIX="${SYNC_PREFIX:-sync/jellyfin}"

if [[ -n "$(git status --porcelain)" ]]; then
  echo "✖ Working tree is dirty. Commit or stash changes before syncing."
  git status --short
  exit 1
fi

if ! git remote get-url "$UPSTREAM_REMOTE" >/dev/null 2>&1; then
  echo "Remote '$UPSTREAM_REMOTE' not found. Adding official Jellyfin remote."
  git remote add "$UPSTREAM_REMOTE" https://github.com/jellyfin/jellyfin-androidtv.git
fi

echo "Fetching latest from '$UPSTREAM_REMOTE'..."
git fetch "$UPSTREAM_REMOTE"

CURRENT_BRANCH="$(git symbolic-ref --short HEAD)"
SYNC_BRANCH="${SYNC_PREFIX}/${CURRENT_BRANCH}-$(date +%Y%m%d-%H%M)"

echo "Creating sync branch '$SYNC_BRANCH' from '$CURRENT_BRANCH'..."
git checkout -b "$SYNC_BRANCH"

echo "Merging '$UPSTREAM_REMOTE/$UPSTREAM_BRANCH' into '$SYNC_BRANCH'..."
if git merge --no-ff --no-edit "$UPSTREAM_REMOTE/$UPSTREAM_BRANCH"; then
  echo "✔ Merge completed successfully."
  echo "Review the merge on branch '$SYNC_BRANCH', run tests, and finalize the update." 
  echo "If you want to keep this branch, push it with: git push origin $SYNC_BRANCH"
else
  echo "✖ Merge stopped due to conflicts."
  echo "Resolve conflicts, then continue with: git merge --continue"
  exit 1
fi

echo "Upstream sync branch created: $SYNC_BRANCH"
