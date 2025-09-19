package com.example.weathertalk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomWeatherView extends View {

    private float temperature = 0f; // default
    private Paint paint;

    public CustomWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    // Allow activity to set temperature
    public void setTemperature(float temp) {
        this.temperature = temp;
        invalidate(); // redraw when new value set
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // --- Draw outline of thermometer ---
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setColor(Color.BLACK);

        // Thermometer tube
        float tubeLeft = width / 3f;
        float tubeRight = 2 * width / 3f;
        float tubeTop = 50;
        float tubeBottom = height - 150;
        canvas.drawRoundRect(tubeLeft, tubeTop, tubeRight, tubeBottom, 30, 30, paint);

        // Bulb outline
        canvas.drawCircle(width / 2f, height - 80, 60, paint);

        // --- Fill level based on temperature ---
        float maxTemp = 50f; // scale max
        float clampedTemp = Math.max(0, Math.min(maxTemp, temperature));
        float fillHeight = (clampedTemp / maxTemp) * (tubeBottom - tubeTop);

        // Color: cold=blue, medium=orange, hot=red
        if (clampedTemp < 15) {
            paint.setColor(Color.BLUE);
        } else if (clampedTemp < 30) {
            paint.setColor(Color.rgb(255, 140, 0)); // orange
        } else {
            paint.setColor(Color.RED);
        }
        paint.setStyle(Paint.Style.FILL);

        // Fill tube
        canvas.drawRoundRect(tubeLeft, tubeBottom - fillHeight, tubeRight, tubeBottom, 30, 30, paint);
        // Fill bulb
        canvas.drawCircle(width / 2f, height - 80, 55, paint);

        // --- Draw temperature text ---
        paint.setColor(Color.BLACK);
        paint.setTextSize(48f);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(temperature + " °C", width / 2f - 70, tubeTop - 20, paint);
    }
}
