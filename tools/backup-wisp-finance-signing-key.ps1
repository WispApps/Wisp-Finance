param(
    [string]$Source = (Join-Path $HOME "WispApps-signing\wisp-finance-release.p12"),
    [Parameter(Mandatory = $true)][string]$Destination
)
$ErrorActionPreference = "Stop"

if (-not (Test-Path $Source)) { throw "Signing keystore not found: $Source" }
$destinationDir = Split-Path -Parent $Destination
New-Item -ItemType Directory -Force -Path $destinationDir | Out-Null
Copy-Item -LiteralPath $Source -Destination $Destination -Force
$hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $Destination).Hash
Write-Host "Encrypted-key backup written to:" $Destination
Write-Host "SHA-256 (record this separately):" $hash
Write-Host "Store the backup and password in separate secure locations. Never commit either to Git."
