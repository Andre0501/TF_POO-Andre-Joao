@echo off
cd /d "%~dp0"
call compilar.bat
if errorlevel 1 exit /b 1
java -cp bin TestesSistema
if errorlevel 1 exit /b 1
java -cp bin TesteRetomada gravar
if errorlevel 1 exit /b 1
java -cp bin TesteRetomada retomar
if errorlevel 1 exit /b 1
pause
