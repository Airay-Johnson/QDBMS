@echo off
chcp 65001 >nul
title QDBMS Agent 一键配置
echo ========================================
echo   Windows Agent 一键配置
echo   配置一次，永久自动运行
echo ========================================
echo.

set "AGENT_DIR=D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS\mcp-server"

:: 1. 检查 Python
echo [1/4] 检查 Python...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Python
    echo 下载: https://www.python.org/downloads/
    pause
    exit /b 1
)
python --version

:: 2. 安装截图依赖
echo.
echo [2/4] 安装截图依赖...
python -c "from PIL import Image; import mss" >nul 2>&1
if %errorlevel% neq 0 (
    echo 正在安装 Pillow + mss...
    pip install Pillow mss --quiet
    if %errorlevel% neq 0 (
        echo [警告] 截图库安装失败
    ) else (
        echo [OK] 截图库已安装
    )
) else (
    echo [OK] 截图库已就绪
)

:: 3. 创建开机自启
echo.
echo [3/4] 创建开机自启...

set "VBS_FILE=%AGENT_DIR%\start-agent-hidden.vbs"
(
echo Set WshShell = CreateObject("WScript.Shell"^)
echo WshShell.CurrentDirectory = "%AGENT_DIR%"
echo WshShell.Run "pythonw.exe ""%AGENT_DIR%\windows-agent.py""", 0, False
) > "%VBS_FILE%"

set "STARTUP_DIR=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup"
copy /Y "%VBS_FILE%" "%STARTUP_DIR%\QDBMS-Agent.vbs" >nul
echo [OK] 开机自启已设置

:: 4. 立即启动 Agent
echo.
echo [4/4] 启动 Agent...
wscript.exe //B "%VBS_FILE%"
echo [OK] Agent 已启动（后台静默运行）

echo.
echo ========================================
echo   配置完成！
echo   Agent 正在后台运行
echo   下次开机自动启动，无需手动操作
echo ========================================
echo.
echo 按任意键关闭...
pause >nul
