@echo off
cd /d "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS"

echo === Step 1: Clean up ===
taskkill /f /im git.exe 2>nul
timeout /t 2 /nobreak >nul
rmdir /s /q .git 2>nul
if exist .git (
    echo Force removing .git...
    takeown /f .git /r /d y >nul 2>&1
    icacls .git /grant everyone:F /t >nul 2>&1
    rmdir /s /q .git 2>nul
)

echo === Step 2: Init ===
git init
git branch -m main
git remote add origin https://github.com/Airay-Johnson/QDBMS.git

echo === Step 3: Add files ===
git add -A

echo === Step 4: Commit ===
git commit -m "Init QDBMS - Spring Boot + Vue 3 Survey Management System"

echo === Step 5: Pull remote ===
git pull origin main --allow-unrelated-histories --no-edit

echo === Step 6: Push ===
git push -u origin main

echo === DONE ===
pause
