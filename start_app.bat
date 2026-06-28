@echo off
REM ─────────────────────────────────────────────────────────────────────────
REM  start_app.bat — One-click launcher for NeoVaccine Designer (Windows)
REM  
REM  HOW TO USE:
REM    Double-click this file in Windows Explorer, OR
REM    Run it from command prompt: start_app.bat
REM ─────────────────────────────────────────────────────────────────────────

echo.
echo  ========================================
echo   NeoVaccine Designer — Starting Up...
echo  ========================================
echo.

REM Step 1: Start the Python backend in a separate window
echo [1/2] Starting Python backend server...
start "NeoVaccine Backend" cmd /k "cd /d %~dp0backend && python app.py"

REM Wait 2 seconds for the backend to start
timeout /t 2 /nobreak > nul

REM Step 2: Start the JavaFX frontend
echo [2/2] Starting JavaFX frontend...
cd /d %~dp0frontend
mvn javafx:run

echo.
echo App closed. Press any key to exit.
pause
