#Requires -Version 5.1

<#
.SYNOPSIS
    Boots the deployed SilkPaper jar once, runs console commands, stops it and reports problems.

.DESCRIPTION
    A startup smoke test. Starts the newest *.jar in <repo>\deploy in a
    throwaway server folder (default <repo>\deploy\boot-test, which accepts the
    EULA). Once the server logs "Done" and -CommandDelaySeconds have passed, the
    given console commands and then "stop" are typed into its console.

    The log is checked every 15 seconds. A server that writes nothing new for
    -StallPolls checks in a row, or runs past -TimeoutSeconds, is treated as
    hung: a jstack thread dump is saved next to the test folder and the server
    is killed.

    Passes (exit 0) when the server reached "Done", exited by itself and wrote
    no ERROR lines, no exceptions (Throwable class names such as
    NoClassDefFoundError, "Caused by:" or "Unexpected exception") and no crash
    report. Otherwise prints the problem lines and exits 1. The full log stays
    in <ServerDir>\boot-test.log.

.PARAMETER Commands
    Console commands to run after startup, before "stop". Commas also separate
    commands, so -Commands 'tps,ram' works through the .bat launcher.
    Default: version, tps, ram, uptime.

.PARAMETER CommandDelaySeconds
    Seconds to wait after "Done" before typing the commands, so tick-time and
    memory readings have samples. Default 25.

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
    stall. Default 2 (30 seconds). Keep it above CommandDelaySeconds / 15.

.PARAMETER MixinAudit
    Pass -Dsilkpaper.mixinAudit=true, so after "Done" the server force-loads
    every mixin target class. A mixin whose injection no longer matches then
    fails the test even when its class does not load during a normal startup.

.PARAMETER Java
    Java executable to use. Default "java" from PATH.

.EXAMPLE
    PS> .\scripts\boot-test.ps1
    Boots, runs the default commands, stops and reports.

.EXAMPLE
    PS> .\scripts\boot-test.ps1 -MixinAudit
    Also checks every mixin, not only those on classes loaded at startup.

.EXAMPLE
    PS> .\scripts\boot-test.ps1 -Fresh
    Same, with a newly generated world and configs.

.EXAMPLE
    PS> .\scripts\boot-test.ps1 -Commands 'plugins,minecraft:help tpsbar' -CommandDelaySeconds 5
    Runs other console commands soon after startup.
#>
[CmdletBinding()]
param(
    [string[]]$Commands = @('version', 'tps', 'ram', 'uptime'),
    [ValidateRange(0, 600)]
    [int]$CommandDelaySeconds = 25,
    [string]$ServerDir,
    [switch]$Fresh,
    [string]$Memory = '3G',
    [int]$TimeoutSeconds = 300,
    [ValidateRange(1, 100)]
    [int]$StallPolls = 2,
    [switch]$MixinAudit,
    [string]$Java = 'java'
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$deploy = Join-Path $root 'deploy'
if (-not $ServerDir) { $ServerDir = Join-Path $deploy 'boot-test' }
$pollSeconds = 15

$process = $null

function Get-JavaChild([int]$ParentId) {
    Get-CimInstance Win32_Process -Filter "ParentProcessId=$ParentId" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -like 'java*' } | Select-Object -First 1
}

function Stop-Server {
    if ($process -and -not $process.HasExited) {
        # cmd.exe wraps java; /T takes the java child with it.
        & taskkill.exe /PID $process.Id /T /F *> $null
    }
}

try {
    $jar = Get-ChildItem -Path $deploy -Filter '*.jar' -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $jar) { throw "No jar in $deploy. Run scripts\build-deploy first." }
    $javaExe = (Get-Command $Java -ErrorAction SilentlyContinue).Source
    if (-not $javaExe) { throw "Java executable '$Java' not found." }

    # The test folder holds only a generated world and configs.
    if ($Fresh -and (Test-Path $ServerDir)) { Remove-Item -LiteralPath $ServerDir -Recurse -Force }
    New-Item -ItemType Directory -Path $ServerDir -Force | Out-Null
    Set-Content -Path (Join-Path $ServerDir 'eula.txt') -Value 'eula=true' -Encoding ASCII
    $crashDir = Join-Path $ServerDir 'crash-reports'
    $crashesBefore = @(Get-ChildItem -Path $crashDir -File -ErrorAction SilentlyContinue).Count
    $log = Join-Path $ServerDir 'boot-test.log'
    Remove-Item -LiteralPath $log, "$log.err" -Force -ErrorAction SilentlyContinue

    # Through the .bat launcher (-File), "-Commands a,b" arrives as one string, so commas separate commands.
    $commandList = @($Commands | ForEach-Object { $_ -split ',' } | ForEach-Object { $_.Trim() } | Where-Object { $_ })

    # cmd.exe sends java's output straight to the log files while this script keeps the
    # console input pipe, so commands can be typed once the server is up.
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = 'cmd.exe'
    $auditArg = if ($MixinAudit) { ' -Dsilkpaper.mixinAudit=true' } else { '' }
    $psi.Arguments = "/d /s /c `"`"$javaExe`" -Xms$Memory -Xmx$Memory$auditArg -jar `"$($jar.FullName)`" --nogui > `"$log`" 2> `"$log.err`"`""
    $psi.WorkingDirectory = $ServerDir
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    Write-Host "> (in $ServerDir) $Java -jar $($jar.Name) --nogui" -ForegroundColor Cyan
    $process = [System.Diagnostics.Process]::Start($psi)
    $started = Get-Date

    $state = 'timed out'
    $deadline = $started.AddSeconds($TimeoutSeconds)
    $doneAt = $null
    $commandsSent = $false
    $lastLength = -1
    $quiet = 0
    $nextPoll = $started.AddSeconds($pollSeconds)
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Seconds 1
        if ($process.HasExited) { $state = 'exited'; break }

        if (-not $doneAt -and (Test-Path $log) -and (Select-String -Path $log -Pattern 'Done \([0-9.]+s\)!' -Quiet)) {
            $doneAt = Get-Date
            Write-Host "  Server is up; typing commands in $CommandDelaySeconds s."
        }
        if ($doneAt -and -not $commandsSent -and ((Get-Date) - $doneAt).TotalSeconds -ge $CommandDelaySeconds) {
            foreach ($command in @($commandList) + 'stop') {
                Write-Host "  > $command"
                $process.StandardInput.WriteLine($command)
            }
            $process.StandardInput.Flush()
            $commandsSent = $true
            # Sending commands is activity; restart the stall count from here.
            $quiet = 0
            $lastLength = -1
        }

        if ((Get-Date) -lt $nextPoll) { continue }
        $nextPoll = (Get-Date).AddSeconds($pollSeconds)
        $length = if (Test-Path $log) { (Get-Item $log).Length } else { 0 }
        # Waiting out CommandDelaySeconds is expected silence, not a stall.
        $waitingToSend = $doneAt -and -not $commandsSent
        if ($length -eq $lastLength -and -not $waitingToSend) { $quiet++ } else { $quiet = 0; $lastLength = $length }
        if ($quiet -ge $StallPolls) { $state = 'stalled'; break }
        $lastLine = if (Test-Path $log) { Get-Content $log -Tail 1 } else { '' }
        Write-Host "  [$([int]((Get-Date) - $started).TotalSeconds)s] $lastLine"
    }

    if (-not $process.HasExited) {
        # A thread dump of the stuck JVM is the quickest way to see where it is blocked.
        $jstack = Get-Command jstack -ErrorAction SilentlyContinue
        $javaProcess = Get-JavaChild $process.Id
        if ($jstack -and $javaProcess) {
            # Kept beside the test folder so a later -Fresh run does not delete it.
            $dump = Join-Path (Split-Path $ServerDir -Parent) ("boot-test-threads-{0:yyyyMMdd-HHmmss}.txt" -f (Get-Date))
            & $jstack.Source $javaProcess.ProcessId > $dump 2>&1
            Write-Host "Thread dump: $dump"
        }
        Write-Warning "Server $state; killing it."
        Stop-Server
    }

    $text = if (Test-Path $log) { Get-Content $log -Raw } else { '' }
    $done = $text -match 'Done \([0-9.]+s\)!'
    # ERROR lines, plus exceptions that the server logs at WARN (failed commands, missing classes).
    $errors = @(Select-String -Path $log -Pattern '^\[[0-9:]+ ERROR\]|Unexpected exception|^\S*(Exception|Error)(:|$)|^Caused by:' -ErrorAction SilentlyContinue | ForEach-Object { $_.Line })
    $newCrashes = @(Get-ChildItem -Path $crashDir -File -ErrorAction SilentlyContinue).Count - $crashesBefore

    Write-Host ''
    Write-Host "Reached Done: $done   Ended: $state   Problem lines: $($errors.Count)   New crash reports: $newCrashes"
    foreach ($line in $errors | Select-Object -First 30) { Write-Host "  $line" -ForegroundColor Red }
    Write-Host "Log: $log"

    if ($done -and $state -eq 'exited' -and $errors.Count -eq 0 -and $newCrashes -eq 0) {
        Write-Host 'Boot test passed.' -ForegroundColor Green
        exit 0
    }
    Write-Host 'Boot test failed.' -ForegroundColor Red
    exit 1
} catch {
    Stop-Server
    Write-Error $_
    exit 1
}
