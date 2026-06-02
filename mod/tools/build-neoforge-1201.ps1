# Build neoforge-1.20.1 module
# Requires Gradle 8.x (userdev plugin 7.x is incompatible with Gradle 9.x)

$ModDir = "F:\模组数据包插件移植\开发\debugbridge-dev-1.21.1-1.20.1\mod"
$WrapperProps = "$ModDir\gradle\wrapper\gradle-wrapper.properties"
$Settings = "$ModDir\settings.gradle.kts"

# Backup files
$WrapperBackup = "$WrapperProps.bak"
$SettingsBackup = "$Settings.bak"

try {
    # 1. Backup current configs
    Copy-Item -Path $WrapperProps -Destination $WrapperBackup -Force
    Copy-Item -Path $Settings -Destination $SettingsBackup -Force

    # 2. Switch Gradle to 8.10.2
    $content = Get-Content $WrapperProps -Raw
    $content = $content -replace 'gradle-9\.[0-9.]+(-bin)?\.zip', 'gradle-8.10.2-bin.zip'
    $content = $content -replace 'distributionSha256Sum=.*', 'distributionSha256Sum=31c55713e40233a8303827ceb42ca48a47267a0ad4bab9177123121e71524c26'
    Set-Content -Path $WrapperProps -Value $content

    # 3. Create minimal settings with only :core and :neoforge-1.20.1
    @"
pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = "debugbridge"
include(":core")
include(":neoforge-1.20.1")
"@ | Set-Content -Path $Settings -Force

    # 4. Run build from the mod directory
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
    Write-Host "Building neoforge-1.20.1 with Gradle 8.10.2..." -ForegroundColor Cyan
    Push-Location $ModDir
    try {
        & .\gradlew.bat :neoforge-1.20.1:jar --console=plain 2>&1
    } finally {
        Pop-Location
    }

    if ($LASTEXITCODE -eq 0) {
        Write-Host "`nBUILD SUCCESSFUL" -ForegroundColor Green
    } else {
        Write-Host "`nBUILD FAILED (exit code: $LASTEXITCODE)" -ForegroundColor Red
    }
}
finally {
    # 5. Restore original configs
    if (Test-Path $WrapperBackup) {
        Move-Item -Path $WrapperBackup -Destination $WrapperProps -Force -ErrorAction SilentlyContinue
    }
    if (Test-Path $SettingsBackup) {
        Move-Item -Path $SettingsBackup -Destination $Settings -Force -ErrorAction SilentlyContinue
    }
}
