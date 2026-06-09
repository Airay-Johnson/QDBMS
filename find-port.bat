@echo off
echo Finding proxy...
tasklist | findstr /i "clash mihomo v2ray sing xray trojan hiddify"
echo.
echo Check system proxy:
reg query "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings" | findstr "Proxy"
pause
