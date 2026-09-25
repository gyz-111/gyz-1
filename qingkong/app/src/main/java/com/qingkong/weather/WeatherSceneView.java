package com.qingkong.weather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * 动态天气场景背景：根据天气类型播放对应氛围动画
 * 晴(太阳光晕/光线) / 多云(云朵飘动) / 阴(灰云层) / 雨(雨丝) / 雷(闪电) /
 * 雪(雪花) / 雾(雾带) / 夜(星空+月亮)
 * 纯 Canvas 轻量绘制，30fps，页面不可见时自动停止。
 */
public class WeatherSceneView extends View {

    public static final int SCENE_SUNNY = 0;
    public static final int SCENE_CLOUDY = 1;
    public static final int SCENE_OVERCAST = 2;
    public static final int SCENE_RAIN = 3;
    public static final int SCENE_THUNDER = 4;
    public static final int SCENE_SNOW = 5;
    public static final int SCENE_FOG = 6;
    public static final int SCENE_NIGHT = 7;

    private static final long FRAME_MS = 33;

    private int scene = SCENE_NIGHT;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, FRAME_MS);
        }
    };

    private final Random rnd = new Random();
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private long startMs = System.currentTimeMillis();

    // 雨滴 [x, y, 长度]（归一化坐标）
    private float[][] rain;
    // 雪花 [x, y, 半径, 相位]
    private float[][] snow;
    // 云 [x, y, 缩放, 速度]
    private float[][] clouds;
    // 星星 [x, y, 半径, 相位]
    private float[][] stars;
    // 雾带 [y, 速度, 相位]
    private float[][] fogBands;
    // 闪电闪烁
    private long lastBolt = 0;

    public WeatherSceneView(Context context) {
        super(context);
        init();
    }

    public WeatherSceneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    /** 切换场景；页面可见时自动启动动画 */
    public void setScene(int s) {
        if (s < SCENE_SUNNY || s > SCENE_NIGHT) s = SCENE_NIGHT;
        if (s == scene && rain != null) return;
        scene = s;
        initParticles();
        startMs = System.currentTimeMillis();
        start();
    }

    public void start() {
        handler.removeCallbacks(ticker);
        handler.postDelayed(ticker, FRAME_MS);
    }

    public void stop() {
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    private void initParticles() {
        rain = null;
        snow = null;
        clouds = null;
        stars = null;
        fogBands = null;
        switch (scene) {
            case SCENE_RAIN:
            case SCENE_THUNDER: {
                rain = new float[70][3];
                for (float[] r : rain) {
                    r[0] = rnd.nextFloat();
                    r[1] = rnd.nextFloat() * 0.9f;
                    r[2] = 0.028f + rnd.nextFloat() * 0.03f;
                }
                clouds = new float[2][4];
                for (float[] c : clouds) {
                    c[0] = rnd.nextFloat() * 1.2f;
                    c[1] = 0.08f + rnd.nextFloat() * 0.12f;
                    c[2] = 0.8f + rnd.nextFloat() * 0.5f;
                    c[3] = 0.0004f + rnd.nextFloat() * 0.0005f;
                }
                lastBolt = 0;
                break;
            }
            case SCENE_SNOW: {
                snow = new float[46][4];
                for (float[] s : snow) {
                    s[0] = rnd.nextFloat();
                    s[1] = rnd.nextFloat() * 0.95f;
                    s[2] = 0.006f + rnd.nextFloat() * 0.008f;
                    s[3] = rnd.nextFloat() * 6.28f;
                }
                clouds = new float[2][4];
                for (float[] c : clouds) {
                    c[0] = rnd.nextFloat() * 1.2f;
                    c[1] = 0.05f + rnd.nextFloat() * 0.10f;
                    c[2] = 0.7f + rnd.nextFloat() * 0.4f;
                    c[3] = 0.0004f + rnd.nextFloat() * 0.0004f;
                }
                break;
            }
            case SCENE_CLOUDY:
            case SCENE_OVERCAST: {
                clouds = new float[3][4];
                for (float[] c : clouds) {
                    c[0] = rnd.nextFloat() * 1.2f;
                    c[1] = 0.10f + rnd.nextFloat() * 0.22f;
                    c[2] = 0.9f + rnd.nextFloat() * 0.6f;
                    c[3] = 0.0003f + rnd.nextFloat() * 0.0004f;
                }
                break;
            }
            case SCENE_FOG: {
                fogBands = new float[3][3];
                for (int i = 0; i < fogBands.length; i++) {
                    fogBands[i][0] = 0.12f + i * 0.13f + rnd.nextFloat() * 0.04f;
                    fogBands[i][1] = 0.0003f + rnd.nextFloat() * 0.0003f;
                    fogBands[i][2] = rnd.nextFloat() * 6.28f;
                }
                break;
            }
            case SCENE_NIGHT: {
                stars = new float[70][4];
                for (float[] s : stars) {
                    s[0] = rnd.nextFloat();
                    s[1] = rnd.nextFloat() * 0.62f;
                    s[2] = 0.0025f + rnd.nextFloat() * 0.004f;
                    s[3] = rnd.nextFloat() * 6.28f;
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;
        // 首次绘制（数据未返回时）也要保证粒子数组存在，避免空指针
        if (rain == null && snow == null && clouds == null
                && stars == null && fogBands == null) {
            initParticles();
        }
        float t = (System.currentTimeMillis() - startMs) / 1000f;
        float d = getResources().getDisplayMetrics().density;

        switch (scene) {
            case SCENE_SUNNY:
                drawSunny(canvas, w, h, t, d);
                break;
            case SCENE_CLOUDY:
                drawSunny(canvas, w, h, t, d);
                drawClouds(canvas, w, h, d, 0.75f);
                break;
            case SCENE_OVERCAST:
                drawClouds(canvas, w, h, d, 0.95f);
                break;
            case SCENE_RAIN:
                drawRain(canvas, w, h, d, false);
                break;
            case SCENE_THUNDER:
                drawRain(canvas, w, h, d, true);
                break;
            case SCENE_SNOW:
                drawSnow(canvas, w, h, t, d);
                break;
            case SCENE_FOG:
                drawFog(canvas, w, h, t);
                break;
            case SCENE_NIGHT:
                drawNight(canvas, w, h, t);
                break;
        }
    }

    // ---------------- 晴 / 多云 ----------------

    private void drawSunny(Canvas c, float w, float h, float t, float d) {
        float cx = w * 0.78f;
        float cy = h * 0.30f;
        float r = Math.min(w, h) * 0.10f;

        // 光晕
        for (int i = 3; i >= 1; i--) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(0x14FFF9C4);
            c.drawCircle(cx, cy, r * (1 + i * 0.55f), p);
        }
        // 旋转光线
        float rayR1 = r * 1.35f;
        float rayR2 = r * 1.75f;
        float base = t * 0.10f;
        for (int i = 0; i < 8; i++) {
            float a = base + i * 3.14159f / 4f;
            float x1 = cx + (float) Math.cos(a) * rayR1;
            float y1 = cy + (float) Math.sin(a) * rayR1;
            float x2 = cx + (float) Math.cos(a) * rayR2;
            float y2 = cy + (float) Math.sin(a) * rayR2;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2.2f * d);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(0x99FFFFFF);
            c.drawLine(x1, y1, x2, y2, p);
        }
        // 太阳
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFFE08A);
        c.drawCircle(cx, cy, r, p);
    }

    private void drawClouds(Canvas c, float w, float h, float d, float alpha) {
        if (clouds == null) return;
        p.setColor(ColorWithAlpha(0xFFFFFF, (int) (alpha * 255 * 0.55f)));
        for (float[] cl : clouds) {
            cl[0] += cl[3];
            if (cl[0] > 1.25f) cl[0] = -0.3f;
            float cx = cl[0] * w;
            float cy = cl[1] * h;
            float s = cl[2];
            drawCloudShape(c, cx, cy, s * 42f * d, p);
        }
    }

    /** 白色云朵形状：三个圆 + 底部圆角矩形 */
    private void drawCloudShape(Canvas c, float cx, float cy, float size, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        c.save();
        c.translate(cx, cy);
        float u = size / 20f;
        c.drawCircle(-4.5f * u, 0.5f * u, 2.6f * u, paint);
        c.drawCircle(0, -1.0f * u, 3.6f * u, paint);
        c.drawCircle(4.5f * u, 0.8f * u, 2.8f * u, paint);
        c.drawRoundRect(-6.5f * u, 0.5f * u, 6.5f * u, 3.8f * u, 1.6f * u, 1.6f * u, paint);
        c.restore();
    }

    // ---------------- 雨 / 雷 ----------------

    private void drawRain(Canvas c, float w, float h, float d, boolean thunder) {
        // 云层
        p.setColor(0xB3FFFFFF);
        for (float[] cl : clouds) {
            cl[0] += cl[3];
            if (cl[0] > 1.3f) cl[0] = -0.3f;
            drawCloudShape(c, cl[0] * w, cl[1] * h, cl[2] * 46f * d, p);
        }
        // 雨丝
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.6f * d);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(0x99E0F7FA);
        for (float[] r : rain) {
            r[1] += 0.030f;
            if (r[1] > 1.02f) {
                r[1] = -0.05f;
                r[0] = rnd.nextFloat();
            }
            float x = r[0] * w;
            float y = r[1] * h;
            c.drawLine(x, y, x - w * 0.025f, y + r[2] * h, p);
        }
        // 闪电
        if (thunder) {
            long now = System.currentTimeMillis();
            if (now - lastBolt > 2600) {
                lastBolt = now;
            }
            float flash = 1f - ((now - lastBolt) / 600f);
            if (flash > 0f && flash <= 1f) {
                float lw = w * 0.55f;
                float ly = h * 0.15f;
                path.reset();
                path.moveTo(lw, ly);
                path.lineTo(lw - w * 0.045f, ly + h * 0.09f);
                path.lineTo(lw + w * 0.012f, ly + h * 0.09f);
                path.lineTo(lw - w * 0.02f, ly + h * 0.17f);
                path.lineTo(lw + w * 0.05f, ly + h * 0.02f);
                path.lineTo(lw + w * 0.012f, ly + h * 0.02f);
                path.lineTo(lw + w * 0.028f, ly - h * 0.015f);
                path.close();
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xCCFFE08A);
                c.drawPath(path, p);
            }
        }
    }

    // ---------------- 雪 ----------------

    private void drawSnow(Canvas c, float w, float h, float t, float d) {
        p.setColor(0x99FFFFFF);
        for (float[] cl : clouds) {
            cl[0] += cl[3];
            if (cl[0] > 1.3f) cl[0] = -0.3f;
            drawCloudShape(c, cl[0] * w, cl[1] * h, cl[2] * 42f * d, p);
        }
        p.setStyle(Paint.Style.FILL);
        for (float[] s : snow) {
            s[1] += 0.004f;
            s[3] += 0.05f;
            if (s[1] > 1.05f) {
                s[1] = -0.03f;
                s[0] = rnd.nextFloat();
            }
            float x = s[0] * w + (float) Math.sin(s[3]) * w * 0.012f;
            float y = s[1] * h;
            float r = s[2] * Math.min(w, h) * 0.5f;
            p.setAlpha((int) (150 + 90 * (0.5 + 0.5 * Math.sin(s[3] * 1.7f))));
            c.drawCircle(x, y, Math.max(r, 1.5f * d), p);
        }
        p.setAlpha(255);
    }

    // ---------------- 雾 ----------------

    private void drawFog(Canvas c, float w, float h, float t) {
        if (fogBands == null) return;
        p.setStyle(Paint.Style.FILL);
        for (float[] band : fogBands) {
            float phase = band[2] + t * 0.4f;
            float cx = (float) Math.sin(phase) * w * 0.10f + w * 0.5f;
            float bw = w * (0.8f + 0.3f * (float) Math.sin(phase * 0.6f));
            float bh = h * 0.035f;
            p.setColor(0x1AFFFFFF);
            c.drawRoundRect(cx - bw / 2, band[0] * h, cx + bw / 2, band[0] * h + bh,
                    bh, bh, p);
        }
    }

    // ---------------- 夜 ----------------

    private void drawNight(Canvas c, float w, float h, float t) {
        p.setStyle(Paint.Style.FILL);
        for (float[] s : stars) {
            float tw = 0.35f + 0.65f * (0.5f + 0.5f * (float) Math.sin(s[3] + t * 2.2f));
            p.setColor(ColorWithAlpha(0xFFFFFF, (int) (tw * 230)));
            c.drawCircle(s[0] * w, s[1] * h, s[2] * Math.min(w, h), p);
        }
        // 月亮
        float mx = w * 0.72f;
        float my = h * 0.18f;
        float mr = Math.min(w, h) * 0.065f;
        p.setColor(0x22FFF3B0);
        c.drawCircle(mx, my, mr * 2.3f, p);
        p.setColor(0x55FFF3B0);
        c.drawCircle(mx, my, mr * 1.5f, p);
        p.setColor(0xFFFFE08A);
        c.drawCircle(mx, my, mr, p);
    }

    private static int ColorWithAlpha(int rgb, int alpha) {
        return (rgb & 0x00FFFFFF) | (alpha << 24);
    }
}
