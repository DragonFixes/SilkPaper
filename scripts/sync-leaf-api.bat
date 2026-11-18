@echo off
setlocal
rem Usage: sync-leaf-api.bat -LeafDir <patched Leaf checkout> [-SkipPatches] [-DryRun] [-Force]
rem        sync-leaf-api.bat -Clone [-Ref <leaf branch/tag/commit>] [-JavaHome <jdk 25 path>] [-SkipPatches] [-DryRun] [-Force]
rem All arguments are forwarded unchanged to sync-leaf-api.ps1.

if not exist "%~dp0sync-leaf-api.ps1" (
    echo sync-leaf-api.ps1 not found next to this script.
    exit /b 1
)

where pwsh >nul 2>nul
if %ERRORLEVEL%==0 (
    pwsh -NoProfile -ExecutionPolicy Bypass -File "%~dp0sync-leaf-api.ps1" %*
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0sync-leaf-api.ps1" %*
)
exit /b %ERRORLEVEL%
