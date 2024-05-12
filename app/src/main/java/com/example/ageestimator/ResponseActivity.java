package com.example.ageestimator;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;


public class ResponseActivity extends AppCompatActivity{
    private TextView faceResponseTextView;
    private TextView voiceResponseTextView;
    private TextView FusionResponseTextView;

    private int calc_face_avg(String x) {
        int faceAvg = 0;
        if (x.equals("0-2")) {
            faceAvg = 1;
        } else if (x.equals("3-9")) {
            faceAvg = 6;
        } else if (x.equals("10-19")) {
            faceAvg = 15;
        } else if (x.equals("20-29")) {
            faceAvg = 25;
        } else if (x.equals("30-39")) {
            faceAvg = 35;
        } else if (x.equals("40-49")) {
            faceAvg = 45;
        } else if (x.equals("50-59")) {
            faceAvg = 55;
        } else if (x.equals("60-69")) {
            faceAvg = 65;
        } else {
            faceAvg = 75;
        }
        return faceAvg;
    }

    private int calc_voice_avg(String x) {
        int voiceAvg = 0;
        if (x.equals("[0,20)")) {
            voiceAvg = 10;
        } else if (x.equals("[20,40)")) {
            voiceAvg = 30;
        } else if (x.equals("[40,60)")) {
            voiceAvg = 50;
        } else if (x.equals("[60,80)")) {
            voiceAvg=70;
        }
        return voiceAvg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        faceResponseTextView = findViewById(R.id.faceAgeEstimateTextView);
        voiceResponseTextView = findViewById(R.id.voiceAgeEstimateTextView);
        FusionResponseTextView = findViewById(R.id.FusionAgeEstimateTextView);

        Gson gson = new Gson();

        String faceResponseString = getIntent().getStringExtra("faceResponse");
        Type faceType = new TypeToken<List<FaceAgeEstimate>>(){}.getType();
        List<FaceAgeEstimate> faceResponse = gson.fromJson(faceResponseString, faceType);
        if (faceResponse != null && !faceResponse.isEmpty()) {
            faceResponseTextView.setText(gson.toJson(faceResponse));
        } else {
            faceResponseTextView.setText("No response received.");
        }

        String voiceResponseString = getIntent().getStringExtra("voiceResponse");
        Type voiceType = new TypeToken<List<VoiceAgeEstimate>>(){}.getType();
        List<FaceAgeEstimate> voiceResponse = gson.fromJson(voiceResponseString, faceType);
        if (voiceResponse != null && !voiceResponse.isEmpty()) {
            voiceResponseTextView.setText(voiceResponse.get(0).label);
        } else {
            voiceResponseTextView.setText("No response received.");
        }

        // models fusion
        String f1_label = faceResponse.get(0).label.replace(" ", "").toLowerCase();
        double f1_score = faceResponse.get(0).score;
        String f2_label = faceResponse.get(1).label.replace(" ", "").toLowerCase();
        double f2_score = faceResponse.get(1).score;
        String v1_label = voiceResponse.get(0).label.replace(" ", "").toLowerCase();

        int faceAvg = (int) (calc_face_avg(f1_label) * f1_score + calc_face_avg(f2_label) * f2_score);
        int voiceAvg = calc_voice_avg(v1_label);

        int estimated_age = (4*faceAvg + voiceAvg)/5;

        FusionResponseTextView.setText(String.valueOf(estimated_age));




    }
}
