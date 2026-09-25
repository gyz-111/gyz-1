package com.qingkong.weather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 24小时温度曲线：平滑贝塞尔曲线 + 渐变填充 + 逐小时标签（温度/图标/降水/时间）。
 * 宽度按小时数自适应，可放入 HorizontalScrollView 横向滑动。
 */
public class HourlyCurveView extends View {

    private static final int COL_W_DP = 60;      // 每小时列宽
    private static final int TOP_PAD_DP = 44;    // 温度文字上方空间
    private static final int BOTTOM_PAD_DP = 66; // 下方文字区总高

    private List<WeatherApi.Hourly> data = new ArrayList<>();
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint tempPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint iconPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint popPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint timePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint timeNowPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Path curvePath = new Path();
    private final Path fillPath = new Path();

    private int accent = 0xFF67E8F9;

    public HourlyCurveView(Context context) {
        super(context);
        init();
    }

    public HourlyCurveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HourlyCurveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float d = getResources().getDisplayMetrics().density;

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2.5f * d);
        linePaint.setColor(accent);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        tempPaint.setTextAlign(Paint.Align.CENTER);
        tempPaint.setColor(0xFFFFFFFF);
        tempPaint.setTextSize(13 * d);
        tempPaint.setFakeBoldText(true);

        iconPaint.setTextAlign(Paint.Align.CENTER);
        iconPaint.setTextSize(20 * d);

        popPaint.setTextAlign(Paint.Align.CENTER);
        popPaint.setTextSize(10 * d);
        popPaint.setColor(0xFF81D4FA);

        timePaint.setTextAlign(Paint.Align.CENTER);
        timePaint.setColor(0xB3FFFFFF);
        timePaint.setTextSize(11 * d);

        timeNowPaint.setTextAlign(Paint.Align.CENTER);
        timeNowPaint.setColor(accent);
        timeNowPaint.setTextSize(11 * d);
        timeNowPaint.setFakeBoldText(true);

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(accent);

        dotRingPaint.setStyle(Paint.Style.STROKE);
        dotRingPaint.setStrokeWidth(2 * d);
        dotRingPaint.setColor(0x66FFFFFF);
    }

    public void setData(List<WeatherApi.Hourly> list) {
        this.data = list != null ? list : new ArrayList<WeatherApi.Hourly>();
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        float d = getResources().getDisplayMetrics().density;
        int w = (int) (data.size() * COL_W_DP * d + 12 * d);
        int h = MeasureSpec.getSize(heightSpec);
        setMeasuredDimension(Math.max(w, MeasureSpec.getSize(widthSpec)),
                Math.max(h, (int) (155 * d)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data.isEmpty()) return;
        try {
            float d = getResources().getDisplayMetrics().density;
            float colW = COL_W_DP * d;
            float topPad = TOP_PAD_DP * d;
            float bottomPad = BOTTOM_PAD_DP * d;
            int n = data.size();
            float chartH = getHeight() - topPad - bottomPad;
            if (chartH <= 0) return;

            // 温度范围
            float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
            for (WeatherApi.Hourly h : data) {
                try {
                    float t = Float.parseFloat(h.temp);
                    if (t < min) min = t;
                    if (t > max) max = t;
                } catch (Exception ignored) {}
            }
            if (min == Float.MAX_VALUE) return;
            if (max - min < 1f) max = min + 1f;

            float[] xs = new float[n];
            float[] ys = new float[n];
            for (int i = 0; i < n; i++) {
                xs[i] = 6 * d + colW * i + colW / 2f;
                try {
                    float t = Float.parseFloat(data.get(i).temp);
                    ys[i] = topPad + chartH * (1f - (t - min) / (max - min));
                } catch (Exception e) {
                    ys[i] = topPad + chartH / 2f;
                }
            }

            // 平滑曲线
            curvePath.reset();
            curvePath.moveTo(xs[0], ys[0]);
            for (int i = 0; i < n - 1; i++) {
                float mx = (xs[i] + xs[i + 1]) / 2f;
                curvePath.cubicTo(mx, ys[i], mx, ys[i + 1], xs[i + 1], ys[i + 1]);
            }

            // 渐变填充
            fillPath.set(curvePath);
            fillPath.lineTo(xs[n - 1], topPad + chartH);
            fillPath.lineTo(xs[0], topPad + chartH);
            fillPath.close();
            fillPaint.setShader(new LinearGradient(0, topPad, 0, topPad + chartH,
                    0x664FC3F7, 0x004FC3F7, Shader.TileMode.CLAMP));
            fillPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(fillPath, fillPaint);

            canvas.drawPath(curvePath, linePaint);

            String nowHour = null;
            try {
                nowHour = new SimpleDateFormat("HH", Locale.CHINA).format(new Date()) + "时";
            } catch (Exception ignored) {}

            float iconY = getHeight() - bottomPad + 16 * d;
            float popY = getHeight() - bottomPad + 38 * d;
            float timeY = getHeight() - 12 * d;

            for (int i = 0; i < n; i++) {
                WeatherApi.Hourly h = data.get(i);
                boolean isNow = i == 0 || (nowHour != null
                        && nowHour.equals(WeatherApi.formatHour(h.fxTime)));

                canvas.drawText(h.temp + "°", xs[i], ys[i] - 10 * d, tempPaint);

                canvas.drawText(WeatherApi.iconEmoji(h.icon, h.text),
                        xs[i], iconY, iconPaint);

                try {
                    int pop = Integer.parseInt(h.pop);
                    if (pop > 0) {
                        canvas.drawText(pop + "%", xs[i], popY, popPaint);
                    }
                } catch (Exception ignored) {}

                canvas.drawText(i == 0 ? "现在" : WeatherApi.formatHour(h.fxTime),
                        xs[i], timeY, isNow ? timeNowPaint : timePaint);

                if (isNow) {
                    canvas.drawCircle(xs[i], ys[i], 7 * d, dotRingPaint);
                    canvas.drawCircle(xs[i], ys[i], 4 * d, dotPaint);
                }
            }
        } catch (Exception e) {
            // 绘制异常静默，避免崩溃
        }
    }
}
