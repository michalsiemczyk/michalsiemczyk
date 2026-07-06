#!/usr/bin/env bash
# Kompiluje i uruchamia aplikację Pauza (wymaga Javy 17 lub nowszej).
set -e
cd "$(dirname "$0")"
mkdir -p out
javac --release 17 -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out pauza.Main "$@"
