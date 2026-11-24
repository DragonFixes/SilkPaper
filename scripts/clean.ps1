#Requires -Version 5.1

<#
.SYNOPSIS
    Removes Gradle build output, and optionally the deployed jars.

.DESCRIPTION
    Runs "gradlew clean" from the repository root. With -Deploy, also deletes
    every *.jar in <repo>\deploy; the server folder under deploy is left alone.

.PARAMETER Deploy
    Also delete the jars in the deploy folder.

.PARAMETER DryRun
    Print what would run and be deleted without doing it.

.EXAMPLE
    PS> .\scripts\clean.ps1
    Runs gradlew clean.

.EXAMPLE
    PS> .\scripts\clean.ps1 -Deploy
    Runs gradlew clean and deletes the deployed jars.

.EXAMPLE
    PS> .\scripts\clean.ps1 -Deploy -DryRun
    Shows what would be cleaned.
#>
[CmdletBinding()]
param(
    [switch]$Deploy,
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$deployDir = Join-Path $root 'deploy'

try {
    Write-Host '> gradlew clean' -ForegroundColor Cyan
    if (-not $DryRun) {
        Push-Location $root
        try {
            & (Join-Path $root 'gradlew.bat') clean
            if ($LASTEXITCODE -ne 0) { throw "gradlew clean failed with exit code $LASTEXITCODE" }
        } finally {
            Pop-Location
        }
    }

    if ($Deploy) {
        # Deployed jars are build output; scripts\build-deploy regenerates them.
        foreach ($jar in @(Get-ChildItem -Path $deployDir -Filter '*.jar' -File -ErrorAction SilentlyContinue)) {
            Write-Host "Removing $($jar.FullName)"
            if (-not $DryRun) { Remove-Item -LiteralPath $jar.FullName -Force }
        }
    }
    exit 0
} catch {
    Write-Error $_
    exit 1
}
