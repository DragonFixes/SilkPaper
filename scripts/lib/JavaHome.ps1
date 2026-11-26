#Requires -Version 5.1

<#
.SYNOPSIS
    Finds a JDK new enough to build and run SilkPaper. Dot-sourced by the scripts in scripts\.

.DESCRIPTION
    Defines Find-SilkPaperJdk and Use-SilkPaperJdk. The build (fabric-loom) and the server both
    need Java 25 or newer, while the "java" on PATH and JAVA_HOME are often an older JDK, or
    JAVA_HOME is not set at all when a script is started from Explorer or an IDE.

    Find-SilkPaperJdk returns the home folder of the first JDK whose major version is at least
    the minimum, checking in order:
      1. JAVA_HOME
      2. the "java" on PATH
      3. JDKs installed under {USERPROFILE}\.jdks (IntelliJ downloads), Program Files\Java,
         Eclipse Adoptium, Zulu, Microsoft, Amazon Corretto, BellSoft and Semeru.
    Among the installed JDKs it prefers exactly the minimum major version (the version the
    project targets), newest update first, and falls back to the lowest newer major.

    Use-SilkPaperJdk sets JAVA_HOME for the current process (and its children, such as
    gradlew.bat) to that JDK and returns the path of its java.exe.

    This file is a library, not a runnable script, so it has no .bat launcher.

.EXAMPLE
    PS> . (Join-Path $PSScriptRoot 'lib\JavaHome.ps1')
    PS> $java = Use-SilkPaperJdk
#>

$script:SilkPaperMinJava = 25

function Get-JdkMajorVersion([string]$JdkHome) {
    if (-not $JdkHome) { return $null }
    if (-not (Test-Path -LiteralPath (Join-Path $JdkHome 'bin\java.exe'))) { return $null }
    $release = Join-Path $JdkHome 'release'
    if (-not (Test-Path -LiteralPath $release)) { return $null }
    $line = Select-String -LiteralPath $release -Pattern '^JAVA_VERSION="([^"]+)"' | Select-Object -First 1
    if (-not $line) { return $null }
    $version = $line.Matches[0].Groups[1].Value
    # Java 8 and older report "1.8.0_x"; 9+ report "<major>.<minor>...".
    if ($version -match '^1\.(\d+)') { return [int]$Matches[1] }
    if ($version -match '^(\d+)') { return [int]$Matches[1] }
    return $null
}

function Get-JdkUpdateVersion([string]$JdkHome) {
    $line = Select-String -LiteralPath (Join-Path $JdkHome 'release') -Pattern '^JAVA_VERSION="([^"]+)"' |
        Select-Object -First 1
    $parts = @($line.Matches[0].Groups[1].Value -split '[^\d]+' | Where-Object { $_ } | ForEach-Object { [int]$_ })
    while ($parts.Count -lt 4) { $parts += 0 }
    return [version]::new($parts[0], $parts[1], $parts[2], $parts[3])
}

function Find-SilkPaperJdk([int]$MinMajor = $script:SilkPaperMinJava) {
    if ((Get-JdkMajorVersion $env:JAVA_HOME) -ge $MinMajor) {
        return (Resolve-Path -LiteralPath $env:JAVA_HOME).Path
    }

    $onPath = Get-Command java.exe -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($onPath) {
        $pathHome = Split-Path -Parent (Split-Path -Parent $onPath.Source)
        if ((Get-JdkMajorVersion $pathHome) -ge $MinMajor) { return $pathHome }
    }

    $roots = @(
        (Join-Path $env:USERPROFILE '.jdks'),
        (Join-Path $env:ProgramFiles 'Java'),
        (Join-Path $env:ProgramFiles 'Eclipse Adoptium'),
        (Join-Path $env:ProgramFiles 'Zulu'),
        (Join-Path $env:ProgramFiles 'Microsoft'),
        (Join-Path $env:ProgramFiles 'Amazon Corretto'),
        (Join-Path $env:ProgramFiles 'BellSoft'),
        (Join-Path $env:ProgramFiles 'Semeru')
    )
    $candidates = foreach ($root in $roots) {
        foreach ($dir in @(Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue)) {
            $major = Get-JdkMajorVersion $dir.FullName
            if ($major -ge $MinMajor -and (Test-Path -LiteralPath (Join-Path $dir.FullName 'bin\javac.exe'))) {
                [pscustomobject]@{ Home = $dir.FullName; Major = $major; Version = Get-JdkUpdateVersion $dir.FullName }
            }
        }
    }
    $best = @($candidates) | Sort-Object @{ Expression = 'Major'; Ascending = $true },
        @{ Expression = 'Version'; Descending = $true } | Select-Object -First 1
    if ($best) { return $best.Home }

    throw ("No JDK $MinMajor or newer found. Install one (for example Temurin or Azul Zulu $MinMajor) " +
        "or set JAVA_HOME to it. Checked JAVA_HOME, the java on PATH, and $($roots -join ', ').")
}

function Use-SilkPaperJdk([int]$MinMajor = $script:SilkPaperMinJava) {
    $jdk = Find-SilkPaperJdk $MinMajor
    if ($env:JAVA_HOME -ne $jdk) {
        Write-Host "Using JDK $jdk" -ForegroundColor DarkGray
        $env:JAVA_HOME = $jdk
    }
    return (Join-Path $jdk 'bin\java.exe')
}
