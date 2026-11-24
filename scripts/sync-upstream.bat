@echo off
setlocal
rem Usage: sync-upstream.bat [-Merge <ref>] [-NoFetch] [-Force] [-DryRun]
rem Fetches upstream Silkard, updates the 26.3 mirror, reports drift.
rem All arguments are forwarded unchanged to sync-upstream.ps1; see Get-Help .\scripts\sync-upstream.ps1 -Full.

set "SCRIPT=%~dp0sync-upstream.ps1"
if not exist "%SCRIPT%" (
    echo sync-upstream.ps1 not found next to %~nx0 1>&2
    exit /b 1
)

where pwsh >nul 2>nul
if %ERRORLEVEL% equ 0 (
    pwsh -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
)
exit /b %ERRORLEVEL%
