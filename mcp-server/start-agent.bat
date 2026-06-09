@echo off
chcp 65001 >nul
title QDBMS Windows Agent

echo ================================================
echo   QDBMS Windows Agent — Claude 桌面桥接
echo ================================================
echo.

:: 检查 Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Python，请先安装 Python 3.8+
    echo 下载: https://www.python.org/downloads/
    pause
    exit /b 1
)

echo [信息] Python 已就绪

:: 检查 Pillow
python -c "from PIL import Image" >nul 2>&1
if %errorlevel% neq 0 (
    echo [信息] 安装 Pillow + mss 截图库...
    pip install Pillow mss --quiet
    if %errorlevel% neq 0 (
        echo [警告] 截图库安装失败，截图功能不可用
        echo 手动安装: pip install Pillow mss
    ) else (
        echo [信息] 截图库安装完成
    )
)

echo.
echo [信息] 启动 Agent...
echo [信息] 共享目录: %~dp0
echo [信息] 按 Ctrl+C 停止
echo.

cd /d "%~dp0"
python windows-agent.py

pause
