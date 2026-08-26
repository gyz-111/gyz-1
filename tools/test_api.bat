@echo off
curl -s "https://devapi.qweather.com/v7/weather/now?location=112.4536,34.6234&key=c22b01d72bbe469cb77699532913ff67" > test_result.txt
echo Done!