package com.qingkong.weather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 和风天气 API 客户端（QWeather）
 * 使用开发者专属域名与密钥，全部请求走 HTTPS，带 X-QW-Api-Key 请求头。
 */
public final class WeatherApi {

    /** 和风天气专属 API 域名（与密钥绑定） */
    private static final String API_HOST = "nr7qquf7hj.re.qweatherapi.com";
    /** 和风天气 API 密钥 */
    private static final String API_KEY = "c22b01d72bbe469cb77699532913ff67";

    // ---------------- 数据模型 ----------------

    public static class Now {
        public String temp, feelsLike, text, icon, windDir, windScale,
                humidity, pressure, visib, cloud, obsTime;
    }

    public static class Hourly {
        public String fxTime, temp, text, icon, windDir, windScale, pop, precip;
    }

    public static class Daily {
        public String fxDate, tempMax, tempMin, textDay, textNight, iconDay, iconNight,
                windDirDay, windScaleDay, precip, uvIndex, sunrise, sunset, moonPhase;
    }

    public interface Callback<T> {
        void onResult(T result);

        void onError(String message);
    }

    /** 单方法接口：JSON 解析逻辑（可用 lambda） */
    public interface JsonCallback {
        void onResult(JSONObject json);
    }

    // ---------------- 接口方法 ----------------

    /** 实时天气 */
    public static void getNow(String location, Callback<Now> cb) {
        getJson("/v7/weather/now", location, (o) -> {
            try {
                JSONObject n = o.getJSONObject("now");
                Now now = new Now();
                now.temp = n.optString("temp");
                now.feelsLike = n.optString("feelsLike");
                now.text = n.optString("text");
                now.icon = n.optString("icon");
                now.windDir = n.optString("windDir");
                now.windScale = n.optString("windScale");
                now.humidity = n.optString("humidity");
                now.pressure = n.optString("pressure");
                now.visib = n.optString("vis");
                now.cloud = n.optString("cloud");
                now.obsTime = n.optString("obsTime");
                cb.onResult(now);
            } catch (Exception e) {
                cb.onError("实时天气数据解析失败");
            }
        }, cb);
    }

    /** 24小时逐小时预报 */
    public static void getHourly24(String location, Callback<List<Hourly>> cb) {
        getJson("/v7/weather/24h", location, (o) -> {
            try {
                JSONArray arr = o.getJSONArray("hourly");
                List<Hourly> list = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject h = arr.getJSONObject(i);
                    Hourly item = new Hourly();
                    item.fxTime = h.optString("fxTime");
                    item.temp = h.optString("temp");
                    item.text = h.optString("text");
                    item.icon = h.optString("icon");
                    item.windDir = h.optString("windDir");
                    item.windScale = h.optString("windScale");
                    item.pop = h.optString("pop");
                    item.precip = h.optString("precip");
                    list.add(item);
                }
                cb.onResult(list);
            } catch (Exception e) {
                cb.onError("24小时预报数据解析失败");
            }
        }, cb);
    }

    /** 未来15天预报 */
    public static void getDaily15(String location, Callback<List<Daily>> cb) {
        getJson("/v7/weather/15d", location, (o) -> {
            try {
                JSONArray arr = o.getJSONArray("daily");
                List<Daily> list = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject d = arr.getJSONObject(i);
                    Daily item = new Daily();
                    item.fxDate = d.optString("fxDate");
                    item.tempMax = d.optString("tempMax");
                    item.tempMin = d.optString("tempMin");
                    item.textDay = d.optString("textDay");
                    item.textNight = d.optString("textNight");
                    item.iconDay = d.optString("iconDay");
                    item.iconNight = d.optString("iconNight");
                    item.windDirDay = d.optString("windDirDay");
                    item.windScaleDay = d.optString("windScaleDay");
                    item.precip = d.optString("precip");
                    item.uvIndex = d.optString("uvIndex");
                    item.sunrise = d.optString("sunrise");
                    item.sunset = d.optString("sunset");
                    item.moonPhase = d.optString("moonPhase");
                    list.add(item);
                }
                cb.onResult(list);
            } catch (Exception e) {
                cb.onError("15天预报数据解析失败");
            }
        }, cb);
    }

    /** 城市名搜索（和风 GeoAPI，最多10条） */
    public static void searchCity(String name, Callback<List<String[]>> cb) {
        getJson("/geo/v2/city/lookup?location=" + urlEncode(name) + "&number=10", null, (o) -> {
            try {
                JSONArray arr = o.getJSONArray("location");
                List<String[]> list = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject c = arr.getJSONObject(i);
                    list.add(new String[]{
                            c.optString("name"),
                            c.optString("adm1"),
                            c.optString("adm2"),
                            c.optString("lon"),
                            c.optString("lat")
                    });
                }
                cb.onResult(list);
            } catch (Exception e) {
                cb.onError("城市查询失败");
            }
        }, cb);
    }

    /** 坐标反查城市名（和风 GeoAPI） */
    public static void reverseGeo(String lonLat, Callback<String> cb) {
        getJson("/geo/v2/city/lookup", lonLat, (o) -> {
            try {
                JSONArray arr = o.getJSONArray("location");
                if (arr.length() > 0) {
                    JSONObject c = arr.getJSONObject(0);
                    String adm2 = c.optString("adm2", "");
                    String name = c.optString("name", "");
                    if (!adm2.isEmpty() && !adm2.equals(name)) {
                        cb.onResult(adm2 + " " + name);
                    } else {
                        cb.onResult(name);
                    }
                } else {
                    cb.onResult(null);
                }
            } catch (Exception e) {
                cb.onResult(null);
            }
        }, cb);
    }

    // ---------------- 工具 ----------------

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    /** 通用 GET 请求：path 可含已拼接参数；location 为 null 时不追加 */
    private static void getJson(String path, String location,
                                JsonCallback parse, Callback<?> outer) {
        new Thread(() -> {
            try {
                StringBuilder sb = new StringBuilder(path);
                if (location != null) {
                    sb.append(path.contains("?") ? "&" : "?").append("location=").append(location);
                }
                sb.append("&key=").append(API_KEY).append("&lang=zh&unit=m");
                URL url = new URL("https://" + API_HOST + sb);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("X-QW-Api-Key", API_KEY);
                conn.setRequestProperty("Accept-Encoding", "gzip");
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
                String contentEncoding = conn.getContentEncoding();
                if (contentEncoding != null
                        && contentEncoding.toLowerCase(Locale.ROOT).contains("gzip")) {
                    is = new java.util.zip.GZIPInputStream(is);
                }
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder resp = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) resp.append(line);
                reader.close();
                conn.disconnect();
                if (code != 200) {
                    outer.onError("网络错误 HTTP " + code);
                    return;
                }
                JSONObject json = new JSONObject(resp.toString());
                String c = json.optString("code");
                if (!"200".equals(c)) {
                    outer.onError("接口错误 code=" + c);
                    return;
                }
                parse.onResult(json);
            } catch (Exception e) {
                outer.onError(e.getMessage() == null ? "网络请求失败" : e.getMessage());
            }
        }).start();
    }

    // ---------------- 格式化与图标 ----------------

    /** 和风 icon 编号 → emoji 天气图标 */
    public static String iconEmoji(String icon, String text) {
        if (icon == null) return textEmoji(text);
        switch (icon) {
            case "100": return "\u2600\uFE0F";            // 晴
            case "101": return "\u26C5";                   // 多云
            case "102": return "\u26C5";                   // 少云
            case "103": return "\uD83C\uDF24\uFE0F";       // 晴间多云
            case "104": return "\u2601\uFE0F";             // 阴
            case "150": case "153": return "\uD83C\uDF19"; // 晴(夜)
            case "151": case "152": return "\uD83C\uDF19"; // 多云(夜)
            case "300": case "301": case "350": return "\uD83C\uDF26\uFE0F"; // 阵雨
            case "302": case "303": case "304": return "\u26C8\uFE0F";      // 雷阵雨
            case "305": case "306": case "307": case "308":
            case "309": case "310": case "311": case "312":
            case "313": case "314": case "315": case "316":
            case "317": case "318": case "399": return "\uD83C\uDF27\uFE0F"; // 雨
            case "400": case "401": case "402": case "403":
            case "407": case "408": case "409": case "410":
            case "499": return "\u2744\uFE0F";              // 雪
            case "404": case "405": case "406": return "\uD83C\uDF28\uFE0F"; // 雨夹雪
            case "500": case "501": case "509": case "510":
            case "514": case "515": return "\uD83C\uDF2B\uFE0F";              // 雾
            case "502": case "511": case "512": case "513": return "\uD83C\uDF01"; // 霾
            case "503": case "504": return "\uD83C\uDF2B\uFE0F";              // 扬沙/浮尘
            case "507": case "508": return "\uD83C\uDF2A\uFE0F";              // 沙尘暴
            case "900": return "\uD83E\uDD75";              // 热
            case "901": return "\uD83E\uDD76";              // 冷
            default: return textEmoji(text);
        }
    }

    /** 按天气文字兜底取 emoji */
    private static String textEmoji(String text) {
        if (text == null) return "\uD83C\uDF15";
        if (text.contains("雷")) return "\u26C8\uFE0F";
        if (text.contains("雨")) return "\uD83C\uDF27\uFE0F";
        if (text.contains("雪")) return "\u2744\uFE0F";
        if (text.contains("雾")) return "\uD83C\uDF2B\uFE0F";
        if (text.contains("霾")) return "\uD83C\uDF01";
        if (text.contains("沙")) return "\uD83C\uDF2A\uFE0F";
        if (text.contains("晴")) return "\u2600\uFE0F";
        if (text.contains("云") || text.contains("阴")) return "\u2601\uFE0F";
        return "\uD83C\uDF15";
    }

    public static String formatHour(String fxTime) {
        if (fxTime == null || fxTime.length() < 13) return "";
        return fxTime.substring(11, 13) + "时";
    }

    public static String formatDayLabel(String fxDate, int index) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Date d = in.parse(fxDate);
            if (index == 0) return "今天";
            if (index == 1) return "明天";
            SimpleDateFormat out = new SimpleDateFormat("M/d EEE", Locale.CHINA);
            return out.format(d);
        } catch (Exception e) {
            return fxDate;
        }
    }

    public static String formatHM(String t) {
        if (t == null || t.length() < 16) return "--:--";
        return t.substring(11, 16);
    }
}
