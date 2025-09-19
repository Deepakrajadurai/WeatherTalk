package com.example.weathertalk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Spinner spinnerSnooze = findViewById(R.id.spinnerSnooze);

        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int savedIndex = prefs.getInt("snooze_index", 1); // default 1 hr
        spinnerSnooze.setSelection(savedIndex);

        spinnerSnooze.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("snooze_index", position);
                switch (position) {
                    case 0:
                        editor.putLong("snooze_duration", 30 * 60 * 1000); // 30 min
                        break;
                    case 1:
                        editor.putLong("snooze_duration", 60 * 60 * 1000); // 1 hr
                        break;
                    case 2:
                        editor.putLong("snooze_duration", 3 * 60 * 60 * 1000); // 3 hr
                        break;
                }
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
