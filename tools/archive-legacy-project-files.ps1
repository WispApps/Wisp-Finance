$ErrorActionPreference = 'Stop'
$ProjectDir = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path -LiteralPath (Join-Path $ProjectDir 'app\build.gradle.kts'))) {
    throw 'Run this script from the Wisp Finance project tools directory.'
}

# Only these known, superseded delivery files are moved. No recursive deletion.
$Names = @(
    'BUILD22-UI-README.txt',
    'BUILD22-UI-SHA256SUMS.txt',
    'BUILD23-E2EE-NEXTCLOUD-README.txt',
    'BUILD23-SHA256SUMS.txt',
    'README-FIX.txt',
    'README-INSTALL.txt',
    'SHA256SUMS.txt',
    'install-wisp-finance-icon.ps1'
)
$Destination = Join-Path (Split-Path -Parent $ProjectDir) (
    'WispFinance-legacy-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 6)
)
$Existing = @($Names | Where-Object { Test-Path -LiteralPath (Join-Path $ProjectDir $_) -PathType Leaf })
if ($Existing.Count -eq 0) {
    Write-Host 'No legacy delivery files found. Nothing moved.'
    return
}
New-Item -ItemType Directory -Path $Destination | Out-Null
foreach ($Name in $Existing) {
    Move-Item -LiteralPath (Join-Path $ProjectDir $Name) -Destination (Join-Path $Destination $Name)
    Write-Host 'Archived:' $Name
}
Write-Host 'Recoverable archive directory:' $Destination
Write-Host 'No source, APK, signing key, cache or device data was deleted.'
