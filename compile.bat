@echo off
REM ============================================================
REM  Compile the Campus Lost & Found application
REM  Requires: JDK 8 or newer on the PATH, and the MySQL
REM  Connector/J jar inside the lib folder.
REM ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
    echo [ERROR] javac was not found. Install a JDK and add its bin folder to PATH.
    pause
    exit /b 1
)

if not exist lib\*.jar (
    echo [ERROR] No JDBC driver found. Download mysql-connector-j and put the
    echo         jar file inside the lib folder.
    pause
    exit /b 1
)

if not exist out mkdir out

REM Build a javac @argfile. Paths are quoted (they contain spaces) and use
REM forward slashes, because backslashes inside quotes are javac escapes.
(for /r src %%f in (*.java) do @(set "srcfile=%%f" & echo "!srcfile:\=/!")) > sources.txt

javac -encoding UTF-8 -d out -cp "lib/*" @sources.txt
if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed. Fix the errors above and try again.
    del sources.txt >nul 2>nul
    pause
    exit /b 1
)

xcopy resources out /E /I /Y >nul

del sources.txt >nul 2>nul

echo.
echo Build successful. Start the application with run.bat
echo.
pause
endlocal
