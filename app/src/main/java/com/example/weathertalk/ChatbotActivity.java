package com.example.weathertalk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class ChatbotActivity extends AppCompatActivity {
    EditText input;
    TextView chat;
    Button send, back;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Allow network on main thread (for simplicity in this project)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        input = findViewById(R.id.editChat);
        chat = findViewById(R.id.txtChat);
        send = findViewById(R.id.btnSend);
        back = findViewById(R.id.btnBack);
        scrollView = findViewById(R.id.scrollChat);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMsg = input.getText().toString().trim();
                if (userMsg.isEmpty()) return;

                chat.append("You: " + userMsg + "\n");
                String botReply = getBotReply(userMsg);
                chat.append("Bot: " + botReply + "\n\n");

                input.setText("");
                scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
            }
        });

        back.setOnClickListener(v -> finish());
    }

    // 🔧 Smarter chatbot replies
    private String getBotReply(String msg) {
        msg = msg.toLowerCase();

        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) {
            return "Hello! I’m WeatherBot. Ask me about the weather!";
        } else if (msg.contains("bye")) {
            return "Goodbye! Stay safe and check the weather often!";
        } else if (msg.contains("thank")) {
            return "You’re welcome! Happy to help.";
        } else if (msg.contains("your name")) {
            return "I’m WeatherBot, your weather assistant.";
        } else if (msg.contains("how are you")) {
            return "I’m great! Always ready to check the weather.";
        } else if (msg.contains("weather")) {
            if (!isInternetAvailable()) {
                return "No internet connection right now.";
            }

            // Extract city name
            String city = extractCity(msg);
            if (city != null) {
                try {
                    double[] coords = WeatherApiClient.getCoordsFromCity(city);
                    if (coords != null) {
                        String weather = WeatherApiClient.getWeather(coords[0], coords[1]);
                        if (weather != null && !weather.isEmpty()) {
                            return "Here’s the weather in " + city + ":\n" + weather;
                        } else {
                            return "Oops, something went wrong while fetching weather.";
                        }
                    } else {
                        return "Sorry, I couldn’t find that city.";
                    }
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            } else {
                return "Please ask like: 'weather in London'";
            }
        } else {
            return "I’m still learning! Try asking me about the weather, or just say hello.";
        }
    }

    // Helper: extract city from message
    private String extractCity(String msg) {
        if (msg.contains("weather in")) {
            return msg.substring(msg.indexOf("weather in") + 10).trim();
        }
        return null;
    }

    // Utility: check internet connectivity
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }
}
