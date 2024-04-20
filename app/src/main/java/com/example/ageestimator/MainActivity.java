package com.example.ageestimator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = findViewById(R.id.startBtn);

        startBtn.setOnClickListener(v -> goToCameraActivity());
    }


    private void goToCameraActivity(){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

}