@echo off
echo ========================================
echo   QDBMS 后端构建
echo ========================================
cd /d "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS\qdbms-server"
echo.
echo [1/2] Maven 编译...
call mvn clean package -DskipTests -P local
if %errorlevel% neq 0 (
    echo [FAIL] Maven 编译失败，查看上方错误
    pause
    exit /b 1
)
echo [OK] 编译成功
echo.
echo [2/2] 启动 QDBMS...
echo 浏览器将打开 http://localhost:8080
start http://localhost:8080
call mvn spring-boot:run -P local
pause
