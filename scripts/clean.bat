@echo off
setlocal
rem Usage: clean.bat [-Deploy] [-DryRun]
rem Runs gradlew clean; -Deploy also deletes deploy\*.jar.
rem All arguments are forwarded unchanged to clean.ps1; see Get-Help .\scripts\clean.ps1 -Full.

set "SCRIPT=%~dp0clean.ps1"
if not exist "%SCRIPT%" (
    echo clean.ps1 not found next to %~nx0 1>&2
    exit /b 1
)

where pwsh >nul 2>nul
if %ERRORLEVEL% equ 0 (
    pwsh -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
)
exit /b %ERRORLEVEL%
