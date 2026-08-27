package com.caiyun.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOCATION = 1001;

    private SwipeRefreshLayout swipeRefresh;
    private NestedScrollView scrollRoot;
    private TextView tvCity, tvUpdateTime, tvTemp, tvCondText, tvSummary;
    private TextView tvSunrise, tvSunset, tvMoonPhase, tvWarningTitle, tvWarningText;
    private TextView btnCoord, btnSearch, btnLocate, tvVersion, btnToggleDays;
    private LinearLayout warningBox;
    private View cardAqi, cardLifeIndex;
    private TextView tvAqiValue, tvAqiLevel, tvPm25, tvPm10, tvSo2, tvNo2, tvCo, tvO3;
    private RecyclerView lifeIndexList, dailyList, hourlyList;
    private LifeIndexAdapter lifeAdapter;
    private DailyAdapter dailyAdapter;
    private HourlyAdapter hourlyAdapter;
    private TextView[] infoValues = new TextView[6];

    private LocationManager locationManager;
    private String currentLocation = "116.4074,39.9042";
    private boolean show15Days = false;
    private List<WeatherApiClient.Daily> allDailyData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CityStore.load(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        bindViews();
        setupListeners();
        requestLocationAndLoad();

        // 启动时检查更新（显示弹窗），每次进入程序都检查
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> UpdateChecker.check(this, false), 1500);
    }

    private void bindViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        scrollRoot = findViewById(R.id.scrollRoot);
        tvCity = findViewById(R.id.tvCity);
        tvUpdateTime = findViewById(R.id.tvUpdateTime);
        tvTemp = findViewById(R.id.tvTemp);
        tvCondText = findViewById(R.id.tvCondText);
        tvSummary = findViewById(R.id.tvSummary);
        tvSunrise = findViewById(R.id.tvSunrise);
        tvSunset = findViewById(R.id.tvSunset);
        tvMoonPhase = findViewById(R.id.tvMoonPhase);
        tvWarningTitle = findViewById(R.id.tvWarningTitle);
        tvWarningText = findViewById(R.id.tvWarningHint);
        btnCoord = findViewById(R.id.btnCoord);
        btnSearch = findViewById(R.id.btnSearch);
        btnLocate = findViewById(R.id.btnLocate);
        tvVersion = findViewById(R.id.tvVersion);
        btnToggleDays = findViewById(R.id.btnToggleDays);
        warningBox = findViewById(R.id.warningBox);
        cardAqi = findViewById(R.id.cardAqi);
        cardLifeIndex = findViewById(R.id.cardLifeIndex);
        tvAqiValue = findViewById(R.id.tvAqiValue);
        tvAqiLevel = findViewById(R.id.tvAqiLevel);
        tvPm25 = findViewById(R.id.tvPm25);
        tvPm10 = findViewById(R.id.tvPm10);
        tvSo2 = findViewById(R.id.tvSo2);
        tvNo2 = findViewById(R.id.tvNo2);
        tvCo = findViewById(R.id.tvCo);
        tvO3 = findViewById(R.id.tvO3);
        hourlyList = findViewById(R.id.hourlyList);
        lifeIndexList = findViewById(R.id.lifeIndexList);
        dailyList = findViewById(R.id.dailyList);
        btnToggleDays = findViewById(R.id.btnToggleDays);

        // 信息宫格
        int[] infoIds = {R.id.infoValue0, R.id.infoValue1, R.id.infoValue2,
                R.id.infoValue3, R.id.infoValue4, R.id.infoValue5};
        for (int i = 0; i < 6; i++) {
            infoValues[i] = findViewById(infoIds[i]);
        }

        // AQI 子项标签（直接复用值 TextView）
        tvPm25 = findViewById(R.id.tvPm25);
        tvPm10 = findViewById(R.id.tvPm10);
        tvSo2 = findViewById(R.id.tvSo2);
        tvNo2 = findViewById(R.id.tvNo2);
        tvCo = findViewById(R.id.tvCo);
        tvO3 = findViewById(R.id.tvO3);

        // 适配器
        lifeIndexList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        lifeAdapter = new LifeIndexAdapter();
        lifeIndexList.setAdapter(lifeAdapter);

        hourlyList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hourlyAdapter = new HourlyAdapter();
        hourlyList.setAdapter(hourlyAdapter);

        dailyList.setLayoutManager(new LinearLayoutManager(this));
        dailyAdapter = new DailyAdapter();
        dailyList.setAdapter(dailyAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> loadAll(false));
        swipeRefresh.setColorSchemeColors(0xFF4FC3F7);
        swipeRefresh.setProgressBackgroundColorSchemeColor(0xFF16264E);

        btnLocate.setOnClickListener(v -> requestLocationAndLoad());
        btnCoord.setOnClickListener(v -> showCoordDialog());
        btnSearch.setOnClickListener(v -> showCityDialog());
        warningBox.setOnClickListener(v -> showWarningsDialog());
        tvVersion.setOnClickListener(v -> UpdateChecker.check(this, false));
        btnToggleDays.setOnClickListener(v -> toggleDailyView());
        tvVersion.setOnClickListener(v -> UpdateChecker.check(this, false));
    }

    private void toggleDailyView() {
        show15Days = !show15Days;
        btnToggleDays.setText(show15Days ? "显示7天" : "显示15天");
        dailyAdapter.setData(allDailyData, show15Days);
    }

    // ---------- 定位 ----------

    private void requestLocationAndLoad() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
            loadAll(false); // 先用当前坐标加载
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
            Toast.makeText(this, "无可用定位方式，使用默认城市", Toast.LENGTH_SHORT).show();
            loadAll(false);
            return;
        }
        tvCity.setText("定位中…");
        final String provider = chosen;
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location loc) {
                try {
                    locationManager.removeUpdates(this);
                } catch (Exception ignored) {
                }
                applyLocated(loc.getLongitude(), loc.getLatitude());
            }

            @Override
            public void onProviderDisabled(@NonNull String p) {
            }
        };
        try {
            locationManager.requestSingleUpdate(provider, listener, getMainLooper());
        } catch (Exception e) {
            Location last = null;
            try {
                last = locationManager.getLastKnownLocation(provider);
            } catch (Exception ignored) {
            }
            if (last != null) {
                applyLocated(last.getLongitude(), last.getLatitude());
            } else {
                loadAll(false);
            }
        }
    }

    /** 定位成功：反查城市名并刷新天气 */
    private void applyLocated(double lon, double lat) {
        currentLocation = String.format(Locale.CHINA, "%.4f,%.4f", lon, lat);
        WeatherApiClient.reverseGeo(currentLocation,
                new WeatherApiClient.Callback<String>() {
                    @Override
                    public void onResult(String name) {
                        if (name != null && !name.isEmpty()) {
                            final String display = name.replace(",", "");
                            runOnUiThread(() -> tvCity.setText(display));
                        }
                    }

                    @Override
                    public void onError(String m) {
                    }
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
                if (r == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            if (granted) {
                startLocate();
            } else {
                Toast.makeText(this, "未授权定位，可手动选择城市或输入坐标",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // ---------- 坐标 / 城市查询对话框 ----------

    private void showCoordDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);

        final EditText etLon = new EditText(this);
        etLon.setHint(R.string.longitude);
        etLon.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        box.addView(etLon);

        final EditText etLat = new EditText(this);
        etLat.setHint(R.string.latitude);
        etLat.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        box.addView(etLat);

        new AlertDialog.Builder(this)
                .setTitle(R.string.input_coord)
                .setView(box)
                .setPositiveButton("查询", (d, w) -> {
                    String lonS = etLon.getText().toString().trim();
                    String latS = etLat.getText().toString().trim();
                    Double lon = parseCoord(lonS, -180, 180);
                    Double lat = parseCoord(latS, -90, 90);
                    if (lon == null || lat == null) {
                        Toast.makeText(this, "坐标格式不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }
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
        List<CityStore.City> hot = new ArrayList<>();
        String[] hots = {"北京", "上海", "广州", "深圳", "郑州",
                "成都", "杭州", "武汉", "西安", "重庆"};
        for (String h : hots) {
            List<CityStore.City> r = CityStore.search(h);
            if (!r.isEmpty()) hot.add(r.get(0));
        }

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);

        final EditText etInput = new EditText(this);
        etInput.setHint("搜索全国任意城市");
        etInput.setSingleLine(true);
        box.addView(etInput);

        TextView tvHot = new TextView(this);
        tvHot.setText("热门城市");
        tvHot.setTextColor(Color.parseColor("#73FFFFFF"));
        tvHot.setTextSize(12);
        tvHot.setPadding(0, pad / 2, 0, pad / 2);
        box.addView(tvHot);

        StringBuilder sb = new StringBuilder();
        for (CityStore.City c : hot) {
            sb.append(c.name).append("  ");
        }
        TextView tvHotList = new TextView(this);
        tvHotList.setText(sb.toString().trim());
        tvHotList.setTextColor(Color.parseColor("#B3FFFFFF"));
        tvHotList.setTextSize(15);
        box.addView(tvHotList);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("选择城市")
                .setView(box)
                .setPositiveButton("关闭", null)
                .create();

        tvHotList.setOnClickListener(v -> {
            String first = tvHotList.getText().toString().split("\\s+")[0];
            pickCity(first);
            dialog.dismiss();
        });

        etInput.setOnEditorActionListener((v, actionId, event) -> {
            String kw = etInput.getText().toString();
            searchOnlineCity(kw);
            dialog.dismiss();
            return true;
        });

        dialog.show();
    }

    /** 联网搜索（含全国区县级），弹出候选列表 */
    private void searchOnlineCity(String kw) {
        if (TextUtils.isEmpty(kw)) return;
        WeatherApiClient.searchCityList(kw,
                new WeatherApiClient.Callback<List<String[]>>() {
                    @Override
                    public void onResult(final List<String[]> list) {
                        runOnUiThread(() -> {
                            if (list == null || list.isEmpty()) {
                                Toast.makeText(MainActivity.this, "未找到该地点",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            showCityPickList(list);
                        });
                    }

                    @Override
                    public void onError(final String m) {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, m,
                                        Toast.LENGTH_SHORT).show());
                    }
                });
    }

    /** 区县级候选选择列表：名称 · 市 · 省 */
    private void showCityPickList(final List<String[]> list) {
        String[] labels = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            String[] r = list.get(i);
            String city = r[2] == null || r[2].isEmpty() ? r[1] : r[2];
            labels[i] = r[0] + " · " + city + (r[1] == null || r[1].isEmpty()
                    || r[1].equals(city) ? "" : " · " + r[1]);
        }
        new AlertDialog.Builder(this)
                .setTitle("选择地点（" + list.size() + "个结果）")
                .setItems(labels, (d, w) -> {
                    String[] r = list.get(w);
                    currentLocation = r[3] + "," + r[4];
                    String city = r[2] == null || r[2].isEmpty() ? r[1] : r[2];
                    String display = r[0].equals(city)
                            ? city + "，" + r[1]
                            : city + " " + r[0] + "，" + r[1];
                    tvCity.setText(display);
                    loadAll(false);
                })
                .show();
    }

    private void pickCity(String cityName) {
        List<CityStore.City> res = CityStore.search(cityName);
        if (!res.isEmpty()) {
            CityStore.City c = res.get(0);
            currentLocation = String.format(Locale.CHINA, "%.4f,%.4f", c.lon, c.lat);
            tvCity.setText(c.name + "，" + c.prov);
            loadAll(false);
        }
    }

    // ---------- 预警详情弹窗 ----------

    private void showWarningsDialog() {
        // 实现类似之前
    }

    // ---------- 数据加载与渲染 ----------

    private void loadAll(boolean silent) {
        if (!silent) {
            swipeRefresh.setRefreshing(true);
        }
        final String loc = currentLocation;

        // 并行请求所有数据
        WeatherApiClient.getNow(loc, new WeatherApiClient.Callback<WeatherApiClient.Now>() {
            @Override
            public void onResult(final WeatherApiClient.Now now) {
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

        WeatherApiClient.getHourly24(loc,
                new WeatherApiClient.Callback<List<org.json.JSONObject>>() {
                    @Override
                    public void onResult(List<org.json.JSONObject> list) {
                        runOnUiThread(() -> hourlyAdapter.setData(list));
                    }

                    @Override
                    public void onError(String m) {
                    }
                });

        WeatherApiClient.getDaily15(loc,
                new WeatherApiClient.Callback<List<org.json.JSONObject>>() {
                    @Override
                    public void onResult(List<org.json.JSONObject> list) {
                        runOnUiThread(() -> {
                            allDailyData.clear();
                            if (list != null) {
                                for (org.json.JSONObject o : list) {
                                    allDailyData.add(toDaily(o));
                                }
                            }
                            dailyAdapter.setData(allDailyData, show15Days);
                            if (!allDailyData.isEmpty()) {
                                String uv = allDailyData.get(0).uvIndex;
                                if (uv != null && !uv.isEmpty() && !uv.equals("0")) {
                                    infoValues[5].setText(uv);
                                }
                            }
                            swipeRefresh.setRefreshing(false);
                        });
                    }

                    @Override
                    public void onError(final String m) {
                        runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                    }
                });

        WeatherApiClient.getWarning(loc,
                new WeatherApiClient.Callback<List<org.json.JSONObject>>() {
                    @Override
                    public void onResult(List<org.json.JSONObject> list) {
                        List<WeatherApiClient.Warning> warnings = new ArrayList<>();
                        if (list != null) {
                            for (org.json.JSONObject o : list) {
                                warnings.add(toWarning(o));
                            }
                        }
                        runOnUiThread(() -> renderWarnings(warnings));
                    }

                    @Override
                    public void onError(String m) {
                    }
                });

        // 空气质量
        WeatherApiClient.getAqi(currentLocation,
                new WeatherApiClient.Callback<org.json.JSONObject>() {
                    @Override
                    public void onResult(org.json.JSONObject o) {
                        try {
                            org.json.JSONObject n = o.getJSONObject("now");
                            runOnUiThread(() -> renderAqi(n));
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onError(String m) {
                    }
                });

        WeatherApiClient.getLifeIndex(currentLocation,
                new WeatherApiClient.Callback<List<org.json.JSONObject>>() {
                    @Override
                    public void onResult(List<org.json.JSONObject> list) {
                        runOnUiThread(() -> lifeAdapter.setData(list));
                    }

                    @Override
                    public void onError(String m) {
                    }
                });

        // 天文数据
        WeatherApiClient.getAstronomy(currentLocation,
                new WeatherApiClient.Callback<org.json.JSONObject>() {
                    @Override
                    public void onResult(org.json.JSONObject o) {
                        try {
                            org.json.JSONObject s = o.getJSONObject("sun");
                            String sunrise = s.optString("sunrise");
                            String sunset = s.optString("sunset");

                            WeatherApiClient.getAstronomy(currentLocation, 
                                new WeatherApiClient.Callback<org.json.JSONObject>() {
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
                                            runOnUiThread(() -> renderAstronomy(result));
} catch (Exception e) {
                // 解析失败时显示默认值
                tvSunrise.setText("日出 --:--");
                tvSunset.setText("日落 --:--");
                tvMoonPhase.setText("月相 --");
            }
        }
                                    }

                                    @Override
                                    public void onError(String m) {
                                    }
                                });
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onError(String m) {
                    }
                });
    }

    // ---------- 渲染方法 ----------

    private void renderNow(WeatherApiClient.Now n) {
        if (n.temp != null && !n.temp.isEmpty()) {
            tvTemp.setText(n.temp + "°");
            tvCondText.setText(n.text);
            String wind = n.windDir + " " + n.windScale + "级";
            tvSummary.setText(wind + " · 体感 " + n.feelsLike + "°");
            infoValues[0].setText(n.feelsLike + "°");
            infoValues[1].setText(n.humidity + "%");
            infoValues[2].setText(wind);
            infoValues[3].setText(n.pressure + "hPa");
            infoValues[4].setText(n.visib + "km");
        }
        if (n.obsTime != null && n.obsTime.length() >= 16) {
            tvUpdateTime.setText(n.obsTime.substring(5, 16).replace("T", " ") + " 更新");
        } else {
            tvUpdateTime.setText(new SimpleDateFormat("M/d HH:mm", Locale.CHINA).format(new Date()) + " 更新");
        }
        applyBgByCondition(n.text);
    }

    private void renderAqi(org.json.JSONObject a) {
        cardAqi.setVisibility(View.VISIBLE);
        String aqi = a.optString("aqi", "");
        if (!aqi.isEmpty()) {
            tvAqiValue.setText("AQI " + aqi);
        } else {
            tvAqiValue.setText("AQI --");
        }
        
        String level = a.optString("level", "");
        if (!level.isEmpty()) {
            tvAqiLevel.setText(level);
            int color = getAqiColor(level);
            tvAqiLevel.setBackgroundColor(color);
        } else {
            tvAqiLevel.setText("级别 --");
            tvAqiLevel.setBackgroundColor(Color.parseColor("#00E676"));
        }
        
        tvPm25.setText(a.optString("pm2p5", "--") + "μg/m³");
        tvPm10.setText(a.optString("pm10", "--") + "μg/m³");
        tvSo2.setText(a.optString("so2", "--") + "μg/m³");
        tvNo2.setText(a.optString("no2", "--") + "μg/m³");
        tvCo.setText(a.optString("co", "--") + "mg/m³");
        tvO3.setText(a.optString("o3", "--") + "μg/m³");
    }

    private int getAqiColor(String level) {
        if (level == null) return Color.parseColor("#00E676");
        switch (level) {
            case "优": return Color.parseColor("#00E676");
            case "良": return Color.parseColor("#4CAF50");
            case "轻度污染": return Color.parseColor("#FFEB3B");
            case "中度污染": return Color.parseColor("#FF9800");
            case "重度污染": return Color.parseColor("#F44336");
            case "严重污染": return Color.parseColor("#9C27B0");
            default: return Color.parseColor("#00E676");
        }
    }

    private void renderAstronomy(org.json.JSONObject astro) {
        try {
            String sunrise = astro.optString("sunrise", "");
            String sunset = astro.optString("sunset", "");
            String moonPhase = astro.optString("moonPhase", "");
            
            if (!sunrise.isEmpty()) {
                String time = sunrise.length() >= 16 ? sunrise.substring(11, 16) : sunrise;
                tvSunrise.setText("日出 " + time);
            } else {
                tvSunrise.setText("日出 --:--");
            }
            
            if (!sunset.isEmpty()) {
                String time = sunset.length() >= 16 ? sunset.substring(11, 16) : sunset;
                tvSunset.setText("日落 " + time);
            } else {
                tvSunset.setText("日落 --:--");
            }
            
            if (!TextUtils.isEmpty(moonPhase)) {
                tvMoonPhase.setText("月相 " + moonPhase);
            } else {
                tvMoonPhase.setText("月相 --");
            }
        } catch (Exception e) {
            tvSunrise.setText("日出 --:--");
            tvSunset.setText("日落 --:--");
            tvMoonPhase.setText("月相 --");
        }
    }

    private void renderWarnings(List<WeatherApiClient.Warning> list) {
        List<WeatherApiClient.Warning> active = new ArrayList<>();
        if (list != null) {
            Date now = new Date();
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.CHINA);
            for (WeatherApiClient.Warning w : list) {
                boolean keep = true;
                if (w.endTime != null && w.endTime.length() >= 16) {
                    try {
                        Date end = iso.parse(w.endTime.substring(0, 16));
                        if (end != null && end.before(now)) keep = false;
                    } catch (Exception ignored) {
                    }
                }
                if (keep) active.add(w);
            }
        }
        if (active.isEmpty()) {
            warningBox.setVisibility(View.GONE);
            return;
        }
        warningBox.setVisibility(View.VISIBLE);
        WeatherApiClient.Warning w = active.get(0);
        tvWarningTitle.setText(active.size() > 1
                ? w.typeName + w.level + "预警 等" + active.size() + "条预警"
                : w.title);
    }

    /** 根据天气现象切换背景渐变 */
    private void applyBgByCondition(String text) {
        int res;
        if (text == null) {
            res = R.drawable.bg_gradient_night;
        } else if (text.contains("雷") || text.contains("雨")) {
            res = R.drawable.bg_gradient_rain;
        } else if (text.contains("雪")) {
            res = R.drawable.bg_gradient_snow;
        } else if (text.contains("晴") || text.contains("云")) {
            res = R.drawable.bg_gradient_day;
        } else {
            res = R.drawable.bg_gradient_night;
        }
        scrollRoot.setBackgroundResource(res);
    }

    // ---------- 7天/15天列表适配器 ----------

    static class DailyVH extends RecyclerView.ViewHolder {
        TextView dayLabel, dayCond, dayLow, dayHigh, dayPrecip;
        View barLeft, bar, barRight;

        DailyVH(View v) {
            super(v);
            dayLabel = v.findViewById(R.id.dayLabel);
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
        private final List<WeatherApiClient.Daily> data = new ArrayList<>();
        private int weekMin = 0, weekMax = 1;
        private boolean show15 = false;

        void setData(List<WeatherApiClient.Daily> d, boolean show15) {
            this.show15 = show15;
            data.clear();
            if (d != null) data.addAll(show15 ? d : d.subList(0, Math.min(7, d.size())));
            weekMin = Integer.MAX_VALUE;
            weekMax = -Integer.MAX_VALUE;
            for (WeatherApiClient.Daily it : data) {
                try {
                    int lo = Integer.parseInt(it.tempMin);
                    int hi = Integer.parseInt(it.tempMax);
                    if (lo < weekMin) weekMin = lo;
                    if (hi > weekMax) weekMax = hi;
                } catch (Exception ignored) {
                }
            }
            if (weekMin == Integer.MAX_VALUE) {
                weekMin = 0;
                weekMax = 1;
            }
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
            WeatherApiClient.Daily d = data.get(position);
            h.dayLabel.setText(WeatherApiClient.formatDayLabel(d.fxDate, position));
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

            // 温度范围条：按本周最低/最高计算左右留白权重
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

    // ---------- 生活指数适配器 ----------

    static class LifeVH extends RecyclerView.ViewHolder {
        TextView lifeIcon, lifeName, lifeLevel, lifeDesc;

        LifeVH(View v) {
            super(v);
            lifeIcon = v.findViewById(R.id.lifeIcon);
            lifeName = v.findViewById(R.id.lifeName);
            lifeLevel = v.findViewById(R.id.lifeLevel);
            lifeDesc = v.findViewById(R.id.lifeDesc);
        }
    }

    class LifeIndexAdapter extends RecyclerView.Adapter<LifeVH> {
        private final List<org.json.JSONObject> data = new ArrayList<>();

        void setData(List<org.json.JSONObject> d) {
            data.clear();
            if (d != null) data.addAll(d);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public LifeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_life_index, parent, false);
            return new LifeVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull LifeVH h, int position) {
            org.json.JSONObject li = data.get(position);
            h.lifeName.setText(li.optString("name", ""));
            h.lifeLevel.setText(li.optString("level", "--"));
            h.lifeDesc.setText(li.optString("text", ""));

            // 图标映射
            String icon = getLifeIcon(li.optString("type", ""));
            h.lifeIcon.setText(icon);
            h.lifeLevel.setText(li.optString("level", "--"));
        }

        private static String getLifeIcon(String type) {
            if (type == null) return "天气";
            switch (type) {
                case "dressing": return "穿衣";
                case "uv": return "紫外线";
                case "carWashing": return "洗车";
                case "sport": return "运动";
                case "fishing": return "钓鱼";
                case "travel": return "旅游";
                case "allergy": return "过敏";
                case "cold": return "感冒";
                case "makeup": return "化妆";
                case "drying": return "晾晒";
                case "traffic": return "交通";
                case "tourism": return "旅游";
                default: return "天气";
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    // ---------- 小时预报适配器 ----------

    static class HourlyVH extends RecyclerView.ViewHolder {
        TextView hourTemp, hourCond, hourPop, hourTime;

        HourlyVH(View v) {
            super(v);
            hourTemp = v.findViewById(R.id.hourTemp);
            hourCond = v.findViewById(R.id.hourCond);
            hourPop = v.findViewById(R.id.hourPop);
            hourTime = v.findViewById(R.id.hourTime);
        }
    }

    static class HourlyAdapter extends RecyclerView.Adapter<HourlyVH> {
        private final List<org.json.JSONObject> data = new ArrayList<>();

        void setData(List<org.json.JSONObject> d) {
            data.clear();
            if (d != null) data.addAll(d);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public HourlyVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hourly, parent, false);
            return new HourlyVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull HourlyVH h, int position) {
            org.json.JSONObject hObj = data.get(position);
            h.hourTemp.setText(hObj.optString("temp") + "°");
            h.hourCond.setText(hObj.optString("text"));
            int pop = hObj.optInt("pop", 0);
            if (pop > 0) {
                h.hourPop.setText(pop + "%");
                h.hourPop.setVisibility(View.VISIBLE);
            } else {
                h.hourPop.setVisibility(View.GONE);
            }
            String fxTime = hObj.optString("fxTime");
            h.hourTime.setText(WeatherApiClient.formatHour(fxTime));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    // ---------- 工具 ----------

    private String nowText() {
        return new SimpleDateFormat("M/d HH:mm:ss", Locale.CHINA).format(new Date());
    }

    // ---------- JSONObject -> POJO 转换 ----------

    private static WeatherApiClient.Daily toDaily(org.json.JSONObject o) {
        WeatherApiClient.Daily d = new WeatherApiClient.Daily();
        d.fxDate = o.optString("fxDate");
        d.tempMax = o.optString("tempMax");
        d.tempMin = o.optString("tempMin");
        d.textDay = o.optString("textDay");
        d.textNight = o.optString("textNight");
        d.precip = o.optString("precip");
        d.uvIndex = o.optString("uvIndex");
        d.sunrise = o.optString("sunrise");
        d.sunset = o.optString("sunset");
        d.moonPhase = o.optString("moonPhase");
        d.moonPhaseIcon = o.optString("moonPhaseIcon");
        return d;
    }

    private static WeatherApiClient.Warning toWarning(org.json.JSONObject o) {
        WeatherApiClient.Warning w = new WeatherApiClient.Warning();
        w.title = o.optString("title");
        w.text = o.optString("text");
        w.typeName = o.optString("typeName");
        w.level = o.optString("level");
        w.startTime = o.optString("startTime");
        w.endTime = o.optString("endTime");
        return w;
    }
}