package com.caiyun.weather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 彩云天气风格的24小时温度曲线：平滑贝塞尔曲线 + 渐变填充 + 逐时刻标签
 * 标签分行绘制：温度(曲线上方) / 天气现象 / 降水概率 / 时间，互不重叠
 */
public class HourlyCurveView extends View {

    private static final int ITEM_W_DP = 58;   // 每小时列宽
    private static final int TOP_PAD_DP = 40;  // 温度文字上方空间
    private static final int BOTTOM_PAD_DP = 62; // 下方文字区总高

    private List<WeatherApiClient.Hourly> data = new ArrayList<>();
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint condPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint popPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timeNowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path curvePath = new Path();
    private final Path fillPath = new Path();

    private int accent;

    public HourlyCurveView(Context context) {
        super(context);
        init(null);
    }

    public HourlyCurveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public HourlyCurveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        float d = getResources().getDisplayMetrics().density;
        accent = android.graphics.Color.parseColor("#4FC3F7");

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2.5f * d);
        linePaint.setColor(accent);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        tempPaint.setTextAlign(Paint.Align.CENTER);
        tempPaint.setColor(0xFFFFFFFF);
        tempPaint.setTextSize(12 * d);

        condPaint.setTextAlign(Paint.Align.CENTER);
        condPaint.setColor(0xFFFFFFFF);
        condPaint.setTextSize(11 * d);

        popPaint.setTextAlign(Paint.Align.CENTER);
        popPaint.setTextSize(10 * d);
        popPaint.setColor(android.graphics.Color.parseColor("#81D4FA"));

        timePaint.setTextAlign(Paint.Align.CENTER);
        timePaint.setColor(0xB3FFFFFF);
        timePaint.setTextSize(11 * d);

        timeNowPaint.setTextAlign(Paint.Align.CENTER);
        timeNowPaint.setColor(accent);
        timeNowPaint.setTextSize(11 * d);
        timeNowPaint.setFakeBoldText(true);

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(0xFFFFFFFF);
    }

    public void setData(List<WeatherApiClient.Hourly> list) {
        this.data = list != null ? list : new ArrayList<WeatherApiClient.Hourly>();
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        float d = getResources().getDisplayMetrics().density;
        int w = (int) (data.size() * ITEM_W_DP * d + 16 * d);
        int h = MeasureSpec.getSize(heightSpec);
        setMeasuredDimension(Math.max(w, MeasureSpec.getSize(widthSpec)),
                Math.max(h, (int) (150 * d)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.isEmpty()) return;
        try {
            float d = getResources().getDisplayMetrics().density;
            float colW = ITEM_W_DP * d;
            float topPad = TOP_PAD_DP * d;
            float bottomPad = BOTTOM_PAD_DP * d;
            int n = data.size();
            float chartH = getHeight() - topPad - bottomPad;

            // 计算温度范围
            float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
            for (WeatherApiClient.Hourly h : data) {
                try {
                    float t = Float.parseFloat(h.temp);
                    if (t < min) min = t;
                    if (t > max) max = t;
                } catch (Exception ignored) {
                }
            }
            if (min == Float.MAX_VALUE) return;
            if (max - min < 1f) max = min + 1f;

            // 各点坐标
            float[] xs = new float[n];
            float[] ys = new float[n];
            for (int i = 0; i < n; i++) {
                xs[i] = 8 * d + colW * i + colW / 2f;
                try {
                    float t = Float.parseFloat(data.get(i).temp);
                    ys[i] = topPad + chartH * (1f - (t - min) / (max - min));
                } catch (Exception e) {
                    ys[i] = topPad + chartH / 2f;
                }
            }

            // 平滑曲线（三次贝塞尔）
            curvePath.reset();
            curvePath.moveTo(xs[0], ys[0]);
            for (int i = 0; i < n - 1; i++) {
                float mx = (xs[i] + xs[i + 1]) / 2f;
                curvePath.cubicTo(mx, ys[i], mx, ys[i + 1], xs[i + 1], ys[i + 1]);
            }

            // 曲线下方渐变填充
            fillPath.set(curvePath);
            fillPath.lineTo(xs[n - 1], topPad + chartH);
            fillPath.lineTo(xs[0], topPad + chartH);
            fillPath.close();
            Shader shader = new LinearGradient(0, topPad, 0, topPad + chartH,
                    0x664FC3F7, 0x004FC3F7, Shader.TileMode.CLAMP);
            fillPaint.setShader(shader);
            fillPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(fillPath, fillPaint);

            canvas.drawPath(curvePath, linePaint);

            String nowHour = null;
            try {
                java.text.SimpleDateFormat f =
                        new java.text.SimpleDateFormat("HH", java.util.Locale.CHINA);
                nowHour = f.format(new java.util.Date()) + "时";
            } catch (Exception ignored) {
            }

            // 三行文字：天气现象 / 降水概率 / 时间（固定行高，绝不重叠）
            float condY = getHeight() - bottomPad + 14 * d;
            float popY = getHeight() - bottomPad + 30 * d;
            float timeY = getHeight() - 10 * d;

            for (int i = 0; i < n; i++) {
                WeatherApiClient.Hourly h = data.get(i);
                boolean isNow = i == 0 || (nowHour != null
                        && nowHour.equals(WeatherApiClient.formatHour(h.time)));

                // 温度（曲线上方）
                canvas.drawText(h.temp + "°", xs[i], ys[i] - 12 * d, tempPaint);

                // 天气现象
                canvas.drawText(h.text, xs[i], condY, condPaint);

                // 降水概率
                try {
                    int pop = Integer.parseInt(h.pop);
                    if (pop > 0) {
                        canvas.drawText(pop + "%", xs[i], popY, popPaint);
                    }
                } catch (Exception ignored) {
                }

                // 时间
                canvas.drawText(i == 0 ? "现在" : WeatherApiClient.formatHour(h.time),
                        xs[i], timeY, isNow && i == 0 ? timeNowPaint : timePaint);

                // 当前时刻高亮点
                if (i == 0) {
                    canvas.drawCircle(xs[0], ys[0], 4 * d, dotPaint);
                }
            }
        } catch (Exception e) {
            // 绘制异常时静默失败，避免崩溃
        }
    }
}
