package com.example.weathertalk;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WeatherWorker extends Worker {

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationHelper helper = new NotificationHelper(getApplicationContext());
        helper.sendNotification(getApplicationContext(),
                "Weather Alert 🌦️",
                "This is a test notification (every run).");
        return Result.success();
    }
}
