Set-Location "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS"

Write-Host "Pull + Push to GitHub..." -ForegroundColor Cyan

git pull origin main --no-rebase
if ($LASTEXITCODE -ne 0) { Write-Host "Pull failed, continuing..." }

git push origin main
if ($LASTEXITCODE -eq 0) { Write-Host "SUCCESS" -ForegroundColor Green }
else { Write-Host "Push failed, may need authentication" -ForegroundColor Yellow }

Read-Host "Press Enter to close"
