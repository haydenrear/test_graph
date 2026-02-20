#!/usr/bin/env zsh

echo "Killing java -jar processes..."

# Function to get current java -jar PIDs
get_pids() {
  ps aux | grep "[j]ava -jar" | tr -s ' ' | cut -d ' ' -f 2
}

max_attempts=20
attempt=1

while [[ $attempt -le $max_attempts ]]; do
  pids=($(get_pids))

  if [[ ${#pids[@]} -eq 0 ]]; then
    echo "All processes terminated gracefully."
    exit 0
  fi

  echo "Attempt $attempt: killing -> $pids"

  for pid in $pids; do
    kill "$pid" 2>/dev/null || true
  done

  sleep 3
  ((attempt++))
done

# Final escalation
pids=($(get_pids))

if [[ ${#pids[@]} -gt 0 ]]; then
  echo "Escalating to SIGKILL for: $pids"
  for pid in $pids; do
    kill -9 "$pid" 2>/dev/null || true
    echo "Force killed $pid"
  done
else
  echo "All processes exited during retry window."
fi

echo "Done."
