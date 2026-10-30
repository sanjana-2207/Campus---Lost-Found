@echo off
REM ============================================================
REM  Start the Campus Lost & Found application
REM  Run compile.bat first.
REM ============================================================
setlocal
cd /d "%~dp0"

if not exist out (
    echo [ERROR] The out folder is missing. Run compile.bat first.
    pause
    exit /b 1
)

java -cp "out;lib/*" com.campus.lostfound.Main
if errorlevel 1 (
    echo.
    echo [ERROR] The application exited with an error.
    pause
)
endlocal
