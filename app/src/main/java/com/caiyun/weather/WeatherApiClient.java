package com.caiyun.weather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherApiClient {

    private static final String API_HOST = "nr7qquf7hj.re.qweatherapi.com";
    private static final String API_KEY = "c22b01d72bbe469cb77699532913ff67";

    public static class Now {
        public String temp, feelsLike, text, windDir, windScale, windSpeed,
                humidity, pressure, visib, uvIndex, obsTime;
    }

    public static class Hourly {
        public String time, temp, text, pop, windDir, windScale;
    }

    public static class Daily {
        public String fxDate, tempMax, tempMin, textDay, textNight, precip, uvIndex;
        public String sunrise, sunset, moonPhase, moonPhaseIcon;
    }

    public static class Aqi {
        public String aqi, level, category, pm2p5, pm10, so2, no2, co, o3;
    }

    public static class LifeIndex {
        public String name, type, level, category, text;
    }

    public static class Astronomy {
        public String sunrise, sunset, moonrise, moonset, moonPhase, moonPhaseIcon;
    }

    public static class Warning {
        public String title, text, typeName, level, startTime, endTime;
    }

    public interface Callback<T> {
        void onResult(T result);
        void onError(String message);
    }

    public static void getNow(String location, final Callback<Now> cb) {
        getJson("/v7/weather/now", location, new Callback<org.json.JSONObject>() {
            @Override
            public void onResult(org.json.JSONObject o) {
                try {
                    org.json.JSONObject n = o.getJSONObject("now");
                    Now now = new Now();
                    now.temp = n.optString("temp");
                    now.feelsLike = n.optString("feelsLike");
                    now.text = n.optString("text");
                    now.windDir = n.optString("windDir");
                    now.windScale = n.optString("windScale");
                    now.windSpeed = n.optString("windSpeed");
                    now.humidity = n.optString("humidity");
                    now.pressure = n.optString("pressure");
                    now.visib = n.optString("vis");
                    now.obsTime = n.optString("obsTime");
                    cb.onResult(now);
                } catch (Exception e) {
                    cb.onError("解析实时天气失败");
                }
            }

            @Override
            public void onError(String m) {
                cb.onError(m);
            }
        });
    }

    public static void getHourly24(final String location, final Callback<List<org.json.JSONObject>> cb) {
        getJson("/v7/weather/24h", location, new Callback<org.json.JSONObject>() {
            @Override
            public void onResult(org.json.JSONObject o) {
                try {
                    org.json.JSONArray arr = o.getJSONArray("hourly");
                    List<org.json.JSONObject> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(arr.getJSONObject(i));
                    }
                    cb.onResult(list);
                } catch (Exception e) {
                    cb.onError("解析24小时预报失败");
                }
            }

            @Override
            public void onError(String m) {
                cb.onError(m);
            }
        });
    }

    public static void getDaily15(final String location, final Callback<List<org.json.JSONObject>> cb) {
        getJson("/v7/weather/15d", location, new Callback<org.json.JSONObject>() {
            @Override
            public void onResult(org.json.JSONObject o) {
                try {
                    org.json.JSONArray arr = o.getJSONArray("daily");
                    List<org.json.JSONObject> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(arr.getJSONObject(i));
                    }
                    cb.onResult(list);
                } catch (Exception e) {
                    cb.onError("解析15天预报失败");
                }
            }

            @Override
            public void onError(String m) {
                cb.onError(m);
            }
        });
    }

    public static void getAqi(final String location, final Callback<org.json.JSONObject> cb) {
        getJson("/v7/air/now", location, new Callback<org.json.JSONObject>() {
            @Override
            public void onResult(org.json.JSONObject o) {
                try {
                    org.json.JSONObject n = o.getJSONObject("now");
                    cb.onResult(n);
                } catch (Exception e) {
                    cb.onError("解析空气质量失败");
                }
            }

            @Override
            public void onError(String m) {
                cb.onError(m);
            }
        });
    }

    public static void getLifeIndex(final String location, final Callback<List<org.json.JSONObject>> cb) {
        getJson("/v7/indices/1d", location, new Callback<org.json.JSONObject>() {
            @Override
            public void onResult(org.json.JSONObject o) {
                try {
                    org.json.JSONArray arr = o.getJSONArray("daily");
                    List<org.json.JSONObject> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(arr.getJSONObject(i));
                    }
                    cb.onResult(list);
                } catch (Exception e) {
                    cb.onError("解析生活指数失败");
                }
            }

            @Override
            public void onError(String m) {
                cb.onError(m);
            }
        });
    }

    public static void getAstronomy(final String location, final Callback<org.json.JSONObject> cb) {
        getJson("/v7/astronomy/sun", location, new Callback<org.json.JSONObject>() {
            @Override
            public void onResult(org.json.JSONObject o) {
                try {
                    org.json.JSONObject s = o.getJSONObject("sun");
                    String sunrise = s.optString("sunrise");
                    String sunset = s.optString("sunset");

                    getJson("/v7/astronomy/moon", location, new Callback<org.json.JSONObject>() {
                        @Override
                        public void onResult(org.json.JSONObject mo) {
                            try {
                                org.json.JSONObject m = mo.getJSONObject("moon");
                                org.json.JSONObject result = new org.json.JSONObject();
                                result.put("sunrise", sunrise);
                                result.put("sunset", sunset);
                                result.put("moonrise", m.optString("moonrise"));
                                result.put("moonset", m.optString("moonset"));
                                result.put("moonPhase", m.optString("moonPhase"));
                                result.put("moonPhaseIcon", m.optString("moonPhaseIcon"));
                                cb.onResult(result);
                            } catch (Exception e) {
                                cb.onError("解析月相失败");
                            }
                        }

                        @Override
                        public void onError(String m) {
                            cb.onError(m);
                        }
                    });
                } catch (Exception e) {
                    cb.onError("解析日出日落失败");
                }
            }

            @Override
            public void onError(String m) {
                cb.onError(m);
            }
        });
    }

    public static void getWarning(final String location, final Callback<List<org.json.JSONObject>> cb) {
        getJson("/v7/warning/now", location, new Callback<org.json.JSONObject>() {
            @Override
            public void onResult(org.json.JSONObject o) {
                try {
                    List<org.json.JSONObject> list = new ArrayList<>();
                    if (!o.isNull("warning")) {
                        org.json.JSONArray arr = o.getJSONArray("warning");
                        for (int i = 0; i < arr.length(); i++) {
                            list.add(arr.getJSONObject(i));
                        }
                    }
                    cb.onResult(list);
                } catch (Exception e) {
                    cb.onError("解析预警失败");
                }
            }

            @Override
            public void onError(String m) {
                cb.onError(m);
            }
        });
    }

    /** 城市名反查坐标（和风 GeoAPI） */
    public static void searchCity(String name, final Callback<String[]> cb) {
        getJson("/geo/v2/city/lookup?location=" + urlEncode(name), null,
                new Callback<org.json.JSONObject>() {
                    @Override
                    public void onResult(org.json.JSONObject o) {
                        try {
                            org.json.JSONArray arr = o.getJSONArray("location");
                            if (arr.length() > 0) {
                                org.json.JSONObject first = arr.getJSONObject(0);
                                cb.onResult(new String[]{
                                        first.optString("name"),
                                        first.optString("lon"),
                                        first.optString("lat")
                                });
                                return;
                            }
                            cb.onError("未找到该城市");
                        } catch (Exception e) {
                            cb.onError("城市查询失败");
                        }
                    }

                    @Override
                    public void onError(String m) {
                        cb.onError(m);
                    }
                });
    }

    /** 联网搜索城市/区县级地点（GeoAPI，最多10条） */
    public static void searchCityList(String name, final Callback<List<String[]>> cb) {
        getJson("/geo/v2/city/lookup?location=" + urlEncode(name) + "&number=10", null,
                new Callback<org.json.JSONObject>() {
                    @Override
                    public void onResult(org.json.JSONObject o) {
                        try {
                            org.json.JSONArray arr = o.getJSONArray("location");
                            List<String[]> list = new ArrayList<>();
                            for (int i = 0; i < arr.length(); i++) {
                                org.json.JSONObject c = arr.getJSONObject(i);
                                String[] r = new String[5];
                                r[0] = c.optString("name");
                                r[1] = c.optString("adm1");
                                r[2] = c.optString("adm2");
                                r[3] = c.optString("lon");
                                r[4] = c.optString("lat");
                                list.add(r);
                            }
                            cb.onResult(list);
                        } catch (Exception e) {
                            cb.onError("城市查询失败");
                        }
                    }

                    @Override
                    public void onError(String m) {
                        cb.onError(m);
                    }
                });
    }

    /** 坐标反查城市名（和风 GeoAPI） */
    public static void reverseGeo(String lonLat, final Callback<String> cb) {
        getJson("/geo/v2/city/lookup", lonLat, new Callback<org.json.JSONObject>() {
            @Override
            public void onResult(org.json.JSONObject o) {
                try {
                    org.json.JSONArray arr = o.getJSONArray("location");
                    if (arr.length() > 0) {
                        org.json.JSONObject c = arr.getJSONObject(0);
                        String adm2 = c.optString("adm2", "");
                        String name = c.optString("name", "");
                        if (!adm2.isEmpty() && !adm2.equals(name)) {
                            cb.onResult(adm2 + " " + name);
                        } else {
                            cb.onResult(name);
                        }
                        return;
                    }
                    cb.onResult(null);
                } catch (Exception e) {
                    cb.onResult(null);
                }
            }

            @Override
            public void onError(String m) {
                cb.onResult(null);
            }
        });
    }

    private static String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    /** path: /v7/... 或 /geo/...；location 可为 "lon,lat" 或 null（参数已拼入 path） */
    private static void getJson(final String path, final String location,
                                final Callback<org.json.JSONObject> cb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder sb = new StringBuilder();
                    if (location == null) {
                        sb.append(path); // 参数已在path中
                    } else {
                        sb.append(path).append("?location=").append(location);
                    }
                    sb.append("&key=").append(API_KEY)
                      .append("&lang=zh&unit=m");
                    URL url = new URL("https://" + API_HOST + sb.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setRequestProperty("X-QW-Api-Key", API_KEY);
                    conn.setRequestMethod("GET");
                    int code = conn.getResponseCode();
                    InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    StringBuilder resp = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        resp.append(line);
                    }
                    reader.close();
                    conn.disconnect();
                    if (code != 200) {
                        cb.onError("网络错误 HTTP " + code);
                        return;
                    }
                    org.json.JSONObject json = new org.json.JSONObject(resp.toString());
                    if (!"200".equals(json.optString("code"))) {
                        cb.onError("接口错误 code=" + json.optString("code"));
                        return;
                    }
                    cb.onResult(json);
                } catch (Exception e) {
                    cb.onError(e.getMessage() == null ? "网络请求失败" : e.getMessage());
                }
            }
        }).start();
    }

    public static String formatHour(String fxTime) {
        try {
            if (fxTime == null || fxTime.length() < 13) return "";
            String hh = fxTime.substring(11, 13);
            return hh + "时";
        } catch (Exception e) {
            return "";
        }
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
}