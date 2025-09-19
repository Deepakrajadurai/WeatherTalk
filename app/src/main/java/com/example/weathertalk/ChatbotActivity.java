package com.example.weathertalk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

        input = findViewById(R.id.editChat);
        chat = findViewById(R.id.txtChat);
        send = findViewById(R.id.btnSend);
        back = findViewById(R.id.btnBack);
        scrollView = findViewById(R.id.scrollChat);

        // Send button
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMsg = input.getText().toString().trim();
                if (userMsg.isEmpty()) return;

                chat.append("You: " + userMsg + "\n");

                // Simple responses
                if (userMsg.toLowerCase().contains("weather")) {
                    chat.append("Bot: Try checking the main screen for weather!\n");
                } else if (userMsg.toLowerCase().contains("hello")) {
                    chat.append("Bot: Hi there! Ask me about the weather.\n");
                } else {
                    chat.append("Bot: I'm just a simple bot. Ask me about the weather!\n");
                }

                // Auto-scroll to bottom
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });

                input.setText("");
            }
        });

        // Back button
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // closes ChatbotActivity and goes back to MainActivity
            }
        });
    }
}
