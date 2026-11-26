#Requires -Version 5.1

<#
.SYNOPSIS
    Runs the GameTest-framework gameplay tests in gametests\packs on the deployed SilkPaper jar.

.DESCRIPTION
    Starts the newest *.jar in <repo>\deploy in a test server folder (default
    <repo>\deploy\game-test) on a newly generated flat world, with every
    datapack in <repo>\gametests\packs copied into world\datapacks. The server
    runs the tests matching -Tests once the worlds are loaded, writes a JUnit
    style report, and stops itself.

    The log is checked every 15 seconds. A server that writes nothing new for
    -StallPolls checks in a row, or runs past -TimeoutSeconds, is treated as
    hung: a jstack thread dump is saved next to the test folder and the server
    is killed.

    Passes (exit 0) when every required test passed and the server wrote no
    ERROR lines, no exceptions (Throwable class names, "Caused by:" or
    "Unexpected exception") and no crash report: a test can pass while the code
    it drives logs an error. Prints each FAIL and problem line and exits 1
    otherwise, or when the run did not finish. The full log stays in
    <ServerDir>\game-test.log and the report in <ServerDir>\report.xml.

.PARAMETER Tests
    Test selector, as vanilla's /test command takes it: namespace:path with *
    wildcards. One selector only. Default "*:*" (every test).

.PARAMETER ServerDir
    Server folder for the test. Default <repo>\deploy\game-test.

.PARAMETER KeepWorld
    Keep the world from the previous run instead of generating a new one.

.PARAMETER Memory
    Heap size passed to -Xms and -Xmx. Default 3G.

.PARAMETER JvmArgs
    Extra JVM arguments for the test server, for example '-Dsome.switch=false'.

.PARAMETER TimeoutSeconds
    Longest the whole run may take before the server is killed. Default 900.

.PARAMETER StallPolls
    How many 15-second checks in a row with no new log output count as a
    stall. Default 4 (60 seconds); some tests are quiet for a while.

.PARAMETER Java
    Java executable to use. Default: a JDK 25+ from JAVA_HOME, PATH or the installed JDKs (scripts\lib\JavaHome.ps1).

.EXAMPLE
    PS> .\scripts\game-test.ps1
    Runs every test.

.EXAMPLE
    PS> .\scripts\game-test.ps1 -Tests 'silkpaper:poi_*'
    Runs only the villager POI tests.

.EXAMPLE
    PS> .\scripts\game-test.ps1 -Tests 'silkpaper:hopper*' -KeepWorld
    Reruns one test without regenerating the world.
#>
[CmdletBinding()]
param(
    [string]$Tests = '*:*',
    [string]$ServerDir,
    [switch]$KeepWorld,
    [string]$Memory = '3G',
    [string[]]$JvmArgs = @(),
    [int]$TimeoutSeconds = 900,
    [ValidateRange(1, 100)]
    [int]$StallPolls = 4,
    [string]$Java = ''
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'lib\JavaHome.ps1')
if (-not $Java) { $Java = Use-SilkPaperJdk }
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$deploy = Join-Path $root 'deploy'
$packs = Join-Path $root 'gametests\packs'
if (-not $ServerDir) { $ServerDir = Join-Path $deploy 'game-test' }
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
    if (-not (Test-Path $packs)) { throw "No test datapacks in $packs." }

    New-Item -ItemType Directory -Path $ServerDir -Force | Out-Null
    $world = Join-Path $ServerDir 'world'
    # The world is generated output of the previous run.
    if (-not $KeepWorld -and (Test-Path $world)) { Remove-Item -LiteralPath $world -Recurse -Force }
    Set-Content -Path (Join-Path $ServerDir 'eula.txt') -Value 'eula=true' -Encoding ASCII
    # A flat world with nothing spawning, so every run starts from the same blocks and entities.
    @(
        'level-type=minecraft\:flat', 'generate-structures=false', 'online-mode=false', 'server-port=25599',
        'spawn-protection=0', 'view-distance=4', 'simulation-distance=4', 'enable-query=false',
        'spawn-monsters=false', 'spawn-animals=false', 'max-players=4',
        # Vanilla's default flat layers; an empty generator-settings fails to parse.
        'generator-settings={"layers"\:[{"block"\:"minecraft\:bedrock","height"\:1},{"block"\:"minecraft\:dirt","height"\:2},{"block"\:"minecraft\:grass_block","height"\:1}],"biome"\:"minecraft\:plains"}'
    ) | Set-Content -Path (Join-Path $ServerDir 'server.properties') -Encoding ASCII

    $datapacks = Join-Path $world 'datapacks'
    New-Item -ItemType Directory -Path $datapacks -Force | Out-Null
    Get-ChildItem -Path $packs | Where-Object { $_.PSIsContainer -or $_.Extension -eq '.zip' } | ForEach-Object {
        $target = Join-Path $datapacks $_.Name
        if (Test-Path $target) { Remove-Item -LiteralPath $target -Recurse -Force }
        Copy-Item -LiteralPath $_.FullName -Destination $target -Recurse
    }

    $crashDir = Join-Path $ServerDir 'crash-reports'
    $crashesBefore = @(Get-ChildItem -Path $crashDir -File -ErrorAction SilentlyContinue).Count
    $log = Join-Path $ServerDir 'game-test.log'
    $report = Join-Path $ServerDir 'report.xml'
    $result = Join-Path $ServerDir 'result.txt'
    Remove-Item -LiteralPath $log, "$log.err", $report, $result -Force -ErrorAction SilentlyContinue

    $props = @(
        "-Dsilkpaper.gametest=$Tests", "-Dsilkpaper.gametest.report=$report", "-Dsilkpaper.gametest.result=$result",
        '-Dpaper.disableMigrationDelay=true', '-Ddisable.watchdog=true'
    ) + $JvmArgs
    $propArgs = ($props | ForEach-Object { "`"$_`"" }) -join ' '

    # cmd.exe sends java's output straight to the log files.
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = 'cmd.exe'
    $psi.Arguments = "/d /s /c `"`"$javaExe`" -Xms$Memory -Xmx$Memory $propArgs -jar `"$($jar.FullName)`" --nogui > `"$log`" 2> `"$log.err`"`""
    $psi.WorkingDirectory = $ServerDir
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    Write-Host "> (in $ServerDir) $Java -jar $($jar.Name) --nogui   tests: $Tests" -ForegroundColor Cyan
    $process = [System.Diagnostics.Process]::Start($psi)
    $started = Get-Date

    $state = 'timed out'
    $deadline = $started.AddSeconds($TimeoutSeconds)
    $lastLength = -1
    $quiet = 0
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Seconds $pollSeconds
        if ($process.HasExited) { $state = 'exited'; break }
        $length = if (Test-Path $log) { (Get-Item $log).Length } else { 0 }
        if ($length -eq $lastLength) { $quiet++ } else { $quiet = 0; $lastLength = $length }
        if ($quiet -ge $StallPolls) { $state = 'stalled'; break }
        $passed = @(Select-String -Path $log -Pattern '\[SilkGameTests\] PASS' -ErrorAction SilentlyContinue).Count
        $failed = @(Select-String -Path $log -Pattern '\[SilkGameTests\] FAIL' -ErrorAction SilentlyContinue).Count
        $lastLine = if (Test-Path $log) { Get-Content $log -Tail 1 } else { '' }
        Write-Host "  [$([int]((Get-Date) - $started).TotalSeconds)s] passed $passed, failed $failed | $lastLine"
    }

    if (-not $process.HasExited) {
        $jstack = Get-Command jstack -ErrorAction SilentlyContinue
        $javaProcess = Get-JavaChild $process.Id
        if ($jstack -and $javaProcess) {
            $dump = Join-Path (Split-Path $ServerDir -Parent) ("game-test-threads-{0:yyyyMMdd-HHmmss}.txt" -f (Get-Date))
            & $jstack.Source $javaProcess.ProcessId > $dump 2>&1
            Write-Host "Thread dump: $dump"
        }
        Write-Warning "Server $state; killing it."
        Stop-Server
    }

    $passLines = @(Select-String -Path $log -Pattern '\[SilkGameTests\] PASS' -ErrorAction SilentlyContinue)
    $failLines = @(Select-String -Path $log -Pattern '\[SilkGameTests\] (FAIL|selector)' -ErrorAction SilentlyContinue | ForEach-Object { $_.Line })
    $summary = Select-String -Path $log -Pattern '\[SilkGameTests\] =====' -ErrorAction SilentlyContinue | Select-Object -Last 1
    $failedRequired = if (Test-Path $result) { [int](Get-Content $result -Raw).Trim() } else { $null }
    # ERROR lines, plus exceptions that the server logs at WARN, as boot-test checks them.
    $problems = @(Select-String -Path $log -Pattern '^\[[0-9:]+ ERROR\]|Unexpected exception|^\S*(Exception|Error)(:|$)|^Caused by:' -ErrorAction SilentlyContinue | ForEach-Object { $_.Line })
    $newCrashes = @(Get-ChildItem -Path $crashDir -File -ErrorAction SilentlyContinue).Count - $crashesBefore

    Write-Host ''
    Write-Host "Ended: $state   Passed: $($passLines.Count)   Failed: $($failLines.Count)   Problem lines: $($problems.Count)   New crash reports: $newCrashes"
    if ($summary) { Write-Host "  $($summary.Line)" }
    foreach ($line in $failLines) { Write-Host "  $line" -ForegroundColor Red }
    foreach ($line in $problems | Select-Object -First 30) { Write-Host "  $line" -ForegroundColor Red }
    Write-Host "Log: $log"
    if (Test-Path $report) { Write-Host "Report: $report" }

    if ($null -eq $failedRequired) {
        Write-Host 'Game tests did not finish (no result file).' -ForegroundColor Red
        exit 1
    }
    if ($failedRequired -gt 0) {
        Write-Host "$failedRequired required game test(s) failed." -ForegroundColor Red
        exit 1
    }
    if ($problems.Count -gt 0 -or $newCrashes -gt 0) {
        Write-Host 'The tests passed, but the server logged errors or crashed.' -ForegroundColor Red
        exit 1
    }
    Write-Host 'All required game tests passed.' -ForegroundColor Green
    exit 0
} catch {
    Stop-Server
    Write-Error $_
    exit 1
}
