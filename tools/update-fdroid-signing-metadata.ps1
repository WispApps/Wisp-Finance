param(
    [Parameter(Mandatory = $true)][string]$Apk,
    [string]$Metadata = (Join-Path $PSScriptRoot "..\fdroid\metadata\com.wisp.finance.yml"),
    [string]$BuildTools = (Join-Path $env:LOCALAPPDATA "Android\Sdk\build-tools\34.0.0")
)
$ErrorActionPreference = "Stop"

$apksigner = Join-Path $BuildTools "apksigner.bat"
if (-not (Test-Path $Apk)) { throw "APK not found: $Apk" }
if (-not (Test-Path $apksigner)) { throw "apksigner not found: $apksigner" }

$certOutput = (& $apksigner verify --print-certs $Apk 2>&1 | Out-String)
$match = [regex]::Match($certOutput, "Signer #1 certificate SHA-256 digest: ([0-9A-Fa-f:]+)")
if (-not $match.Success) { throw "Could not read the APK certificate SHA-256 digest.`n$certOutput" }
$digest = $match.Groups[1].Value.ToUpperInvariant()

$content = Get-Content -Raw -LiteralPath $Metadata
if (-not $content.Contains("__REPLACE_WITH_SHA256_CERTIFICATE_DIGEST__")) {
    throw "Metadata does not contain the expected signing-key placeholder: $Metadata"
}
$content = $content.Replace("__REPLACE_WITH_SHA256_CERTIFICATE_DIGEST__", $digest)
[IO.File]::WriteAllText((Resolve-Path $Metadata), $content, (New-Object Text.UTF8Encoding($false)))
Write-Host "AllowedAPKSigningKeys set to:" $digest
Write-Host "Metadata:" (Resolve-Path $Metadata)
