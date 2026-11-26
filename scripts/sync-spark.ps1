#Requires -Version 5.1
<#
.SYNOPSIS
    Builds SilkPaper's bundled spark from the spark fork and vendors it into libs\maven.

.DESCRIPTION
    SilkPaper bundles spark the way Paper does (io.papermc.paper.SparksFly), using the
    DragonFixes spark fork (https://github.com/DragonFixes/spark), which adds native and Java
    heap leak detection. The fork is not published to a maven repository, so its jars are kept
    in a file-based maven repository inside this repository, libs\maven, which
    projects\silkard\build.gradle reads.

    This script:
      1. builds :spark-paper:shadowJar and :spark-api:jar in the fork checkout,
      2. replaces libs\maven\me\lucko\spark-paper and spark-api with the new jars and a POM
         without dependencies (spark-paper is a shaded jar),
      3. writes the versions to spark_paper_version / spark_api_version in gradle.properties.

    Versions are the fork's own version plus its short commit, for example
    1.10.235-04badc0 and 0.1-04badc0, so each vendored build is traceable to a fork commit.

.PARAMETER SparkDir
    The spark fork checkout. Default: $env:SILKPAPER_SPARK_DIR, else ..\..\Fixes\Spark beside
    this repository (C:\Development\Java\Fixes\Spark for C:\Development\Java\ServerSoftware\SilkPaper).

.PARAMETER JavaHome
    JDK used for the fork's Gradle build. Default: a JDK 25+ from JAVA_HOME, PATH or the
    installed JDKs (scripts\lib\JavaHome.ps1).

.PARAMETER AllowDirty
    Build even when spark-api, spark-common or spark-paper in the fork have uncommitted
    changes. The version then ends in -dirty.

.PARAMETER SkipBuild
    Vendor the jars already in the fork's build\libs folders without running Gradle.

.PARAMETER DryRun
    Show what would be built and replaced without changing anything.

.PARAMETER Force
    Do not ask before replacing the vendored jars.

.EXAMPLE
    scripts\sync-spark.bat

.EXAMPLE
    scripts\sync-spark.bat -SparkDir D:\src\spark -Force

.EXAMPLE
    scripts\sync-spark.bat -DryRun
#>
[CmdletBinding()]
param(
    [string] $SparkDir,
    [string] $JavaHome,
    [switch] $AllowDirty,
    [switch] $SkipBuild,
    [switch] $DryRun,
    [switch] $Force
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$mavenDir = Join-Path $repoRoot 'libs\maven\me\lucko'
$propsFile = Join-Path $repoRoot 'gradle.properties'

function Invoke-Git {
    $out = & git -C $SparkDir @args
    if ($LASTEXITCODE -ne 0) { throw "git $($args -join ' ') failed with exit code $LASTEXITCODE" }
    $out
}

function Remove-ToRecycleBin([string] $path) {
    Add-Type -AssemblyName Microsoft.VisualBasic
    [Microsoft.VisualBasic.FileIO.FileSystem]::DeleteDirectory($path, 'OnlyErrorDialogs', 'SendToRecycleBin')
}

function Write-Artifact([string] $artifactId, [string] $version, [string] $jar) {
    $dir = Join-Path $mavenDir "$artifactId"
    $versionDir = Join-Path $dir $version
    Write-Host "  $artifactId $version <- $jar"
    if ($DryRun) { return }
    if (Test-Path $dir) { Remove-ToRecycleBin $dir }
    New-Item -ItemType Directory -Force $versionDir | Out-Null
    Copy-Item $jar (Join-Path $versionDir "$artifactId-$version.jar")
    $pom = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>me.lucko</groupId>
  <artifactId>$artifactId</artifactId>
  <version>$version</version>
  <packaging>jar</packaging>
</project>
"@
    [IO.File]::WriteAllText((Join-Path $versionDir "$artifactId-$version.pom"), $pom.Replace("`r`n", "`n") + "`n")
}

function Set-Property([string] $text, [string] $key, [string] $value) {
    $pattern = "(?m)^$([regex]::Escape($key))=.*$"
    if ($text -match $pattern) { return [regex]::Replace($text, $pattern, "$key=$value") }
    throw "$key is missing from $propsFile"
}

try {
    if (-not $SparkDir) { $SparkDir = $env:SILKPAPER_SPARK_DIR }
    if (-not $SparkDir) { $SparkDir = Join-Path $repoRoot '..\..\Fixes\Spark' }
    if (-not (Test-Path (Join-Path $SparkDir 'spark-paper'))) { throw "No spark checkout at $SparkDir (pass -SparkDir)." }
    $SparkDir = (Resolve-Path $SparkDir).Path

    $commit = (Invoke-Git rev-parse --short=7 HEAD | Select-Object -First 1).Trim()
    # The vendored classes come from these modules only; edits elsewhere in the checkout (other
    # platforms, build tweaks for other consumers of the fork) are reported but do not mark the
    # build dirty.
    $dirty = [bool](Invoke-Git status --porcelain --untracked-files=no -- spark-api spark-common spark-paper)
    $other = Invoke-Git status --porcelain --untracked-files=no
    if ($other -and -not $dirty) { Write-Warning "Uncommitted changes outside the bundled modules:`n$($other -join "`n")" }
    if ($dirty -and -not $AllowDirty) { throw "$SparkDir has uncommitted changes in spark-api, spark-common or spark-paper; commit them or pass -AllowDirty." }
    $suffix = if ($dirty) { "$commit-dirty" } else { $commit }
    Write-Host "spark fork: $SparkDir at $suffix"

    if (-not $SkipBuild) {
        Write-Host 'Building :spark-paper:shadowJar :spark-api:jar'
        if (-not $DryRun) {
            if (-not $JavaHome) { . (Join-Path $PSScriptRoot 'lib\JavaHome.ps1'); $JavaHome = Find-SilkPaperJdk }
            $env:JAVA_HOME = $JavaHome
            Push-Location $SparkDir
            try {
                & .\gradlew.bat :spark-paper:shadowJar :spark-api:jar --console=plain
                if ($LASTEXITCODE -ne 0) { throw "spark build failed with exit code $LASTEXITCODE" }
            } finally { Pop-Location }
        }
    }

    $paperJar = Get-ChildItem (Join-Path $SparkDir 'spark-paper\build\libs') -Filter 'spark-*-paper.jar' -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    $apiJar = Get-ChildItem (Join-Path $SparkDir 'spark-api\build\libs') -Filter 'spark-api-*.jar' -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch '-(sources|javadoc)\.jar$' } |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $paperJar -or -not $apiJar) {
        if ($DryRun) { Write-Host 'Jars not built yet; a real run would build them.'; exit 0 }
        throw 'Built jars not found in spark-paper\build\libs and spark-api\build\libs.'
    }

    $paperBase = [regex]::Match($paperJar.Name, '^spark-(.+)-paper\.jar$').Groups[1].Value
    $apiBase = [regex]::Match($apiJar.Name, '^spark-api-(.+?)(-SNAPSHOT)?\.jar$').Groups[1].Value
    $paperVersion = "$paperBase-$suffix"
    $apiVersion = "$apiBase-$suffix"

    if (-not $DryRun -and -not $Force) {
        $answer = Read-Host "Replace the vendored spark with spark-paper $paperVersion and spark-api $apiVersion? [y/N]"
        if ($answer -notmatch '^(y|yes)$') { Write-Host 'Cancelled.'; exit 1 }
    }

    Write-Host "Vendoring into $mavenDir"
    Write-Artifact 'spark-paper' $paperVersion $paperJar.FullName
    Write-Artifact 'spark-api' $apiVersion $apiJar.FullName

    Write-Host "Setting spark_paper_version=$paperVersion, spark_api_version=$apiVersion in gradle.properties"
    if (-not $DryRun) {
        $text = [IO.File]::ReadAllText($propsFile)
        $text = Set-Property $text 'spark_paper_version' $paperVersion
        $text = Set-Property $text 'spark_api_version' $apiVersion
        [IO.File]::WriteAllText($propsFile, $text)
    }
    Write-Host 'Done. Run scripts\build-deploy.bat and scripts\boot-test.bat -Commands "spark tps" next.'
    exit 0
} catch {
    Write-Error $_
    exit 1
}
