package com.example.weathertalk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class CustomWeatherView extends View {
    private float temperature;

    public CustomWeatherView(Context context, float temp) {
        super(context);
        this.temperature = temp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw thermometer outline
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(8);
        canvas.drawRoundRect(width/4, 50, width*3/4, height-50, 30, 30, paint);

        // Fill thermometer based on temperature
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);

        float fillHeight = (temperature / 50f) * (height - 100); // scale to 0–50 °C
        canvas.drawRoundRect(width/4, height-50-fillHeight, width*3/4, height-50, 30, 30, paint);
    }

}
