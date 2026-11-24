@echo off
setlocal
rem Usage: boot-test.bat [-Commands <cmd1>,<cmd2>] [-CommandDelaySeconds <n>] [-ServerDir <path>] [-Fresh] [-Memory <size>] [-TimeoutSeconds <n>] [-StallPolls <n>] [-MixinAudit] [-Java <java exe>]
rem Boots the deployed jar, runs console commands after startup, stops it, reports ERROR lines and crashes.
rem All arguments are forwarded unchanged to boot-test.ps1; see Get-Help .\scripts\boot-test.ps1 -Full.

set "SCRIPT=%~dp0boot-test.ps1"
if not exist "%SCRIPT%" (
    echo boot-test.ps1 not found next to %~nx0 1>&2
    exit /b 1
)

where pwsh >nul 2>nul
if %ERRORLEVEL% equ 0 (
    pwsh -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
)
exit /b %ERRORLEVEL%
