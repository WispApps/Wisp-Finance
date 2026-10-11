param(
    [switch]$SkipBuild,
    [string]$KeyStore = (Join-Path $HOME 'WispApps-signing\wisp-finance-release.p12'),
    [string]$Alias = 'wisp-finance',
    [string]$BuildTools = (Join-Path $env:LOCALAPPDATA 'Android\Sdk\build-tools\34.0.0')
)
$ErrorActionPreference = 'Stop'
$ProjectDir = Split-Path -Parent $PSScriptRoot
$UnsignedApk = Join-Path $ProjectDir 'app\build\outputs\apk\release\app-release-unsigned.apk'
$SignedApk = Join-Path $ProjectDir 'app\build\outputs\apk\release\wisp-finance-release-signed.apk'
$ApkSigner = Join-Path $BuildTools 'apksigner.bat'
if (-not (Test-Path -LiteralPath $KeyStore -PathType Leaf)) { throw "Release keystore not found: $KeyStore" }
if (-not (Test-Path -LiteralPath $ApkSigner -PathType Leaf)) { throw "apksigner not found: $ApkSigner" }

Push-Location $ProjectDir
try {
    if (-not $SkipBuild) {
        & .\gradlew.bat :app:assembleRelease
        if ($LASTEXITCODE -ne 0) { throw "Gradle failed ($LASTEXITCODE). No APK was signed." }
    }
    if (-not (Test-Path -LiteralPath $UnsignedApk -PathType Leaf)) { throw "Unsigned APK not found: $UnsignedApk" }

    # Preserve any previously verified APK if signing or verification fails.
    $Candidate = Join-Path (Split-Path -Parent $SignedApk) ('wisp-candidate-' + [guid]::NewGuid().ToString('N') + '.apk')
    try {
        # apksigner asks for the password; it is not placed in command arguments.
        & $ApkSigner sign --ks $KeyStore --ks-key-alias $Alias --v4-signing-enabled false --out $Candidate $UnsignedApk
        if ($LASTEXITCODE -ne 0) { throw "Signing failed ($LASTEXITCODE). Previous signed APK was preserved." }
        & $ApkSigner verify --verbose --print-certs $Candidate
        if ($LASTEXITCODE -ne 0) { throw "Signature verification failed ($LASTEXITCODE). Previous signed APK was preserved." }
        Move-Item -LiteralPath $Candidate -Destination $SignedApk -Force
        if (Test-Path -LiteralPath "$SignedApk.idsig" -PathType Leaf) {
            Remove-Item -LiteralPath "$SignedApk.idsig"
        }
        Write-Host 'Verified signed APK:' $SignedApk
    } finally {
        if (Test-Path -LiteralPath $Candidate -PathType Leaf) {
            Remove-Item -LiteralPath $Candidate
        }
    }
} finally {
    Pop-Location
}
