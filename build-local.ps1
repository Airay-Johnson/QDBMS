# ============================================
# QDBMS 本地构建脚本
# 1. 构建前端 (Vue 3)
# 2. 复制前端到 Spring Boot static 目录
# 3. 编译后端 JAR
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  QDBMS 本地构建" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. 构建前端
Write-Host "`n[1/3] 构建前端..." -ForegroundColor Yellow
Set-Location qdbms-web
if (-not (Test-Path node_modules)) {
    Write-Host "  安装依赖..." -ForegroundColor Gray
    npm install
}
Write-Host "  打包..." -ForegroundColor Gray
npm run build
Set-Location ..

# 2. 复制前端到后端 static 目录
Write-Host "`n[2/3] 复制前端文件到 Spring Boot..." -ForegroundColor Yellow
$staticDir = "qdbms-server\src\main\resources\static"
if (Test-Path $staticDir) {
    Remove-Item -Recurse -Force "$staticDir\*" -ErrorAction SilentlyContinue
} else {
    New-Item -ItemType Directory -Path $staticDir | Out-Null
}
Copy-Item -Recurse -Force "qdbms-web\dist\*" $staticDir
Write-Host "  已复制到 $staticDir" -ForegroundColor Green

# 3. 构建后端
Write-Host "`n[3/3] 构建后端 JAR..." -ForegroundColor Yellow
Set-Location qdbms-server
mvn clean package -DskipTests -P local
Set-Location ..

Write-Host "`n========================================"  -ForegroundColor Green
Write-Host "  构建完成！" -ForegroundColor Green
Write-Host "  JAR 位置: qdbms-server\target\qdbms-server-1.0.0.jar" -ForegroundColor Green
Write-Host "  运行: mvn spring-boot:run -f qdbms-server  (或 java -jar ...)" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
