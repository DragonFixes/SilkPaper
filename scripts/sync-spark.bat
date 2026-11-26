@echo off
setlocal
rem Usage: sync-spark.bat [-SparkDir <spark fork checkout>] [-JavaHome <jdk path>] [-AllowDirty] [-SkipBuild] [-DryRun] [-Force]
rem All arguments are forwarded unchanged to sync-spark.ps1.

if not exist "%~dp0sync-spark.ps1" (
    echo sync-spark.ps1 not found next to this script.
    exit /b 1
)

where pwsh >nul 2>nul
if %ERRORLEVEL%==0 (
    pwsh -NoProfile -ExecutionPolicy Bypass -File "%~dp0sync-spark.ps1" %*
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0sync-spark.ps1" %*
)
exit /b %ERRORLEVEL%
