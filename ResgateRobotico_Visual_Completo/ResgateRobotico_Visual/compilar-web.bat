@echo off
setlocal
cd /d "%~dp0"
if not exist extra-web\build\web mkdir extra-web\build\web
(for %%f in (src\*.java extra-web\src\*.java) do @echo "%%f") > extra-web\fontes.txt
java -m jdk.compiler/com.sun.tools.javac.Main -source 21 -target 21 --add-modules jdk.httpserver -encoding UTF-8 -d extra-web\build @extra-web\fontes.txt
if errorlevel 1 (
  echo Falha de compilacao. Use o JDK 21 ou superior.
  exit /b 1
)
copy /y extra-web\web\* extra-web\build\web\ >nul
java -m jdk.jartool/sun.tools.jar.Main --create --file ResgateRoboticoVisual.jar --main-class ServidorWeb -C extra-web\build .
if errorlevel 1 exit /b 1
echo Versao visual compilada: ResgateRoboticoVisual.jar
