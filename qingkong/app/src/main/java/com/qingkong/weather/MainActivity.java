package com.qingkong.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 晴空天气主界面
 * 实时天气 + 24小时温度曲线 + 未来15天 + 城市搜索/定位
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOCATION = 1001;
    private static final String PREF_NAME = "qingkong_weather";
    private static final String KEY_LOC = "loc";
    private static final String KEY_CITY = "city";
    /** 默认城市：洛阳 */
    private static final String DEFAULT_LOC = "112.4540,34.6200";
    private static final String DEFAULT_CITY = "洛阳";

    private SwipeRefreshLayout swipeRefresh;
    private NestedScrollView scrollRoot;
    private View bgView, iconHalo;
    private WeatherSceneView sceneView;
    private TextView tvCity, tvUpdateTime, tvTemp, tvCond, tvFeels, tvMainIcon;
    private TextView tvSunrise, tvSunset, tvMoon;
    private TextView[] infoValues = new TextView[6];
    private TextView btnToggleDays;
    private HourlyCurveView curveView;
    private RecyclerView dailyList;
    private DailyAdapter dailyAdapter;

    private LocationManager locationManager;
    private SharedPreferences prefs;
    private String currentLocation = DEFAULT_LOC;
    private String currentCity = DEFAULT_CITY;
    private boolean show15Days = false;
    private List<WeatherApi.Daily> allDaily = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        currentLocation = prefs.getString(KEY_LOC, DEFAULT_LOC);
        currentCity = prefs.getString(KEY_CITY, DEFAULT_CITY);
        CityStore.load(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        bindViews();
        setupListeners();

        tvCity.setText(currentCity);
        loadAll(false);

        new Handler(Looper.getMainLooper()).postDelayed(this::tryLocate, 1200);
    }

    private void bindViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        scrollRoot = findViewById(R.id.scrollRoot);
        bgView = findViewById(R.id.bgView);
        sceneView = findViewById(R.id.sceneView);
        iconHalo = findViewById(R.id.iconHalo);
        tvCity = findViewById(R.id.tvCity);
        tvUpdateTime = findViewById(R.id.tvUpdateTime);
        tvMainIcon = findViewById(R.id.tvMainIcon);
        tvTemp = findViewById(R.id.tvTemp);
        tvCond = findViewById(R.id.tvCond);
        tvFeels = findViewById(R.id.tvFeels);
        tvSunrise = findViewById(R.id.tvSunrise);
        tvSunset = findViewById(R.id.tvSunset);
        tvMoon = findViewById(R.id.tvMoon);
        btnToggleDays = findViewById(R.id.btnToggleDays);
        curveView = findViewById(R.id.curveView);
        dailyList = findViewById(R.id.dailyList);

        int[] infoIds = {R.id.infoValue0, R.id.infoValue1, R.id.infoValue2,
                R.id.infoValue3, R.id.infoValue4, R.id.infoValue5};
        for (int i = 0; i < 6; i++) infoValues[i] = findViewById(infoIds[i]);

        dailyList.setLayoutManager(new LinearLayoutManager(this));
        dailyAdapter = new DailyAdapter();
        dailyList.setAdapter(dailyAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> loadAll(false));
        swipeRefresh.setColorSchemeColors(0xFF67E8F9);
        swipeRefresh.setProgressBackgroundColorSchemeColor(0xFF0F172A);

        findViewById(R.id.btnLocate).setOnClickListener(v -> tryLocate());
        findViewById(R.id.btnCity).setOnClickListener(v -> showCityDialog());
        findViewById(R.id.btnCoord).setOnClickListener(v -> showCoordDialog());
        btnToggleDays.setOnClickListener(v -> toggleDailyView());
    }

    // ---------------- 定位 ----------------

    @SuppressLint("MissingPermission")
    private void tryLocate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
            return;
        }
        startLocate();
    }

    @SuppressLint("MissingPermission")
    private void startLocate() {
        List<String> providers = locationManager.getProviders(true);
        String chosen = null;
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            chosen = LocationManager.NETWORK_PROVIDER;
        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
            chosen = LocationManager.GPS_PROVIDER;
        } else if (!providers.isEmpty()) {
            chosen = providers.get(0);
        }
        if (chosen == null) {
            Toast.makeText(this, R.string.no_location, Toast.LENGTH_SHORT).show();
            return;
        }
        tvCity.setText("定位中…");
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location loc) {
                try { locationManager.removeUpdates(this); } catch (Exception ignored) {}
                applyLocated(loc.getLongitude(), loc.getLatitude());
            }

            @Override
            public void onProviderDisabled(@NonNull String p) {}
        };
        try {
            locationManager.requestSingleUpdate(chosen, listener, getMainLooper());
        } catch (Exception e) {
            Location last = null;
            try { last = locationManager.getLastKnownLocation(chosen); } catch (Exception ignored) {}
            if (last != null) applyLocated(last.getLongitude(), last.getLatitude());
            else tvCity.setText(currentCity);
        }
    }

    private void applyLocated(double lon, double lat) {
        currentLocation = String.format(Locale.CHINA, "%.4f,%.4f", lon, lat);
        saveCity(currentLocation, currentCity);
        WeatherApi.reverseGeo(currentLocation, new WeatherApi.Callback<String>() {
            @Override
            public void onResult(String name) {
                if (name != null && !name.isEmpty()) {
                    currentCity = name.replace(",", "");
                    saveCity(currentLocation, currentCity);
                    runOnUiThread(() -> tvCity.setText(currentCity));
                }
            }

            @Override
            public void onError(String m) {}
        });
        loadAll(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            boolean granted = false;
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_GRANTED) { granted = true; break; }
            }
            if (granted) startLocate();
            else Toast.makeText(this, R.string.locate_denied, Toast.LENGTH_LONG).show();
        }
    }

    // ---------------- 城市选择 ----------------

    private void showCoordDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);

        final EditText etLon = new EditText(this);
        etLon.setHint(R.string.longitude);
        etLon.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        box.addView(etLon);

        final EditText etLat = new EditText(this);
        etLat.setHint(R.string.latitude);
        etLat.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        box.addView(etLat);

        new AlertDialog.Builder(this)
                .setTitle(R.string.input_coord)
                .setView(box)
                .setPositiveButton("查询", (d, w) -> {
                    Double lon = parseCoord(etLon.getText().toString(), -180, 180);
                    Double lat = parseCoord(etLat.getText().toString(), -90, 90);
                    if (lon == null || lat == null) {
                        Toast.makeText(this, R.string.coord_invalid, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentCity = String.format(Locale.CHINA, "%.2f,%.2f", lon, lat);
                    applyLocated(lon, lat);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private Double parseCoord(String s, double min, double max) {
        if (TextUtils.isEmpty(s)) return null;
        try {
            double v = Double.parseDouble(s);
            if (v < min || v > max) return null;
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void showCityDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);

        final EditText etInput = new EditText(this);
        etInput.setHint(R.string.search_hint);
        etInput.setSingleLine(true);
        box.addView(etInput);

        TextView tvHot = new TextView(this);
        tvHot.setText(R.string.hot_cities);
        tvHot.setTextColor(Color.parseColor("#73FFFFFF"));
        tvHot.setTextSize(12);
        tvHot.setPadding(0, pad / 2, 0, pad / 2);
        box.addView(tvHot);

        StringBuilder sb = new StringBuilder();
        String[] hots = {"北京", "上海", "广州", "深圳", "洛阳", "郑州",
                "成都", "杭州", "武汉", "西安", "重庆", "南京"};
        for (String h : hots) {
            List<CityStore.City> r = CityStore.search(h);
            if (!r.isEmpty()) sb.append(r.get(0).name).append("  ");
        }
        TextView tvHotList = new TextView(this);
        tvHotList.setText(sb.toString().trim());
        tvHotList.setTextColor(Color.parseColor("#B3FFFFFF"));
        tvHotList.setTextSize(15);
        box.addView(tvHotList);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.select_city)
                .setView(box)
                .setPositiveButton("关闭", null)
                .create();

        tvHotList.setOnClickListener(v -> {
            String first = tvHotList.getText().toString().split("\\s+")[0];
            pickCity(first);
            dialog.dismiss();
        });

        etInput.setOnEditorActionListener((v, actionId, event) -> {
            searchOnlineCity(etInput.getText().toString());
            dialog.dismiss();
            return true;
        });

        dialog.show();
    }

    private void searchOnlineCity(String kw) {
        if (TextUtils.isEmpty(kw)) return;
        WeatherApi.searchCity(kw, new WeatherApi.Callback<List<String[]>>() {
            @Override
            public void onResult(final List<String[]> list) {
                runOnUiThread(() -> {
                    if (list == null || list.isEmpty()) {
                        List<CityStore.City> local = CityStore.search(kw);
                        if (!local.isEmpty()) pickCity(local.get(0).name);
                        else Toast.makeText(MainActivity.this, R.string.city_not_found,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showCityPickList(list);
                });
            }

            @Override
            public void onError(final String m) {
                runOnUiThread(() -> {
                    List<CityStore.City> local = CityStore.search(kw);
                    if (!local.isEmpty()) pickCity(local.get(0).name);
                    else Toast.makeText(MainActivity.this, m, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showCityPickList(final List<String[]> list) {
        String[] labels = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            String[] r = list.get(i);
            String city = r[2] == null || r[2].isEmpty() ? r[1] : r[2];
            labels[i] = r[0] + " · " + city
                    + (r[1] == null || r[1].isEmpty() || r[1].equals(city) ? "" : " · " + r[1]);
        }
        new AlertDialog.Builder(this)
                .setTitle("选择地点（" + list.size() + "个结果）")
                .setItems(labels, (d, w) -> {
                    String[] r = list.get(w);
                    currentLocation = r[3] + "," + r[4];
                    String city = r[2] == null || r[2].isEmpty() ? r[1] : r[2];
                    currentCity = r[0].equals(city) ? city + "，" + r[1] : city + " " + r[0] + "，" + r[1];
                    saveCity(currentLocation, currentCity);
                    tvCity.setText(currentCity);
                    loadAll(false);
                })
                .show();
    }

    private void pickCity(String cityName) {
        List<CityStore.City> res = CityStore.search(cityName);
        if (!res.isEmpty()) {
            CityStore.City c = res.get(0);
            currentLocation = String.format(Locale.CHINA, "%.4f,%.4f", c.lon, c.lat);
            currentCity = c.name + "，" + c.prov;
            saveCity(currentLocation, currentCity);
            tvCity.setText(currentCity);
            loadAll(false);
        }
    }

    private void saveCity(String loc, String city) {
        prefs.edit().putString(KEY_LOC, loc).putString(KEY_CITY, city).apply();
    }

    // ---------------- 数据加载 ----------------

    private void loadAll(boolean silent) {
        if (!silent) swipeRefresh.setRefreshing(true);
        final String loc = currentLocation;

        WeatherApi.getNow(loc, new WeatherApi.Callback<WeatherApi.Now>() {
            @Override
            public void onResult(final WeatherApi.Now now) {
                runOnUiThread(() -> renderNow(now));
            }

            @Override
            public void onError(final String m) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(MainActivity.this, m, Toast.LENGTH_SHORT).show();
                });
            }
        });

        WeatherApi.getHourly24(loc, new WeatherApi.Callback<List<WeatherApi.Hourly>>() {
            @Override
            public void onResult(List<WeatherApi.Hourly> list) {
                runOnUiThread(() -> curveView.setData(list));
            }

            @Override
            public void onError(String m) {}
        });

        WeatherApi.getDaily15(loc, new WeatherApi.Callback<List<WeatherApi.Daily>>() {
            @Override
            public void onResult(List<WeatherApi.Daily> list) {
                runOnUiThread(() -> {
                    allDaily.clear();
                    if (list != null) allDaily.addAll(list);
                    dailyAdapter.setData(allDaily, show15Days);
                    if (!allDaily.isEmpty()) {
                        WeatherApi.Daily d = allDaily.get(0);
                        tvSunrise.setText(WeatherApi.formatHM(d.sunrise));
                        tvSunset.setText(WeatherApi.formatHM(d.sunset));
                        tvMoon.setText(d.moonPhase.isEmpty() ? "--" : d.moonPhase);
                        String uv = d.uvIndex;
                        if (uv != null && !uv.isEmpty() && !uv.equals("0")) {
                            infoValues[5].setText(uv);
                        }
                    }
                    swipeRefresh.setRefreshing(false);
                });
            }

            @Override
            public void onError(String m) {
                runOnUiThread(() -> swipeRefresh.setRefreshing(false));
            }
        });
    }

    // ---------------- 渲染 ----------------

    private void renderNow(WeatherApi.Now n) {
        if (n.temp != null && !n.temp.isEmpty()) {
            tvTemp.setText(n.temp + "°");
            tvCond.setText(n.text);
            tvMainIcon.setText(WeatherApi.iconEmoji(n.icon, n.text));
            iconHalo.setBackgroundResource(haloRes(n.icon));
            tvFeels.setText(n.windDir + " " + n.windScale + "级 · 体感 " + n.feelsLike + "°");
            infoValues[0].setText(n.feelsLike + "°");
            infoValues[1].setText(n.humidity + "%");
            infoValues[2].setText(n.windDir + " " + n.windScale + "级");
            infoValues[3].setText(n.pressure + "hPa");
            infoValues[4].setText(n.visib + "km");
        }
        if (n.obsTime != null && n.obsTime.length() >= 16) {
            tvUpdateTime.setText(n.obsTime.substring(5, 16).replace("T", " ") + " 更新");
        } else {
            tvUpdateTime.setText(new SimpleDateFormat("M/d HH:mm", Locale.CHINA)
                    .format(new Date()) + " 更新");
        }
        applyBgByCondition(n.text);
        sceneView.setScene(sceneByIcon(n.icon, n.text));
    }

    /** 和风 icon 编号 → 渐变光晕底座 */
    private int haloRes(String icon) {
        if (icon != null && icon.startsWith("1")) {
            switch (icon) {
                case "100": return R.drawable.bg_halo_sunny;
                case "101":
                case "102":
                case "103": return R.drawable.bg_halo_cloudy;
                case "104": return R.drawable.bg_halo_overcast;
                case "150":
                case "151":
                case "152":
                case "153": return R.drawable.bg_halo_night;
                default: return R.drawable.bg_halo_sunny;
            }
        }
        if (icon != null && icon.startsWith("3")) {
            if ("302".equals(icon) || "303".equals(icon) || "304".equals(icon)
                    || "350".equals(icon)) return R.drawable.bg_halo_thunder;
            return R.drawable.bg_halo_rain;
        }
        if (icon != null && icon.startsWith("4")) return R.drawable.bg_halo_snow;
        if (icon != null && icon.startsWith("5")) return R.drawable.bg_halo_fog;
        return R.drawable.bg_halo_sunny;
    }

    /** 和风 icon 编号 → 动画场景 */
    private int sceneByIcon(String icon, String text) {
        if (icon != null && icon.startsWith("1")) {
            switch (icon) {
                case "100": return WeatherSceneView.SCENE_SUNNY;
                case "101":
                case "102":
                case "103": return WeatherSceneView.SCENE_CLOUDY;
                case "104": return WeatherSceneView.SCENE_OVERCAST;
                case "150":
                case "151":
                case "152":
                case "153": return WeatherSceneView.SCENE_NIGHT;
                default: return WeatherSceneView.SCENE_SUNNY;
            }
        }
        if (icon != null && icon.startsWith("3")) {
            if ("302".equals(icon) || "303".equals(icon) || "304".equals(icon)
                    || "350".equals(icon)) return WeatherSceneView.SCENE_THUNDER;
            return WeatherSceneView.SCENE_RAIN;
        }
        if (icon != null && icon.startsWith("4")) return WeatherSceneView.SCENE_SNOW;
        if (icon != null && icon.startsWith("5")) return WeatherSceneView.SCENE_FOG;
        if (icon == null && text != null) {
            if (text.contains("雷")) return WeatherSceneView.SCENE_THUNDER;
            if (text.contains("雨")) return WeatherSceneView.SCENE_RAIN;
            if (text.contains("雪")) return WeatherSceneView.SCENE_SNOW;
            if (text.contains("雾") || text.contains("霾")) return WeatherSceneView.SCENE_FOG;
            if (text.contains("晴")) return WeatherSceneView.SCENE_SUNNY;
            if (text.contains("云") || text.contains("阴")) return WeatherSceneView.SCENE_CLOUDY;
        }
        return WeatherSceneView.SCENE_NIGHT;
    }

    private void toggleDailyView() {
        show15Days = !show15Days;
        btnToggleDays.setText(show15Days ? "显示7天" : "显示15天");
        dailyAdapter.setData(allDaily, show15Days);
    }

    private void applyBgByCondition(String text) {
        int res;
        if (text == null) {
            res = R.drawable.bg_gradient_night;
        } else if (text.contains("雷") || text.contains("雨")) {
            res = R.drawable.bg_gradient_rain;
        } else if (text.contains("雪")) {
            res = R.drawable.bg_gradient_snow;
        } else if (text.contains("晴")) {
            res = R.drawable.bg_gradient_day;
        } else if (text.contains("云") || text.contains("阴")) {
            res = R.drawable.bg_gradient_cloudy;
        } else {
            res = R.drawable.bg_gradient_night;
        }
        bgView.setBackgroundResource(res);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sceneView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sceneView.start();
    }

    // ---------------- 适配器 ----------------

    static class DailyVH extends RecyclerView.ViewHolder {
        TextView dayLabel, dayIcon, dayCond, dayLow, dayHigh, dayPrecip;
        View barLeft, bar, barRight;

        DailyVH(View v) {
            super(v);
            dayLabel = v.findViewById(R.id.dayLabel);
            dayIcon = v.findViewById(R.id.dayIcon);
            dayCond = v.findViewById(R.id.dayCond);
            dayLow = v.findViewById(R.id.dayLow);
            dayHigh = v.findViewById(R.id.dayHigh);
            dayPrecip = v.findViewById(R.id.dayPrecip);
            barLeft = v.findViewById(R.id.dayBarLeft);
            bar = v.findViewById(R.id.dayBar);
            barRight = v.findViewById(R.id.dayBarRight);
        }
    }

    class DailyAdapter extends RecyclerView.Adapter<DailyVH> {
        private final List<WeatherApi.Daily> data = new ArrayList<>();
        private int weekMin = 0, weekMax = 1;
        private boolean show15 = false;

        void setData(List<WeatherApi.Daily> d, boolean show15) {
            this.show15 = show15;
            data.clear();
            if (d != null) data.addAll(show15 ? d : d.subList(0, Math.min(7, d.size())));
            weekMin = Integer.MAX_VALUE;
            weekMax = -Integer.MAX_VALUE;
            for (WeatherApi.Daily it : data) {
                try {
                    int lo = Integer.parseInt(it.tempMin);
                    int hi = Integer.parseInt(it.tempMax);
                    if (lo < weekMin) weekMin = lo;
                    if (hi > weekMax) weekMax = hi;
                } catch (Exception ignored) {}
            }
            if (weekMin == Integer.MAX_VALUE) { weekMin = 0; weekMax = 1; }
            if (weekMax <= weekMin) weekMax = weekMin + 1;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DailyVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_daily, parent, false);
            return new DailyVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DailyVH h, int position) {
            WeatherApi.Daily d = data.get(position);
            h.dayLabel.setText(WeatherApi.formatDayLabel(d.fxDate, position));
            // 今天/明天高亮
            if (position == 0) {
                h.dayLabel.setTextColor(Color.parseColor("#67E8F9"));
            } else if (position == 1) {
                h.dayLabel.setTextColor(Color.parseColor("#D0FFFFFF"));
            } else {
                h.dayLabel.setTextColor(Color.parseColor("#99FFFFFF"));
            }
            h.dayIcon.setText(WeatherApi.iconEmoji(d.iconDay, d.textDay));
            String cond = d.textDay.equals(d.textNight) ? d.textDay
                    : d.textDay + "转" + d.textNight;
            h.dayCond.setText(cond);

            int lo = weekMin, hi = weekMax;
            try {
                lo = Integer.parseInt(d.tempMin);
                h.dayLow.setText(lo + "°");
            } catch (Exception e) {
                h.dayLow.setText("--");
            }
            try {
                hi = Integer.parseInt(d.tempMax);
                h.dayHigh.setText(hi + "°");
            } catch (Exception e) {
                h.dayHigh.setText("--");
            }
            if (d.precip != null && !d.precip.equals("0")) {
                h.dayPrecip.setText(d.precip + "mm");
                h.dayPrecip.setVisibility(View.VISIBLE);
            } else {
                h.dayPrecip.setVisibility(View.GONE);
            }

            float span = weekMax - weekMin;
            float leftPct = Math.max(0f, Math.min(100f, (lo - weekMin) / span * 100f));
            float rightPct = Math.max(0f, Math.min(100f, (weekMax - hi) / span * 100f));
            float barPct = Math.max(6f, 100f - leftPct - rightPct);
            float total = leftPct + barPct + rightPct;

            LinearLayout.LayoutParams lpL = (LinearLayout.LayoutParams) h.barLeft.getLayoutParams();
            LinearLayout.LayoutParams lpB = (LinearLayout.LayoutParams) h.bar.getLayoutParams();
            LinearLayout.LayoutParams lpR = (LinearLayout.LayoutParams) h.barRight.getLayoutParams();
            lpL.weight = leftPct / total;
            lpB.weight = barPct / total;
            lpR.weight = rightPct / total;
            h.barLeft.setLayoutParams(lpL);
            h.bar.setLayoutParams(lpB);
            h.barRight.setLayoutParams(lpR);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
