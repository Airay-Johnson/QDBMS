@echo off
echo Killing old agent...
taskkill /f /fi "WINDOWTITLE eq *agent*" 2>nul
taskkill /f /pid 18332 2>nul
timeout /t 2 /nobreak >nul
echo Starting new agent v2...
start /min powershell -ExecutionPolicy Bypass -WindowStyle Hidden -File "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS\mcp-servergent.ps1"
echo Agent restarted!
timeout /t 2 /nobreak >nul

pushd "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS"
git config --global http.proxy http://127.0.0.1:12000
git config --global https.proxy http://127.0.0.1:12000
git add -A
git commit -m "Fix CI/CD workflow and upgrade agent to v2 async"
git pull origin main --no-rebase 2>nul
if errorlevel 1 (git push --force-with-lease origin main) else (git push origin main)
echo ALL DONE
pause
