@echo off
chcp 65001 >nul

title Add City to Weather Program

echo ========================================
echo         Add City to Weather Program
echo ========================================
echo.
echo Please visit this website to get coordinates:
echo https://www.mapchaxun.cn/jingweidu
echo.
echo Note: Website returns "longitude,latitude" format
echo       But program needs: latitude first, then longitude
echo.
echo Press any key to continue...
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

where python >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo Error: Python not found!
    echo Please install Python and add to PATH.
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo Python: OK

echo.
echo Environment check passed!
echo.

set "cityName="
set /p cityName=Enter city name (e.g., Luoyang):
if "%cityName%"=="" (
    echo.
    echo Error: City name cannot be empty!
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

set "cityPinyin="
set /p cityPinyin=Enter city pinyin (e.g., luoyang):
if "%cityPinyin%"=="" (
    echo.
    echo Error: Pinyin cannot be empty!
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

set "latitude="
set /p latitude=Enter latitude (e.g., 34.6234):
if "%latitude%"=="" (
    echo.
    echo Error: Latitude cannot be empty!
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

set "longitude="
set /p longitude=Enter longitude (e.g., 112.4536):
if "%longitude%"=="" (
    echo.
    echo Error: Longitude cannot be empty!
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

set "province="
set /p province=Enter province (e.g., Henan):
if "%province%"=="" (
    echo.
    echo Error: Province cannot be empty!
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

set "cityType="
set /p cityType=Enter city type (prefecture/district/municipality/special/autonomous):
if "%cityType%"=="" (
    echo.
    echo Error: City type cannot be empty!
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

echo.
echo Backing up Weather.cpp...
copy Weather.cpp Weather.cpp.bak >nul
if %errorlevel% neq 0 (
    echo Error: Failed to backup file!
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo Backup successful!

echo.
echo Adding city data...
python add_city.py "%cityName%" "%cityPinyin%" "%latitude%" "%longitude%" "%province%" "%cityType%"

if %errorlevel% neq 0 (
    echo.
    echo Error: Failed to add city data!
    echo Restoring backup...
    copy Weather.cpp.bak Weather.cpp >nul
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo City data added successfully!

echo.
echo Compiling program...
g++ -std=c++11 -o Weather_API_v0.0.5.exe Weather.cpp -lwininet -lz

if %errorlevel% neq 0 (
    echo.
    echo Error: Compilation failed!
    echo Restoring backup...
    copy Weather.cpp.bak Weather.cpp >nul
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo Compilation successful!

echo.
echo ========================================
echo           City Added Successfully!
echo ========================================
echo.
echo City Name: %cityName%
echo Pinyin: %cityPinyin%
echo Latitude: %latitude%
echo Longitude: %longitude%
echo Province: %province%
echo Type: %cityType%
echo.
echo New executable generated: Weather_API_v0.0.5.exe
echo.
echo Press any key to exit...
pause >nul