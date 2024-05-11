package com.example.ageestimator;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResponseActivity extends AppCompatActivity{
    private TextView faceResponseTextView;
    private TextView voiceResponseTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        faceResponseTextView = findViewById(R.id.faceAgeEstimateTextView);
        voiceResponseTextView = findViewById(R.id.voiceAgeEstimateTextView);


        String faceResponseString = getIntent().getStringExtra("faceResponse");
        if (faceResponseString != null && !faceResponseString.isEmpty()) {
            faceResponseTextView.setText(faceResponseString);
        } else {
            faceResponseTextView.setText("No response received.");
        }

        String voiceResponseString = getIntent().getStringExtra("voiceResponse");
        if (voiceResponseString != null && !voiceResponseString.isEmpty()) {
            voiceResponseTextView.setText(voiceResponseString);
        } else {
            voiceResponseTextView.setText("No response received.");
        }
    }
}
