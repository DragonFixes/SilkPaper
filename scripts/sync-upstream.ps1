#Requires -Version 5.1

<#
.SYNOPSIS
    Fetches upstream Silkard and reports how far this fork's branches have drifted.

.DESCRIPTION
    Fetches the "upstream" (MohistMC/Silkard) and "origin" remotes, fast-forwards
    the local 26.3 mirror branch to upstream/26.3 when it is not checked out and
    can fast-forward, then prints ahead/behind counts for develop and 26.3
    against upstream/26.3. With -Merge, merges the given ref into the current
    branch after a confirmation prompt. Nothing is ever pushed.

    The Paper/Purpur/Leaf API is synced separately by scripts\sync-leaf-api.

.PARAMETER Merge
    A ref to merge into the current branch after fetching, e.g. upstream/26.3.

.PARAMETER NoFetch
    Skip fetching and report against the remote-tracking refs already present.

.PARAMETER Force
    Skip the confirmation prompt before merging.

.PARAMETER DryRun
    Print the fetch and merge commands without running them; the drift report
    uses the refs already present.

.EXAMPLE
    PS> .\scripts\sync-upstream.ps1
    Fetches, updates the 26.3 mirror and prints the drift report.

.EXAMPLE
    PS> .\scripts\sync-upstream.ps1 -Merge upstream/26.3
    Fetches, reports, then merges upstream Silkard into the current branch after confirming.

.EXAMPLE
    PS> .\scripts\sync-upstream.ps1 -Merge upstream/26.3 -DryRun
    Shows what would be fetched and merged.
#>
[CmdletBinding()]
param(
    [string]$Merge,
    [switch]$NoFetch,
    [switch]$Force,
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$mirror = '26.3'
$upstreamRef = "upstream/$mirror"

function Invoke-Git([string[]]$GitArgs) {
    & git -C $root @GitArgs
    if ($LASTEXITCODE -ne 0) { throw "git $($GitArgs -join ' ') failed with exit code $LASTEXITCODE" }
}

function Test-Ref([string]$Ref) {
    & git -C $root rev-parse --verify --quiet "$Ref^{commit}" *> $null
    return $LASTEXITCODE -eq 0
}

function Write-Drift([string]$Local, [string]$Remote) {
    if (-not (Test-Ref $Local)) { Write-Host ("{0,-10} vs {1,-16} {2}" -f $Local, $Remote, '(local branch missing)'); return }
    if (-not (Test-Ref $Remote)) { Write-Host ("{0,-10} vs {1,-16} {2}" -f $Local, $Remote, '(remote ref missing)'); return }
    $counts = (& git -C $root rev-list --left-right --count "$Local...$Remote") -split '\s+'
    Write-Host ("{0,-10} vs {1,-16} ahead {2,5}  behind {3,5}" -f $Local, $Remote, $counts[0], $counts[1])
}

try {
    $branch = (& git -C $root rev-parse --abbrev-ref HEAD).Trim()

    if (-not $NoFetch) {
        foreach ($remote in 'upstream', 'origin') {
            Write-Host "> git fetch $remote" -ForegroundColor Cyan
            if (-not $DryRun) { Invoke-Git @('fetch', '--prune', $remote) }
        }
    }

    # The mirror only ever fast-forwards; a diverged mirror is reported, not reset.
    if ($branch -ne $mirror -and (Test-Ref $mirror) -and (Test-Ref $upstreamRef)) {
        $behind = [int](& git -C $root rev-list --count "$mirror..$upstreamRef")
        & git -C $root merge-base --is-ancestor $mirror $upstreamRef
        $canFastForward = $LASTEXITCODE -eq 0
        if ($behind -eq 0) {
            # Already current; nothing to move.
        } elseif ($canFastForward) {
            Write-Host "> git branch -f $mirror $upstreamRef   (fast-forward)" -ForegroundColor Cyan
            if (-not $DryRun) { Invoke-Git @('update-ref', "refs/heads/$mirror", $upstreamRef) }
        } else {
            Write-Warning "$mirror has commits that are not on $upstreamRef; leaving it alone."
        }
    }

    Write-Host ''
    Write-Drift 'develop' $upstreamRef
    Write-Drift $mirror $upstreamRef
    Write-Host ''

    if ($Merge) {
        if (-not (Test-Ref $Merge)) { throw "Ref '$Merge' does not exist." }
        $dirty = & git -C $root status --porcelain --untracked-files=no
        if ($dirty -and -not $DryRun) { throw 'The working tree has uncommitted changes; commit or stash them before merging.' }
        if ($dirty) { Write-Warning 'The working tree has uncommitted changes; a real merge would refuse to start.' }

        Write-Host "> git merge $Merge   (into $branch)" -ForegroundColor Cyan
        if ($DryRun) { exit 0 }
        if (-not $Force) {
            $answer = Read-Host "Merge $Merge into $branch? [y/N]"
            if ($answer -notmatch '^(y|yes)$') { Write-Host 'Merge cancelled.'; exit 0 }
        }
        Invoke-Git @('merge', '--no-edit', $Merge)
        Write-Host "Merged $Merge into $branch. Run scripts\build-deploy next." -ForegroundColor Green
    }
    exit 0
} catch {
    Write-Error $_
    exit 1
}
