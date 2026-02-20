#!/usr/bin/env zsh

port=$1

if [[ -z "$port" ]]; then
  echo "Usage: $0 <port>"
  exit 1
fi

echo "Killing processes on port $port"

get_pids() {
  lsof -ti tcp:$port
}

max_attempts=20
attempt=1

while [[ $attempt -le $max_attempts ]]; do
  pids=($(get_pids))

  if [[ ${#pids[@]} -eq 0 ]]; then
    echo "Port $port is free."
    exit 0
  fi

  echo "Attempt $attempt: killing -> $pids"

  for pid in $pids; do
    kill "$pid" 2>/dev/null || true
  done

  sleep 3
  ((attempt++))
done

# Escalate
pids=($(get_pids))

if [[ ${#pids[@]} -gt 0 ]]; then
  echo "Escalating to SIGKILL for: $pids"
  for pid in $pids; do
    kill -9 "$pid" 2>/dev/null || true
    echo "Force killed $pid"
  done
else
  echo "Port freed during retry window."
fi

echo "Done."
