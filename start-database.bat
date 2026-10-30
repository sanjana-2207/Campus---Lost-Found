@echo off
REM ============================================================
REM  Start the local MySQL server this project connects to.
REM  Instance: C:\Users\Soul_Enoch\MySQL  (user-level, no service)
REM  Login:    root / root   on  localhost:3306
REM ============================================================
setlocal
set "MYSQLD=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
set "MYINI=C:\Users\Soul_Enoch\MySQL\my.ini"

netstat -ano | findstr /r /c:":3306 .*LISTENING" >nul 2>nul
if not errorlevel 1 (
    echo MySQL is already running on port 3306.
    timeout /t 2 >nul
    exit /b 0
)

start "" /min "%MYSQLD%" --defaults-file="%MYINI%"
echo Starting MySQL...

for /l %%i in (1,1,30) do (
    timeout /t 1 >nul
    netstat -ano | findstr /r /c:":3306 .*LISTENING" >nul 2>nul
    if not errorlevel 1 goto :ready
)

echo [ERROR] MySQL did not come up. Check:
echo         C:\Users\Soul_Enoch\MySQL\mysqld-error.log
pause
exit /b 1

:ready
echo MySQL is running on port 3306. You can start run.bat now.
timeout /t 3 >nul
endlocal
