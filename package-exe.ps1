<#
.SYNOPSIS
    QDBMS EXE 打包脚本 — 使用 jpackage 生成 Windows 安装包
.DESCRIPTION
    1. 构建前端 + 后端 JAR
    2. 使用 jpackage 打包成原生 Windows EXE
    3. 输出到 package/ 目录
.REQUIREMENT
    - JDK 17+ (含 jpackage)
    - Node.js (含 npm)
    - WiX Toolset (生成 .msi 时需要, https://wixtoolset.org/)
#>

$ErrorActionPreference = "Stop"
$APP_NAME = "QDBMS"
$APP_VERSION = "1.0.0"
$MAIN_JAR = "qdbms-server-${APP_VERSION}.jar"
$MAIN_CLASS = "com.qdbms.DesktopLauncher"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  QDBMS EXE 打包工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# ---- 检查环境 ----
Write-Host "`n检查环境..." -ForegroundColor Yellow

$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    Write-Host "  [错误] 请设置 JAVA_HOME 环境变量 (JDK 17+)" -ForegroundColor Red
    exit 1
}
Write-Host "  JAVA_HOME = $javaHome" -ForegroundColor Green

$jpackage = "$javaHome\bin\jpackage.exe"
if (-not (Test-Path $jpackage)) {
    Write-Host "  [错误] 找不到 jpackage, 需要 JDK 16+" -ForegroundColor Red
    exit 1
}
Write-Host "  jpackage 已找到" -ForegroundColor Green

# ---- 构建项目 ----
Write-Host "`n[1/3] 构建项目..." -ForegroundColor Yellow

# 前端
Write-Host "  构建前端..." -ForegroundColor Gray
Set-Location qdbms-web
if (-not (Test-Path node_modules)) {
    npm install
}
npm run build
Set-Location ..

# 复制前端到 static
$staticDir = "qdbms-server\src\main\resources\static"
if (Test-Path $staticDir) { Remove-Item -Recurse -Force "$staticDir\*" -ErrorAction SilentlyContinue }
else { New-Item -ItemType Directory -Path $staticDir | Out-Null }
Copy-Item -Recurse -Force "qdbms-web\dist\*" $staticDir

# 后端
Write-Host "  构建后端..." -ForegroundColor Gray
Set-Location qdbms-server
mvn clean package -DskipTests -P local -q
Set-Location ..

$jarPath = "qdbms-server\target\$MAIN_JAR"
if (-not (Test-Path $jarPath)) {
    Write-Host "  [错误] JAR 构建失败" -ForegroundColor Red
    exit 1
}
Write-Host "  JAR 构建成功: $jarPath" -ForegroundColor Green

# ---- jpackage 打包 ----
Write-Host "`n[2/3] jpackage 打包..." -ForegroundColor Yellow

$outputDir = "package"
$inputDir = "qdbms-server\target"
if (Test-Path $outputDir) { Remove-Item -Recurse -Force $outputDir }
New-Item -ItemType Directory -Path $outputDir | Out-Null

# 复制图标 (如果有的话)
$iconPath = ""
if (Test-Path "qdbms.ico") {
    $iconPath = "--icon qdbms.ico"
}

# jpackage 参数
$jpArgs = @(
    "--name", $APP_NAME,
    "--app-version", $APP_VERSION,
    "--input", $inputDir,
    "--main-jar", $MAIN_JAR,
    "--main-class", $MAIN_CLASS,
    "--dest", $outputDir,
    "--type", "exe",
    "--win-shortcut",
    "--win-menu",
    "--win-dir-chooser",
    "--description", "QDBMS 问卷数据库管理系统",
    "--vendor", "QDBMS",
    "--java-options", "-Xmx512m -Dfile.encoding=UTF-8",
    "--runtime-image", "$javaHome"
)

if ($iconPath) { $jpArgs += $iconPath }

Write-Host "  执行: jpackage $($jpArgs -join ' ')" -ForegroundColor Gray
& $jpackage $jpArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host "  [错误] jpackage 打包失败 (exit code: $LASTEXITCODE)" -ForegroundColor Red
    Write-Host "  提示: 检查是否安装了 WiX Toolset (https://wixtoolset.org/)" -ForegroundColor Yellow
    exit 1
}

# ---- 完成 ----
Write-Host "`n[3/3] 打包完成!" -ForegroundColor Yellow
$exePath = Get-ChildItem -Path $outputDir -Filter "*.exe" -Recurse | Select-Object -First 1
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  EXE 路径: $($exePath.FullName)" -ForegroundColor Green
Write-Host "  双击运行，浏览器自动打开" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
