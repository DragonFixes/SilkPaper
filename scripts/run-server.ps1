#Requires -Version 5.1

<#
.SYNOPSIS
    Runs the deployed Silkard jar as a local test server.

.DESCRIPTION
    Starts the newest *.jar in <repo>\deploy with java, using a separate server
    directory (default <repo>\deploy\server) as the working directory so
    worlds, configs, mods, plugins and logs stay out of the deploy folder.
    Fabric mods go in <server>\mods and Bukkit plugins in <server>\plugins.
    The server runs without its GUI unless -Gui is passed. eula.txt is written
    only when -AcceptEula is passed. Silkard needs Java 25 or newer.

.PARAMETER Memory
    Heap size passed to -Xms and -Xmx, e.g. 4G or 8192M. Default 4G.

.PARAMETER Gui
    Show the vanilla server GUI. Without it, --nogui is passed.

.PARAMETER ServerDir
    Working directory for the server. Default <repo>\deploy\server.

.PARAMETER AcceptEula
    Write eula=true to eula.txt in the server directory, accepting the
    Minecraft EULA (https://aka.ms/MinecraftEULA).

.PARAMETER Java
    Java executable to use. Default: a JDK 25+ from JAVA_HOME, PATH or the installed JDKs (scripts\lib\JavaHome.ps1).

.PARAMETER DryRun
    Print the java command without starting the server.

.EXAMPLE
    PS> .\scripts\run-server.ps1 -AcceptEula
    First run: accepts the EULA and starts the server with 4G of heap.

.EXAMPLE
    PS> .\scripts\run-server.ps1 -Memory 8G
    Starts the server with 8G of heap.

.EXAMPLE
    PS> .\scripts\run-server.ps1 -ServerDir C:\mc\test -Gui
    Starts in another folder with the server GUI.
#>
[CmdletBinding()]
param(
    [string]$Memory = '4G',
    [switch]$Gui,
    [string]$ServerDir,
    [switch]$AcceptEula,
    [string]$Java = '',
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'lib\JavaHome.ps1')
if (-not $Java) { $Java = Use-SilkPaperJdk }
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$deploy = Join-Path $root 'deploy'
if (-not $ServerDir) { $ServerDir = Join-Path $deploy 'server' }

try {
    $jar = Get-ChildItem -Path $deploy -Filter '*.jar' -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $jar) { throw "No jar in $deploy. Run scripts\build-deploy first." }

    if (-not (Get-Command $Java -ErrorAction SilentlyContinue)) { throw "Java executable '$Java' not found." }
    & $Java -version

    if (-not (Test-Path $ServerDir)) {
        Write-Host "Creating $ServerDir"
        if (-not $DryRun) { New-Item -ItemType Directory -Path $ServerDir -Force | Out-Null }
    }
    if ($AcceptEula) {
        Write-Host 'Writing eula=true'
        if (-not $DryRun) { Set-Content -Path (Join-Path $ServerDir 'eula.txt') -Value 'eula=true' -Encoding ASCII }
    }

    $javaArgs = @("-Xms$Memory", "-Xmx$Memory", '-jar', $jar.FullName)
    if (-not $Gui) { $javaArgs += '--nogui' }

    Write-Host "> (in $ServerDir) $Java $($javaArgs -join ' ')" -ForegroundColor Cyan
    if ($DryRun) { exit 0 }

    Push-Location $ServerDir
    try {
        & $Java @javaArgs
        exit $LASTEXITCODE
    } finally {
        Pop-Location
    }
} catch {
    Write-Error $_
    exit 1
}
