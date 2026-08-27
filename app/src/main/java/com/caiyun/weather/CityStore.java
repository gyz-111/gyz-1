package com.caiyun.weather;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 内置全国城市坐标库（assets/cities.json）
 */
public class CityStore {

    public static class City {
        public String name;   // 城市名
        public String prov;   // 省份
        public double lon, lat;

        City(String n, String p, double lo, double la) {
            name = n;
            prov = p;
            lon = lo;
            lat = la;
        }

        @Override
        public String toString() {
            return name + "，" + prov;
        }
    }

    private static List<City> all = new ArrayList<>();

    public static synchronized void load(Context ctx) {
        if (!all.isEmpty()) return;
        try {
            InputStream is = ctx.getAssets().open("cities.json");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            is.close();
            JSONArray arr = new JSONArray(new String(bos.toByteArray(), StandardCharsets.UTF_8));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject c = arr.getJSONObject(i);
                all.add(new City(c.optString("n"), c.optString("p"),
                        c.optDouble("lon"), c.optDouble("lat")));
            }
        } catch (Exception e) {
            Log.e("CityStore", "load failed", e);
        }
    }

    /** 关键词模糊搜索（先精确/前缀，再包含） */
    public static List<City> search(String keyword) {
        List<City> out = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return out;
        String k = keyword.trim();
        for (City c : all) {
            if (c.name.equals(k)) {
                out.add(0, c);
                break;
            }
        }
        for (City c : all) {
            if (!c.name.equals(k) && c.name.startsWith(k)) out.add(c);
            if (out.size() >= 20) return out;
        }
        for (City c : all) {
            if (!c.name.startsWith(k) && c.name.contains(k)) out.add(c);
            if (out.size() >= 20) return out;
        }
        for (City c : all) {
            if (c.prov != null && c.prov.contains(k)) {
                if (!out.contains(c)) out.add(c);
                if (out.size() >= 20) return out;
            }
        }
        return out;
    }

    public static int size() {
        return all.size();
    }
}
