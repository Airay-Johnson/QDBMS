@echo off
chcp 65001 >nul
title QDBMS 验证 - 数据库创建 + 编译启动

echo ============================================
echo   QDBMS 一键验证脚本
echo ============================================
echo.

cd /d "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS\qdbms-server"

:: ── 检查 Java ──
echo [1/4] 检查 Java 环境...
java --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [FAIL] 未找到 Java。请安装 JDK 17+
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('java --version 2^>^&1 ^| findstr /i "openjdk\|version"') do echo   %%i
echo   [OK]

:: ── 检查 Maven ──
echo.
echo [2/4] 检查 Maven...
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARN] 未找到 Maven，尝试使用 mvnw...
    if exist "..\demo\mvnw.cmd" (
        set MVN=..\demo\mvnw.cmd
        echo   使用项目自带 mvnw
    ) else (
        echo [FAIL] 请安装 Maven 或使用 IntelliJ IDEA 打开项目
        pause
        exit /b 1
    )
) else (
    set MVN=mvn
    for /f "tokens=*" %%i in ('mvn --version 2^>^&1 ^| findstr /i "Apache"') do echo   %%i
)
echo   [OK]

:: ── 创建数据库 ──
echo.
echo [3/4] 创建 MySQL 数据库...
echo   请手动执行以下SQL（MySQL需已启动）:
echo.
echo   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS qdbms DEFAULT CHARSET utf8mb4;"
echo   mysql -u root -p qdbms ^< src\main\resources\db\init.sql
echo.
echo   如果 MySQL 用户名/密码不是 root/root，请先修改 application.yml
echo   [SKIP] 需要手动操作

:: ── 编译项目 ──
echo.
echo [4/4] 编译项目...
echo   正在下载依赖和编译...

%MVN% compile -q 2>&1
if %errorlevel% equ 0 (
    echo   [OK] 编译成功！
    echo.
    echo ============================================
    echo   验证通过！启动命令:
    echo   %MVN% spring-boot:run
    echo.
    echo   启动后访问: http://localhost:8080/doc.html
    echo   登录测试: POST /api/auth/login
    echo     {"username":"admin","password":"admin123"}
    echo ============================================
) else (
    echo   [FAIL] 编译失败，请查看上方错误信息
)

pause
