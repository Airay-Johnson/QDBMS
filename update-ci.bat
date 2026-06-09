@echo off
pushd "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS"
git pull origin main --no-rebase
git push origin main
echo DONE
pause
