#!/usr/bin/env zsh
echo "Killing port $1"
pid=$(lsof -i:$1 | grep node | tr -s ' ' | cut -d ' ' -f 2)
echo "Killing $pid"
kill "$pid" || true