@echo off
rem Kompiluje i uruchamia aplikacje Pauza (wymaga Javy 17 lub nowszej).
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac --release 17 -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    echo Blad kompilacji.
    pause
    exit /b 1
)
del sources.txt
start javaw -cp out pauza.Main
