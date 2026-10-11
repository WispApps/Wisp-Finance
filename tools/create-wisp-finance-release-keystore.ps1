$ErrorActionPreference = "Stop"

$SigningDir = Join-Path $HOME "WispApps-signing"
$KeyStore = Join-Path $SigningDir "wisp-finance-release.p12"
$Alias = "wisp-finance"

New-Item -ItemType Directory -Force -Path $SigningDir | Out-Null

if (Test-Path $KeyStore) {
    Write-Host "Keystore already exists:" $KeyStore
    exit 0
}

$keytool = $null
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\keytool.exe"))) {
    $keytool = Join-Path $env:JAVA_HOME "bin\keytool.exe"
} else {
    $cmd = Get-Command keytool.exe -ErrorAction SilentlyContinue
    if ($cmd) { $keytool = $cmd.Source }
}

if (-not $keytool) {
    throw "keytool.exe not found. Install/configure JDK 21 and set JAVA_HOME."
}

$securePassword = Read-Host "Create password for Wisp Finance release key" -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try { $password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }

& $keytool -genkeypair `
  -v `
  -storetype PKCS12 `
  -keystore $KeyStore `
  -storepass $password `
  -keypass $password `
  -alias $Alias `
  -keyalg RSA `
  -keysize 3072 `
  -validity 10000 `
  -dname "CN=Wisp Finance, OU=WispApps, O=WispApps, L=Warsaw, C=PL"

Write-Host "Created release keystore:" $KeyStore
Write-Host "IMPORTANT: Back up this file and never commit it to Git."
