#!/bin/sh
set -e
cd "$(dirname "$0")"
mkdir -p bin
javac -encoding UTF-8 -d bin src/*.java
jar cfe ResgateRobotico.jar Aplicacao -C bin .
echo "Compilacao concluida."
