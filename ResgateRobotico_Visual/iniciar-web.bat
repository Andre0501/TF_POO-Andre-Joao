@echo off
setlocal
cd /d "%~dp0"
set "RESGATE_PORTA=8080"
if not "%~1"=="" set "RESGATE_PORTA=%~1"
set "RESGATE_ARQUIVO=extra-web/dados/central-web.dat"
if not "%~2"=="" set "RESGATE_ARQUIVO=%~2"
call compilar-web.bat
if errorlevel 1 (
  pause
  exit /b 1
)
java -Duser.timezone=America/Sao_Paulo --add-modules jdk.httpserver -jar ResgateRoboticoVisual.jar "%RESGATE_PORTA%" "%RESGATE_ARQUIVO%"
pause
