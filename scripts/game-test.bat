@echo off
setlocal
rem Usage: game-test.bat [-Tests <namespace:path selector>] [-ServerDir <path>] [-KeepWorld] [-Memory <size>] [-JvmArgs <arg>] [-TimeoutSeconds <n>] [-StallPolls <n>] [-Java <java exe>]
rem Runs the GameTest gameplay tests in gametests\packs on the deployed jar and reports failures.
rem All arguments are forwarded unchanged to game-test.ps1; see Get-Help .\scripts\game-test.ps1 -Full.

set "SCRIPT=%~dp0game-test.ps1"
if not exist "%SCRIPT%" (
    echo game-test.ps1 not found next to %~nx0 1>&2
    exit /b 1
)

where pwsh >nul 2>nul
if %ERRORLEVEL% equ 0 (
    pwsh -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
)
exit /b %ERRORLEVEL%
