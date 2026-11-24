#Requires -Version 5.1

<#
.SYNOPSIS
    Boots the deployed Silkard jar once, runs console commands, stops it and reports problems.

.DESCRIPTION
    A startup smoke test. Starts the newest *.jar in <repo>\deploy in a
    throwaway server folder (default <repo>\deploy\boot-test, which accepts the
    EULA), feeds the given console commands followed by "stop" on standard
    input, and checks the log every 15 seconds until the server exits, crashes,
    goes quiet or passes -TimeoutSeconds. A hung server is killed.

    Passes (exit 0) when the server reached "Done", exited by itself and wrote
    no ERROR lines and no crash report. Otherwise prints the ERROR lines and
    exits 1. The full log stays in <ServerDir>\boot-test.log.

.PARAMETER Commands
    Console commands to run after startup, before "stop".
    Default: version, tps, ram, uptime.

.PARAMETER ServerDir
    Server folder for the test. Default <repo>\deploy\boot-test.

.PARAMETER Fresh
    Delete the test server folder first, so the world and configs are generated anew.

.PARAMETER Memory
    Heap size passed to -Xms and -Xmx. Default 3G.

.PARAMETER TimeoutSeconds
    Longest the whole run may take before the server is killed. Default 300.

.PARAMETER StallPolls
    How many 15-second checks in a row with no new log output count as a
    stall; the server is then killed. Default 2 (30 seconds).

.PARAMETER Java
    Java executable to use. Default "java" from PATH.

.EXAMPLE
    PS> .\scripts\boot-test.ps1
    Boots, runs the default commands, stops and reports.

.EXAMPLE
    PS> .\scripts\boot-test.ps1 -Fresh
    Same, with a newly generated world and configs.

.EXAMPLE
    PS> .\scripts\boot-test.ps1 -Commands 'plugins','minecraft:help tpsbar'
    Runs other console commands before stopping.
#>
[CmdletBinding()]
param(
    [string[]]$Commands = @('version', 'tps', 'ram', 'uptime'),
    [string]$ServerDir,
    [switch]$Fresh,
    [string]$Memory = '3G',
    [int]$TimeoutSeconds = 300,
    [ValidateRange(1, 100)]
    [int]$StallPolls = 2,
    [string]$Java = 'java'
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$deploy = Join-Path $root 'deploy'
if (-not $ServerDir) { $ServerDir = Join-Path $deploy 'boot-test' }
$pollSeconds = 15

$process = $null
try {
    $jar = Get-ChildItem -Path $deploy -Filter '*.jar' -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $jar) { throw "No jar in $deploy. Run scripts\build-deploy first." }
    if (-not (Get-Command $Java -ErrorAction SilentlyContinue)) { throw "Java executable '$Java' not found." }

    # The test folder holds only a generated world and configs.
    if ($Fresh -and (Test-Path $ServerDir)) { Remove-Item -LiteralPath $ServerDir -Recurse -Force }
    New-Item -ItemType Directory -Path $ServerDir -Force | Out-Null
    Set-Content -Path (Join-Path $ServerDir 'eula.txt') -Value 'eula=true' -Encoding ASCII
    $crashDir = Join-Path $ServerDir 'crash-reports'
    $crashesBefore = @(Get-ChildItem -Path $crashDir -File -ErrorAction SilentlyContinue).Count

    $inFile = Join-Path $ServerDir 'boot-test.in'
    $log = Join-Path $ServerDir 'boot-test.log'
    # The console reads these at once, but the server only runs them from its tick loop, after startup.
    [IO.File]::WriteAllText($inFile, ((@($Commands) + 'stop') -join "`n") + "`n")

    Write-Host "> (in $ServerDir) $Java -jar $($jar.Name) --nogui" -ForegroundColor Cyan
    $process = Start-Process -FilePath $Java -WorkingDirectory $ServerDir -NoNewWindow -PassThru `
        -ArgumentList @("-Xms$Memory", "-Xmx$Memory", '-jar', "`"$($jar.FullName)`"", '--nogui') `
        -RedirectStandardInput $inFile -RedirectStandardOutput $log -RedirectStandardError "$log.err"

    $state = 'timed out'
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastLength = -1
    $quiet = 0
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Seconds $pollSeconds
        if ($process.HasExited) { $state = 'exited'; break }
        $length = (Get-Item $log).Length
        if ($length -eq $lastLength) { $quiet++ } else { $quiet = 0; $lastLength = $length }
        if ($quiet -ge $StallPolls) { $state = 'stalled'; break }
        $lastLine = (Get-Content $log -Tail 1)
        Write-Host "  [$([int]((Get-Date) - $process.StartTime).TotalSeconds)s] $lastLine"
    }
    if (-not $process.HasExited) {
        # A thread dump of the stuck JVM is the quickest way to see where it is blocked.
        $jstack = Get-Command jstack -ErrorAction SilentlyContinue
        if ($jstack) {
            $dump = Join-Path $ServerDir 'boot-test-threads.txt'
            & $jstack.Source $process.Id > $dump 2>&1
            Write-Host "Thread dump: $dump"
        }
        Write-Warning "Server $state; killing it."
        Stop-Process -Id $process.Id -Force
    }

    $text = Get-Content $log -Raw
    $done = $text -match 'Done \([0-9.]+s\)!'
    $errors = @(Select-String -Path $log -Pattern '^\[[0-9:]+ ERROR\]' | ForEach-Object { $_.Line })
    $newCrashes = @(Get-ChildItem -Path $crashDir -File -ErrorAction SilentlyContinue).Count - $crashesBefore

    Write-Host ''
    Write-Host "Reached Done: $done   Ended: $state   ERROR lines: $($errors.Count)   New crash reports: $newCrashes"
    foreach ($line in $errors | Select-Object -First 30) { Write-Host "  $line" -ForegroundColor Red }
    Write-Host "Log: $log"

    if ($done -and $state -eq 'exited' -and $errors.Count -eq 0 -and $newCrashes -eq 0) {
        Write-Host 'Boot test passed.' -ForegroundColor Green
        exit 0
    }
    Write-Host 'Boot test failed.' -ForegroundColor Red
    exit 1
} catch {
    if ($process -and -not $process.HasExited) { Stop-Process -Id $process.Id -Force }
    Write-Error $_
    exit 1
}
