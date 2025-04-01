@echo off
setlocal

:: Define service name and port
set SERVICE_NAME=lms-service-gateway
set PORT=9093

:: Find the process ID (PID) running on the specified port
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT% ^| findstr LISTENING') do set PID=%%a

:: Check if a valid PID was found
if "%PID%"=="" (
    echo ❌ No service running on port %PORT%.
    exit /b 1
)

:: Stop the service
echo 🛑 Stopping %SERVICE_NAME% running on port %PORT% (PID: %PID%)...
taskkill /PID %PID% /F

:: Confirm the process was stopped
echo ✅ %SERVICE_NAME% stopped.

endlocal
