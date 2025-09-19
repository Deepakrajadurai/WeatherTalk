package com.example.weathertalk;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class CustomWeatherView extends View {

    private float temperature = 0f;
    private Paint outlinePaint;
    private Paint fillPaint;
    private Paint glowPaint;

    private int currentColor = Color.BLUE; // default
    private float pulseScale = 1.0f;
    private ValueAnimator pulseAnimator;

    public CustomWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);

        outlinePaint = new Paint();
        outlinePaint.setAntiAlias(true);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(8);
        outlinePaint.setColor(Color.BLACK);

        fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.FILL);

        glowPaint = new Paint();
        glowPaint.setAntiAlias(true);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL));
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    // Update temperature + handle pulse
    public void setTemperature(float newTemp) {
        this.temperature = newTemp;
        invalidate();

        if (newTemp > 30) {
            startPulseAnimation();
        } else {
            stopPulseAnimation();
        }
    }

    private void startPulseAnimation() {
        if (pulseAnimator != null && pulseAnimator.isRunning()) return;

        pulseAnimator = ValueAnimator.ofFloat(1f, 1.2f);
        pulseAnimator.setDuration(600);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new LinearInterpolator());

        pulseAnimator.addUpdateListener(animation -> {
            pulseScale = (float) animation.getAnimatedValue();
            invalidate();
        });

        pulseAnimator.start();
    }

    private void stopPulseAnimation() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        pulseScale = 1.0f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float tubeLeft = width / 3f;
        float tubeRight = 2 * width / 3f;
        float tubeTop = 100;
        float tubeBottom = height - 200;
        float cornerRadius = 40f;

        // Thermometer outline
        RectF tubeRect = new RectF(tubeLeft, tubeTop, tubeRight, tubeBottom);
        canvas.drawRoundRect(tubeRect, cornerRadius, cornerRadius, outlinePaint);

        // Map temperature to fill height
        float maxTemp = 50f;
        float clampedTemp = Math.max(0, Math.min(maxTemp, temperature));
        float fillHeight = (clampedTemp / maxTemp) * (tubeBottom - tubeTop);
        float fillTop = tubeBottom - fillHeight;

        // Color by range (and sync bulb + glow)
        if (clampedTemp < 15) {
            currentColor = Color.BLUE;
        } else if (clampedTemp < 30) {
            currentColor = Color.rgb(255, 140, 0); // orange
        } else {
            currentColor = Color.RED;
        }

        fillPaint.setColor(currentColor);
        glowPaint.setColor(currentColor);

        // ✅ Clip fill to the rounded rect path
        canvas.save();
        Path clipPath = new Path();
        clipPath.addRoundRect(tubeRect, cornerRadius, cornerRadius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        canvas.drawRect(tubeLeft, fillTop, tubeRight, tubeBottom, fillPaint);
        canvas.restore();

        // Bulb at bottom (same color as tube)
        float bulbRadius = 60;
        float bulbCenterY = height - 100;
        canvas.drawCircle(width / 2f, bulbCenterY, bulbRadius, fillPaint);

        // Glow around bulb (same color, scaled if pulsing)
        float glowRadius = 80 * pulseScale;
        canvas.drawCircle(width / 2f, bulbCenterY, glowRadius, glowPaint);

        // Label text
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(48f);
        canvas.drawText(String.format("%.1f °C", temperature), width / 2f - 70, tubeTop - 30, textPaint);
    }
}
