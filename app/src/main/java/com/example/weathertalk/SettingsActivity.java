package com.example.weathertalk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar seekThreshold;
    private TextView txtValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekThreshold = findViewById(R.id.seekThreshold);
        txtValue = findViewById(R.id.txtValue);

        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        int currentThreshold = prefs.getInt("shake_threshold", 5); // default 5

        seekThreshold.setProgress(currentThreshold);
        txtValue.setText("Shake Threshold: " + currentThreshold);

        seekThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtValue.setText("Shake Threshold: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int newThreshold = seekBar.getProgress();
                prefs.edit().putInt("shake_threshold", newThreshold).apply();
                Toast.makeText(SettingsActivity.this,
                        "Saved threshold: " + newThreshold,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
