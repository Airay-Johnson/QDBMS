@echo off
git config --global http.proxy http://127.0.0.1:12000
git config --global https.proxy http://127.0.0.1:12000
pushd "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS"
git add .github/workflows/release.yml
git commit -m "Fix CI/CD: merge frontend into JAR, remove -P local"
git pull origin main --no-rebase 2>nul
if errorlevel 1 (git push --force-with-lease origin main) else (git push origin main)
echo DONE
pause
