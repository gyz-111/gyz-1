@echo off
chcp 65001 >nul

title Change Password for Weather_API

echo ========================================
echo         Change Download Password
echo ========================================
echo.
echo This tool will change the download password.
echo The password will be stored as MD5 hash.
echo.

set "newPassword="
set /p "newPassword=Enter new password: "

if "%newPassword%"=="" (
    echo Error: Password cannot be empty!
    pause
    exit /b 1
)

echo.
echo Generating MD5 hash...

for /f "delims=" %%a in ('powershell -Command "([System.BitConverter]::ToString(([System.Security.Cryptography.MD5]::Create()).ComputeHash([System.Text.Encoding]::UTF8.GetBytes('%newPassword%')))).Replace('-','')"') do (
    set "hash=%%a"
)

echo Hash generated: %hash%
echo.
echo Saving to password_hash.txt...

echo %hash% > password_hash.txt

echo.
echo Password changed successfully!
echo The new password is: %newPassword%
echo.
pause