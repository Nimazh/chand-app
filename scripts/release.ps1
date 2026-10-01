param(
    [switch]$CreateOnly,
    [switch]$ShowRecoveryPassword
)

$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
$signingDir = Join-Path $projectDir 'local-signing'
$keyPath = Join-Path $signingDir 'chand-release.p12'
$secretPath = Join-Path $signingDir 'password.dpapi'
$keytoolPath = Join-Path $env:JAVA_HOME 'bin\keytool.exe'

if (-not (Test-Path -LiteralPath $keytoolPath)) {
    throw 'JAVA_HOME must point to a JDK with keytool.exe.'
}

if ((Test-Path -LiteralPath $keyPath) -xor (Test-Path -LiteralPath $secretPath)) {
    throw 'Incomplete signing materials: both the keystore and encrypted password must be present.'
}

if (-not (Test-Path -LiteralPath $keyPath)) {
    New-Item -ItemType Directory -Path $signingDir -Force | Out-Null
    $password = [Convert]::ToBase64String(
        [Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
    )
    $securePassword = ConvertTo-SecureString $password -AsPlainText -Force
    ConvertFrom-SecureString $securePassword |
        Set-Content -LiteralPath $secretPath -NoNewline -Encoding UTF8
    try {
        $env:CHAND_KEYSTORE_PASSWORD = $password
        $env:CHAND_KEY_PASSWORD = $password
        & $keytoolPath -genkeypair -v -storetype PKCS12 -keystore $keyPath `
            -alias chand-release -keyalg RSA -keysize 4096 -validity 10000 `
            -dname 'CN=Chand, OU=Android, O=Chand, C=IR' `
            -storepass:env CHAND_KEYSTORE_PASSWORD -keypass:env CHAND_KEY_PASSWORD
        if ($LASTEXITCODE -ne 0) { throw 'keytool failed to create the release key.' }
    } finally {
        Remove-Item Env:CHAND_KEYSTORE_PASSWORD,Env:CHAND_KEY_PASSWORD -ErrorAction SilentlyContinue
    }
}

$securePassword = Get-Content -LiteralPath $secretPath -Raw | ConvertTo-SecureString
$password = [System.Net.NetworkCredential]::new('', $securePassword).Password
if ($ShowRecoveryPassword) {
    Write-Host 'Save this password in a password manager. The .dpapi file alone cannot be restored on another Windows account or PC:'
    Write-Output $password
    exit 0
}

if ($CreateOnly) {
    Write-Host "Release key is ready at $keyPath. Back up the .p12 and use -ShowRecoveryPassword to save its password separately."
    exit 0
}

try {
    $env:CHAND_KEYSTORE_PATH = $keyPath
    $env:CHAND_KEYSTORE_PASSWORD = $password
    $env:CHAND_KEY_ALIAS = 'chand-release'
    $env:CHAND_KEY_PASSWORD = $password
    Push-Location $projectDir
    try {
        & (Join-Path $projectDir 'gradlew.bat') :app:assembleRelease
        if ($LASTEXITCODE -ne 0) { throw 'Release build failed.' }
    } finally {
        Pop-Location
    }
} finally {
    Remove-Item Env:CHAND_KEYSTORE_PATH,Env:CHAND_KEYSTORE_PASSWORD,Env:CHAND_KEY_ALIAS,Env:CHAND_KEY_PASSWORD -ErrorAction SilentlyContinue
}
