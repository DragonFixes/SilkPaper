#Requires -Version 5.1
<#
.SYNOPSIS
    Copies the Paper + Purpur + Leaf API from a patched Leaf checkout into Silkard.

.DESCRIPTION
    Leaf (https://github.com/Winds-Studio/Leaf) builds its API by applying the
    Purpur and Leaf API patches on top of paper-api with paperweight. Silkard
    vendors the result as plain source under projects/silkard/src/paper-api/java,
    the same way it vendors Bukkit and CraftBukkit.

    This script:
      1. optionally clones Leaf at -Ref and runs `gradlew applyAllPatches`
         (the API stages are all that is needed; a failure in the later
         vanilla-patching stages is reported but does not stop the sync),
      2. replaces projects/silkard/src/paper-api/java with
         paper-api/src/main/java, paper-api/src/generated/java and
         leaf-api/src/main/java from that checkout,
      3. re-applies Silkard's own API changes from patches/paper-api/*.patch
         with `git apply --3way`,
      4. writes the Leaf ref and Paper commit to paper-api-upstream.properties.

    Silkard-specific API changes belong in patches/paper-api, never directly in
    the vendored tree, so the next sync keeps them.

.PARAMETER LeafDir
    A Leaf checkout on which `gradlew applyAllPatches` has already run (its
    paper-api folder must exist). Ignored with -Clone.

.PARAMETER Clone
    Clone Leaf into a temporary folder at -Ref and apply its patches first.

.PARAMETER Ref
    Leaf branch, tag or commit to clone with -Clone. Default: ver/26.3.

.PARAMETER JavaHome
    JDK 25+ used for the Leaf build with -Clone. Default: $env:JAVA_HOME.

.PARAMETER SkipPatches
    Copy the upstream API only, without applying patches/paper-api.

.PARAMETER DryRun
    Show what would be copied and patched without changing anything.

.PARAMETER Force
    Do not ask before replacing projects/silkard/src/paper-api/java.

.EXAMPLE
    .\scripts\sync-leaf-api.ps1 -LeafDir C:\src\Leaf

.EXAMPLE
    .\scripts\sync-leaf-api.ps1 -Clone -Ref ver/26.3 -JavaHome "$env:USERPROFILE\.jdks\azul-25.0.5" -Force

.EXAMPLE
    .\scripts\sync-leaf-api.ps1 -LeafDir C:\src\Leaf -DryRun
#>
[CmdletBinding()]
param(
    [string] $LeafDir,
    [switch] $Clone,
    [string] $Ref = 'ver/26.3',
    [string] $JavaHome = $env:JAVA_HOME,
    [switch] $SkipPatches,
    [switch] $DryRun,
    [switch] $Force
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$target = Join-Path $repoRoot 'projects\silkard\src\paper-api\java'
$patchDir = Join-Path $repoRoot 'patches\paper-api'
$propsFile = Join-Path $repoRoot 'paper-api-upstream.properties'

function Invoke-Git {
    & git @args
    if ($LASTEXITCODE -ne 0) { throw "git $($args -join ' ') failed with exit code $LASTEXITCODE" }
}

try {
    if ($Clone) {
        $LeafDir = Join-Path ([IO.Path]::GetTempPath()) ("leaf-" + [Guid]::NewGuid().ToString('N').Substring(0, 8))
        Write-Host "Cloning Leaf $Ref into $LeafDir"
        if (-not $DryRun) {
            Invoke-Git -c core.longpaths=true clone --depth 1 --branch $Ref https://github.com/Winds-Studio/Leaf.git $LeafDir
            Push-Location $LeafDir
            try {
                if ($JavaHome) { $env:JAVA_HOME = $JavaHome }
                # paperweight commits inside its work trees; give it an identity and
                # keep git from garbage-collecting objects it still references.
                $env:GIT_CONFIG_COUNT = '4'
                $env:GIT_CONFIG_KEY_0 = 'core.longpaths'; $env:GIT_CONFIG_VALUE_0 = 'true'
                $env:GIT_CONFIG_KEY_1 = 'gc.auto';        $env:GIT_CONFIG_VALUE_1 = '0'
                $env:GIT_CONFIG_KEY_2 = 'user.name';      $env:GIT_CONFIG_VALUE_2 = 'sync-leaf-api'
                $env:GIT_CONFIG_KEY_3 = 'user.email';     $env:GIT_CONFIG_VALUE_3 = 'sync-leaf-api@localhost'
                & .\gradlew.bat applyAllPatches --console=plain
                if ($LASTEXITCODE -ne 0) {
                    Write-Warning "applyAllPatches exited with $LASTEXITCODE; continuing if the API stages finished."
                }
            } finally {
                Pop-Location
            }
        }
    }

    if (-not $LeafDir) { throw 'Pass -LeafDir <patched Leaf checkout> or -Clone.' }
    $LeafDir = (Resolve-Path $LeafDir).Path

    $sources = @(
        (Join-Path $LeafDir 'paper-api\src\main\java'),
        (Join-Path $LeafDir 'paper-api\src\generated\java'),
        (Join-Path $LeafDir 'leaf-api\src\main\java')
    )
    foreach ($src in $sources[0], $sources[2]) {
        if (-not (Test-Path $src)) { throw "Missing $src - has applyAllPatches run in $LeafDir?" }
    }

    $count = ($sources | Where-Object { Test-Path $_ } | ForEach-Object { Get-ChildItem $_ -Recurse -File } | Measure-Object).Count
    Write-Host "Replacing $target with $count files from $LeafDir"

    if ($DryRun) {
        Get-ChildItem $patchDir -Filter *.patch -ErrorAction SilentlyContinue | ForEach-Object { Write-Host "Would apply $($_.Name)" }
        exit 0
    }

    if ((Test-Path $target) -and -not $Force) {
        $answer = Read-Host "Replace the existing vendored API in $target? [y/N]"
        if ($answer -notmatch '^[yY]') { Write-Host 'Aborted.'; exit 1 }
    }

    if (Test-Path $target) { Remove-Item -Recurse -Force $target }
    New-Item -ItemType Directory -Force $target | Out-Null
    foreach ($src in $sources) {
        if (Test-Path $src) { Copy-Item -Path (Join-Path $src '*') -Destination $target -Recurse -Force }
    }

    if (-not $SkipPatches -and (Test-Path $patchDir)) {
        Push-Location $repoRoot
        try {
            foreach ($patch in Get-ChildItem $patchDir -Filter *.patch | Sort-Object Name) {
                Write-Host "Applying $($patch.Name)"
                & git apply --3way --whitespace=nowarn $patch.FullName
                if ($LASTEXITCODE -ne 0) { throw "Patch $($patch.Name) did not apply; resolve it and regenerate the patch." }
            }
        } finally {
            Pop-Location
        }
    }

    $paperCommit = ''
    $gp = Join-Path $LeafDir 'gradle.properties'
    if (Test-Path $gp) {
        $line = Select-String -Path $gp -Pattern '^paperCommit=(.+)$' | Select-Object -First 1
        if ($line) { $paperCommit = $line.Matches[0].Groups[1].Value.Trim() }
    }
    $leafCommit = (& git -C $LeafDir rev-parse HEAD 2>$null)
    @(
        "# Written by scripts/sync-leaf-api.ps1; records what src/paper-api/java was synced from.",
        "leafRef=$Ref",
        "leafCommit=$leafCommit",
        "paperCommit=$paperCommit"
    ) | Set-Content -Encoding UTF8 $propsFile

    Write-Host "Done. Leaf $leafCommit, Paper $paperCommit"
    exit 0
} catch {
    Write-Error $_
    exit 1
}
