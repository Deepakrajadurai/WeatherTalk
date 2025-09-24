package com.example.weathertalk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Spinner spinnerSnooze = findViewById(R.id.spinnerSnooze);
        SeekBar seekThreshold = findViewById(R.id.seekThreshold);
        TextView txtThreshold = findViewById(R.id.txtThreshold);

        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // --- Restore saved settings ---
        int savedIndex = prefs.getInt("snooze_index", 1);
        spinnerSnooze.setSelection(savedIndex);

        int savedThreshold = prefs.getInt("shake_threshold", 5);
        seekThreshold.setProgress(savedThreshold);
        txtThreshold.setText("Shake Threshold: " + savedThreshold);

        // --- Save snooze selection ---
        spinnerSnooze.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("snooze_index", position);
                switch (position) {
                    case 0:
                        editor.putLong("snooze_duration", 30 * 60 * 1000);
                        break;
                    case 1:
                        editor.putLong("snooze_duration", 60 * 60 * 1000);
                        break;
                    case 2:
                        editor.putLong("snooze_duration", 3 * 60 * 60 * 1000);
                        break;
                }
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
//
        // --- Save threshold changes ---
        seekThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                txtThreshold.setText("Shake Threshold: " + value);
                editor.putInt("shake_threshold", value);
                editor.apply();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
