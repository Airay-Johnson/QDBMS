@echo off
pushd "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS"

echo Step 1: Commit all changes (line ending fix)...
git add -A
git commit -m "Sync line endings and add CI/CD workflow"

echo Step 2: Pull and merge from GitHub...
git pull origin main --no-rebase -X theirs

echo Step 3: Push to GitHub...
git push origin main

echo DONE
pause
