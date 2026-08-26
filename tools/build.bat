@echo off
chcp 65001 >nul

title Build Weather Program

echo ========================================
echo         Build Weather Program
echo ========================================
echo.
echo Press any key to start building...
pause >nul

echo.
echo Checking environment...

where g++ >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo Error: g++ compiler not found!
    echo Please install MinGW and add to PATH.
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo g++ compiler: OK

echo.
echo Compiling Weather.cpp (Standard Edition)...
g++ -std=c++11 -o Weather_API_v0.0.6.exe Weather.cpp -lwininet -lz

if %errorlevel% equ 0 (
    echo.
    echo Standard Edition compilation successful!
    echo Executable generated: Weather_API_v0.0.6.exe
    echo.
) else (
    echo.
    echo Standard Edition compilation failed! Please check code for errors.
    echo.
)

echo.
echo Compiling Weather_Special.cpp (Special Edition)...
g++ -std=c++11 -o Weather_API_特供版.exe Weather_Special.cpp -lwininet -lz

if %errorlevel% equ 0 (
    echo.
    echo Special Edition compilation successful!
    echo Executable generated: Weather_API_特供版.exe
    echo.
) else (
    echo.
    echo Special Edition compilation failed! Please check code for errors.
    echo.
)

echo Press any key to exit...
pause >nul