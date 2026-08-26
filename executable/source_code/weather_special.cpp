#include <iostream>
#include <string>
#include <vector>
#include <iomanip>
#include <ctime>
#include <windows.h>
#include <wininet.h>
#include <zlib.h>

#pragma comment(lib, "wininet.lib")

struct CityInfo
{
    std::wstring name;
    double latitude;
    double longitude;
};

std::vector<CityInfo> cityDatabase = {
    {L"北京", 39.9042, 116.4074},
    {L"上海", 31.2304, 121.4737},
    {L"广州", 23.1291, 113.2644},
    {L"深圳", 22.5431, 114.0579},
    {L"杭州", 30.2741, 120.1551},
    {L"成都", 30.5728, 104.0668},
    {L"武汉", 30.5928, 114.3055},
    {L"南京", 32.0603, 118.7969},
    {L"西安", 34.3416, 108.9398},
    {L"重庆", 29.4316, 106.9123},
    {L"郑州", 34.7466, 113.6253},
    {L"开封", 34.8016, 114.3534},
    {L"洛阳", 34.6234, 112.4536},
    {L"香港", 22.3193, 114.1694},
    {L"澳门", 22.1987, 113.5439}
};

std::string decompressGzip(const std::string& compressed)
{
    if (compressed.empty())
        return "";
    
    std::string decompressed;
    z_stream strm;
    strm.zalloc = Z_NULL;
    strm.zfree = Z_NULL;
    strm.opaque = Z_NULL;
    strm.avail_in = (uInt)compressed.size();
    strm.next_in = (Bytef*)compressed.data();
    
    if (inflateInit2(&strm, 16 + MAX_WBITS) != Z_OK)
        return compressed;
    
    char buffer[8192];
    int ret;
    do {
        strm.avail_out = sizeof(buffer);
        strm.next_out = (Bytef*)buffer;
        ret = inflate(&strm, Z_NO_FLUSH);
        if (ret == Z_NEED_DICT || ret == Z_DATA_ERROR || ret == Z_MEM_ERROR)
        {
            inflateEnd(&strm);
            return compressed;
        }
        decompressed.append(buffer, sizeof(buffer) - strm.avail_out);
    } while (ret != Z_STREAM_END);
    
    inflateEnd(&strm);
    return decompressed;
}

std::string httpGet(const std::string& host, const std::string& path)
{
    HINTERNET hInternet = InternetOpenA("WeatherAPI/2.0", INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
    if (!hInternet) return "";

    HINTERNET hConnect = InternetConnectA(hInternet, host.c_str(), INTERNET_DEFAULT_HTTP_PORT, NULL, NULL, INTERNET_SERVICE_HTTP, 0, 0);
    if (!hConnect) { InternetCloseHandle(hInternet); return ""; }

    HINTERNET hRequest = HttpOpenRequestA(hConnect, "GET", path.c_str(), NULL, NULL, NULL, INTERNET_FLAG_KEEP_CONNECTION, 0);
    if (!hRequest) { InternetCloseHandle(hConnect); InternetCloseHandle(hInternet); return ""; }

    BOOL bSend = HttpSendRequestA(hRequest, "Accept-Encoding: gzip, deflate\r\n", -1, NULL, 0);
    if (!bSend) { InternetCloseHandle(hRequest); InternetCloseHandle(hConnect); InternetCloseHandle(hInternet); return ""; }

    char contentEncoding[64] = {0};
    DWORD contentEncodingSize = sizeof(contentEncoding);
    HttpQueryInfoA(hRequest, HTTP_QUERY_CONTENT_ENCODING, contentEncoding, &contentEncodingSize, NULL);

    std::string compressedResponse;
    char buffer[8192];
    DWORD bytesRead;

    while (InternetReadFile(hRequest, buffer, sizeof(buffer), &bytesRead) && bytesRead > 0)
    {
        compressedResponse.append(buffer, bytesRead);
    }

    InternetCloseHandle(hRequest);
    InternetCloseHandle(hConnect);
    InternetCloseHandle(hInternet);

    if (strcmp(contentEncoding, "gzip") == 0 || strcmp(contentEncoding, "deflate") == 0)
    {
        return decompressGzip(compressedResponse);
    }

    return compressedResponse;
}

std::string trim(const std::string& str) {
    size_t first = str.find_first_not_of(" \t\n\r");
    size_t last = str.find_last_not_of(" \t\n\r");
    if (first == std::string::npos) return "";
    return str.substr(first, last - first + 1);
}

std::string getJsonValue(const std::string& json, const std::string& key) {
    std::string searchKey = "\"" + key + "\"";
    size_t pos = json.find(searchKey);
    if (pos == std::string::npos) return "";
    
    pos = json.find(":", pos);
    if (pos == std::string::npos) return "";
    pos++;
    
    while (pos < json.size() && (json[pos] == ' ' || json[pos] == '\t' || json[pos] == '\n')) pos++;
    
    if (pos >= json.size()) return "";
    
    if (json[pos] == '"') {
        pos++;
        size_t end = json.find("\"", pos);
        if (end == std::string::npos) return "";
        return json.substr(pos, end - pos);
    }
    
    size_t end = pos;
    while (end < json.size() && json[end] != ',' && json[end] != '}' && json[end] != ']') end++;
    return trim(json.substr(pos, end - pos));
}

std::vector<std::string> parseJsonArray(const std::string& json, const std::string& key) {
    std::vector<std::string> result;
    std::string searchKey = "\"" + key + "\"";
    size_t pos = json.find(searchKey);
    if (pos == std::string::npos) return result;
    
    pos = json.find("[", pos);
    if (pos == std::string::npos) return result;
    pos++;
    
    int depth = 1;
    size_t start = pos;
    while (pos < json.size() && depth > 0) {
        if (json[pos] == '[') depth++;
        if (json[pos] == ']') depth--;
        if (json[pos] == ',' && depth == 1) {
            result.push_back(trim(json.substr(start, pos - start)));
            start = pos + 1;
        }
        pos++;
    }
    if (start < pos - 1) {
        result.push_back(trim(json.substr(start, pos - start - 1)));
    }
    
    return result;
}

std::wstring getCondition(int code) {
    if (code == 0 || code == 1) return L"晴";
    if (code == 2) return L"多云";
    if (code == 3) return L"阴";
    if (code >= 45 && code <= 48) return L"雾";
    if (code >= 51 && code <= 55) return L"小雨";
    if (code >= 61 && code <= 65) return L"中雨";
    if (code >= 80 && code <= 82) return L"大雨";
    if (code >= 71 && code <= 75) return L"小雪";
    if (code >= 85 && code <= 86) return L"大雪";
    if (code >= 95 && code <= 99) return L"雷雨";
    return L"阴";
}

CityInfo findCity(const std::wstring& name) {
    std::wstring simplified = name;
    size_t pos = simplified.find(L"市");
    if (pos != std::wstring::npos) {
        simplified = simplified.substr(0, pos);
    }
    
    for (const auto& city : cityDatabase) {
        if (city.name == name || city.name == simplified) {
            return city;
        }
    }
    
    return {L"", 39.9042, 116.4074};
}

void printWeather(double lat, double lon, const std::wstring& cityName) {
    std::wcout << L"\n正在获取实时天气数据...\n";
    
    std::stringstream ss;
    ss << "/v1/forecast?latitude=" << std::fixed << std::setprecision(4) << lat
       << "&longitude=" << std::fixed << std::setprecision(4) << lon
       << "&current=temperature_2m,apparent_temperature,relative_humidity,wind_speed_10m,weather_code,sunrise,sunset"
       << "&daily=temperature_2m_max,temperature_2m_min,weather_code,precipitation_sum"
       << "&timezone=Asia/Shanghai";
    
    std::string response = httpGet("api.open-meteo.com", ss.str());
    
    if (response.empty()) {
        std::wcout << L"获取天气数据失败，请检查网络连接！\n";
        return;
    }
    
    std::wcout << L"\n========================================\n";
    std::wcout << L"           实时天气预报\n";
    std::wcout << L"========================================\n";
    
    std::string tempStr = getJsonValue(response, "temperature_2m");
    std::string feelsLikeStr = getJsonValue(response, "apparent_temperature");
    std::string humidityStr = getJsonValue(response, "relative_humidity");
    std::string windSpeedStr = getJsonValue(response, "wind_speed_10m");
    std::string weatherCodeStr = getJsonValue(response, "weather_code");
    std::string sunriseStr = getJsonValue(response, "sunrise");
    std::string sunsetStr = getJsonValue(response, "sunset");
    
    double temp = tempStr.empty() ? 0 : std::stod(tempStr);
    double feelsLike = feelsLikeStr.empty() ? 0 : std::stod(feelsLikeStr);
    int humidity = humidityStr.empty() ? 0 : std::stoi(humidityStr);
    double windSpeed = windSpeedStr.empty() ? 0 : std::stod(windSpeedStr);
    int weatherCode = weatherCodeStr.empty() ? 0 : std::stoi(weatherCodeStr);
    
    std::wcout << L"城市:             " << cityName << L"\n";
    std::wcout << L"温度:             " << std::fixed << std::setprecision(1) << temp << L"°C\n";
    std::wcout << L"体感温度:         " << std::fixed << std::setprecision(1) << feelsLike << L"°C\n";
    std::wcout << L"天气状况:         " << getCondition(weatherCode) << L"\n";
    std::wcout << L"湿度:             " << humidity << L"%\n";
    std::wcout << L"风速:             " << std::fixed << std::setprecision(1) << windSpeed << L" km/h\n";
    
    if (!sunriseStr.empty()) {
        std::wstring sunrise(sunriseStr.begin(), sunriseStr.end());
        if (sunrise.size() > 10) sunrise = sunrise.substr(11, 5);
        std::wcout << L"日出:             " << sunrise << L"\n";
    } else {
        std::wcout << L"日出:             N/A\n";
    }
    
    if (!sunsetStr.empty()) {
        std::wstring sunset(sunsetStr.begin(), sunsetStr.end());
        if (sunset.size() > 10) sunset = sunset.substr(11, 5);
        std::wcout << L"日落:             " << sunset << L"\n";
    } else {
        std::wcout << L"日落:             N/A\n";
    }
    
    std::wcout << L"========================================\n";
    
    std::wcout << L"\n========================================\n";
    std::wcout << L"           未来7天天气预报\n";
    std::wcout << L"========================================\n";
    std::wcout << L"日期      最高温   最低温   天气状况   降水量\n";
    std::wcout << L"----------------------------------------\n";
    
    auto timeArray = parseJsonArray(response, "time");
    auto tempMaxArray = parseJsonArray(response, "temperature_2m_max");
    auto tempMinArray = parseJsonArray(response, "temperature_2m_min");
    auto weatherCodeArray = parseJsonArray(response, "weather_code");
    auto precipArray = parseJsonArray(response, "precipitation_sum");
    
    size_t count = std::min(timeArray.size(), std::min(tempMaxArray.size(), std::min(tempMinArray.size(), weatherCodeArray.size())));
    
    for (size_t i = 0; i < count && i < 7; i++) {
        std::wstring date(timeArray[i].begin(), timeArray[i].end());
        if (date.size() > 5) date = date.substr(5);
        
        double maxTemp = 0, minTemp = 0, precip = 0;
        int code = 0;
        
        try { maxTemp = std::stod(tempMaxArray[i]); } catch(...) {}
        try { minTemp = std::stod(tempMinArray[i]); } catch(...) {}
        try { code = std::stoi(weatherCodeArray[i]); } catch(...) {}
        if (i < precipArray.size()) {
            try { precip = std::stod(precipArray[i]); } catch(...) {}
        }
        
        std::wcout << std::setw(10) << std::left << date
                   << std::setw(10) << std::left << std::fixed << std::setprecision(1) << maxTemp << L"°C"
                   << std::setw(10) << std::left << std::fixed << std::setprecision(1) << minTemp << L"°C"
                   << std::setw(12) << std::left << getCondition(code)
                   << std::fixed << std::setprecision(1) << precip << L"mm\n";
    }
    
    std::wcout << L"========================================\n";
    
    std::wcout << L"\n========================================\n";
    std::wcout << L"           降水趋势图\n";
    std::wcout << L"========================================\n";
    
    if (precipArray.empty()) {
        std::wcout << L"暂无降水数据\n";
    } else {
        double maxPrecip = 0;
        for (size_t i = 0; i < precipArray.size() && i < 7; i++) {
            try {
                double p = std::stod(precipArray[i]);
                if (p > maxPrecip) maxPrecip = p;
            } catch(...) {}
        }
        
        if (maxPrecip == 0) maxPrecip = 1;
        
        for (int row = 10; row >= 0; row--) {
            double threshold = (maxPrecip / 10) * row;
            if (row == 10) {
                std::wcout << std::setw(8) << std::right << std::fixed << std::setprecision(1) << maxPrecip << L"mm |";
            } else if (row == 0) {
                std::wcout << std::setw(8) << std::right << L"0.0mm |";
            } else {
                std::wcout << std::setw(8) << std::right << L"      |";
            }
            
            for (size_t i = 0; i < precipArray.size() && i < 7; i++) {
                double p = 0;
                try { p = std::stod(precipArray[i]); } catch(...) {}
                
                if (p >= threshold && threshold > 0) {
                    std::wcout << L" ██";
                } else if (p > 0 && row == 0) {
                    std::wcout << L" ░░";
                } else {
                    std::wcout << L"  ";
                }
            }
            std::wcout << L"\n";
        }
        
        std::wcout << L"        +";
        for (size_t i = 0; i < std::min(precipArray.size(), (size_t)7); i++) {
            std::wcout << L"--";
        }
        std::wcout << L"\n        ";
        
        for (size_t i = 0; i < timeArray.size() && i < 7; i++) {
            std::wstring date(timeArray[i].begin(), timeArray[i].end());
            if (date.size() > 5) date = date.substr(8, 2);
            std::wcout << date;
        }
        std::wcout << L"\n";
    }
    
    std::wcout << L"========================================\n";
}

int main()
{
    SetConsoleOutputCP(CP_UTF8);
    SetConsoleCP(CP_UTF8);
    
    while (true)
    {
        system("cls");
        
        std::wcout << L"========================================\n";
        std::wcout << L"              特供版\n";
        std::wcout << L"========================================\n";
        std::wcout << L"\n当前数据源: Open-Meteo\n";
        std::wcout << L"\n请选择查询方式:\n";
        std::wcout << L"  1. 输入城市名称查询（支持全国城市）\n";
        std::wcout << L"  2. 输入经纬度查询（支持全球天气）\n";
        std::wcout << L"  0. 退出程序\n";
        std::wcout << L"\n请输入选择: ";
        
        int choice;
        std::wcin >> choice;
        std::wcin.ignore(1000, L'\n');
        
        if (choice == 0) {
            std::wcout << L"感谢使用！\n";
            break;
        }
        
        if (choice == 1) {
            std::wcout << L"\n请输入城市名称: ";
            std::wstring city;
            std::getline(std::wcin, city);
            
            if (city.empty()) {
                std::wcout << L"\n错误：城市名称不能为空！\n";
                system("pause");
                continue;
            }
            
            CityInfo cityInfo = findCity(city);
            
            if (cityInfo.name.empty()) {
                std::wcout << L"\n错误：暂不支持该城市，请尝试输入经纬度查询！\n";
                system("pause");
                continue;
            }
            
            printWeather(cityInfo.latitude, cityInfo.longitude, cityInfo.name);
        }
        else if (choice == 2) {
            std::wcout << L"\n请输入经纬度（格式: 纬度,经度）: ";
            std::wstring location;
            std::getline(std::wcin, location);
            
            if (location.empty()) {
                std::wcout << L"\n错误：经纬度不能为空！\n";
                system("pause");
                continue;
            }
            
            size_t commaPos = location.find(L',');
            if (commaPos == std::wstring::npos) {
                std::wcout << L"\n错误：格式错误，请输入: 纬度,经度\n";
                system("pause");
                continue;
            }
            
            std::wstring latStr = location.substr(0, commaPos);
            std::wstring lonStr = location.substr(commaPos + 1);
            
            try {
                double lat = std::stod(latStr);
                double lon = std::stod(lonStr);
                
                if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                    std::wcout << L"\n错误：经纬度范围不正确！\n";
                    system("pause");
                    continue;
                }
                
                printWeather(lat, lon, L"自定义位置");
            } catch (...) {
                std::wcout << L"\n错误：经纬度格式不正确！\n";
                system("pause");
                continue;
            }
        }
        else {
            std::wcout << L"\n错误：请输入 0-2 之间的数字！\n";
            system("pause");
            continue;
        }
        
        std::wcout << L"\n按任意键继续查询...\n";
        system("pause >nul");
    }
    
    return 0;
}
