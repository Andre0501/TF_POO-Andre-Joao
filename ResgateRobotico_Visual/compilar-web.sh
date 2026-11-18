#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
command -v java >/dev/null || { echo "Instale o JDK 21 ou superior para continuar."; exit 1; }
mkdir -p extra-web/build/web
FONTES=(src/*.java extra-web/src/*.java)
if command -v javac >/dev/null; then
  javac -source 21 -target 21 --add-modules jdk.httpserver -encoding UTF-8 -d extra-web/build "${FONTES[@]}"
else
  java -m jdk.compiler/com.sun.tools.javac.Main -source 21 -target 21 --add-modules jdk.httpserver -encoding UTF-8 -d extra-web/build "${FONTES[@]}"
fi
cp extra-web/web/* extra-web/build/web/
if command -v jar >/dev/null; then
  jar --create --file ResgateRoboticoVisual.jar --main-class ServidorWeb -C extra-web/build .
else
  java -m jdk.jartool/sun.tools.jar.Main --create --file ResgateRoboticoVisual.jar --main-class ServidorWeb -C extra-web/build .
fi
echo "Versão visual compilada: ResgateRoboticoVisual.jar"
