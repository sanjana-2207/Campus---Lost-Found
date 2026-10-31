@echo off
REM ============================================================
REM  Start the Campus Lost & Found application
REM  Run compile.bat first.
REM
REM  This script also makes sure the local MySQL server is up,
REM  so the app never fails with "Cannot connect to MySQL".
REM ============================================================
setlocal
cd /d "%~dp0"

if not exist out (
    echo [ERROR] The out folder is missing. Run compile.bat first.
    pause
    exit /b 1
)

REM ---- Auto-start the local MySQL instance if port 3306 is closed ----
netstat -ano | findstr /r /c:":3306 .*LISTENING" >nul 2>nul
if errorlevel 1 (
    echo MySQL is not running. Starting it automatically...
    call "%~dp0start-database.bat" quiet
    if errorlevel 1 (
        echo.
        echo [ERROR] MySQL could not be started, so the application cannot connect.
        echo         Run start-database.bat to see the full error, or check:
        echo         %USERPROFILE%\MySQL\mysqld-error.log
        pause
        exit /b 1
    )
)

java -cp "out;lib/*" com.campus.lostfound.Main
if errorlevel 1 (
    echo.
    echo [ERROR] The application exited with an error.
    pause
)
endlocal
