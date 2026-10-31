@echo off
REM ============================================================
REM  Start the local MySQL server this project connects to.
REM  Instance: %USERPROFILE%\MySQL  (user-level, no service)
REM  Override with the MYSQLD / MYINI / LOGFILE environment variables.
REM
REM  Usage:
REM    start-database.bat          interactive (pauses on error)
REM    start-database.bat quiet    called by run.bat (no pause)
REM  Exit code: 0 = server is up, 1 = server could not be started.
REM ============================================================
setlocal
if not defined MYSQLD set "MYSQLD=%ProgramFiles%\MySQL\MySQL Server 8.4\bin\mysqld.exe"
if not exist "%MYSQLD%" (
    for /d %%d in ("%ProgramFiles%\MySQL\MySQL Server *") do (
        if exist "%%~fd\bin\mysqld.exe" set "MYSQLD=%%~fd\bin\mysqld.exe"
    )
)
if not defined MYINI set "MYINI=%USERPROFILE%\MySQL\my.ini"
if not defined LOGFILE set "LOGFILE=%USERPROFILE%\MySQL\mysqld-error.log"
set "QUIET=%~1"
set "EXITCODE=0"

netstat -ano | findstr /r /c:":3306 .*LISTENING" >nul 2>nul
if not errorlevel 1 goto :already

if not exist "%MYSQLD%" (
    echo [ERROR] mysqld.exe was not found. Install MySQL Server or set MYSQLD
    echo         to the full path of mysqld.exe.
    set "EXITCODE=1"
    goto :done
)

if not exist "%MYINI%" (
    echo [ERROR] MySQL configuration file was not found at:
    echo         %MYINI%
    echo         Create it, or set MYINI to your my.ini path.
    set "EXITCODE=1"
    goto :done
)

start "" /min "%MYSQLD%" --defaults-file="%MYINI%"
echo Starting MySQL...

for /l %%i in (1,1,30) do (
    REM ping is used as the 1-second delay: unlike timeout it also works
    REM when the script runs with redirected or missing console input.
    ping -n 2 127.0.0.1 >nul 2>nul
    netstat -ano | findstr /r /c:":3306 .*LISTENING" >nul 2>nul
    if not errorlevel 1 goto :ready
)

echo [ERROR] MySQL did not come up. Check:
echo         %LOGFILE%
set "EXITCODE=1"
goto :done

:already
if /i not "%QUIET%"=="quiet" (
    echo MySQL is already running on port 3306.
    ping -n 3 127.0.0.1 >nul 2>nul
)
goto :done

:ready
if /i "%QUIET%"=="quiet" (
    echo MySQL is running on port 3306.
    goto :done
)
echo MySQL is running on port 3306. You can start run.bat now.
ping -n 4 127.0.0.1 >nul 2>nul

:done
if /i not "%QUIET%"=="quiet" (
    if not "%EXITCODE%"=="0" pause
)
endlocal & exit /b %EXITCODE%
