<#
.SYNOPSIS
    QDBMS GitHub 推送脚本
.DESCRIPTION
    初始化 Git 仓库、提交代码、推送到 GitHub
    使用前请修改 $repoUrl 为你的仓库地址
#>

$ErrorActionPreference = "Stop"
$projectDir = "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS"

# ============================================
# 1. 修改为你的 GitHub 仓库地址
# ============================================
$repoUrl = "https://github.com/YOUR_USERNAME/QDBMS.git"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  QDBMS GitHub 推送" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 检查 git
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Host "[错误] 未找到 Git，请先安装: https://git-scm.com/" -ForegroundColor Red
    exit 1
}

Set-Location $projectDir

# 清理旧仓库
if (Test-Path .git) {
    Write-Host "清理旧 .git 目录..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force .git
}

# 初始化
Write-Host "初始化 Git 仓库..." -ForegroundColor Yellow
git init
git branch -m main

# 提交
Write-Host "添加文件..." -ForegroundColor Yellow
git add -A
Write-Host "提交..." -ForegroundColor Yellow
git commit -m "初始化 QDBMS - Spring Boot 3.2.5 + Vue 3 问卷管理系统"

# 推送
Write-Host "推送到 $repoUrl ..." -ForegroundColor Yellow
git remote add origin $repoUrl
git push -u origin main

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  推送完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
