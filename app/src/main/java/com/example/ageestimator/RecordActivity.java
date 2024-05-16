package com.example.ageestimator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.Manifest;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import pl.droidsonroids.gif.GifImageView;
import okhttp3.MediaType;
import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.RequestBody;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;


public class RecordActivity extends AppCompatActivity {

    ImageButton backBtn, play, nextBtn;
    ToggleButton recBtn;
    Chronometer timeRec;
    GifImageView recGif;
    TextView textView;
    private MediaPlayer mediaPlayer = null;
    File recordedFile;
    private MediaRecorder recorder;

    public static class FaceRetrofitClient {
        private static Retrofit retrofit = null;

        public static FaceAgeEstimatorService getService() {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl("https://api-inference.huggingface.co")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }

            return retrofit.create(FaceAgeEstimatorService.class);
        }
    }

    public static class VoiceRetrofitClient {
        private static Retrofit retrofit = null;

        public static VoiceAgeEstimatorService getService() {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl("https://api-inference.huggingface.co")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }

            return retrofit.create(VoiceAgeEstimatorService.class);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        initViews();

        backBtn.setOnClickListener(v -> finish());
        nextBtn.setOnClickListener(v -> goToResponseActivity());
        play.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()){
                pause();
                play.setImageResource(R.drawable.play);

            } else {
                play();
                play.setImageResource(R.drawable.pause);
            }
        });

        recBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                try {
                    startRecording();
                    recGif.setVisibility(View.VISIBLE);
                    play.setVisibility(View.GONE);
                    nextBtn.setVisibility(View.GONE);
                    timeRec.setBase(SystemClock.elapsedRealtime());
                    timeRec.start();
                    textView.setText("Recording...");
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this,"couldn't record", Toast.LENGTH_SHORT).show();
                    buttonView.setChecked(false);
                }
            } else {
                stopRecording();
                recGif.setVisibility(View.GONE);
                timeRec.setBase(SystemClock.elapsedRealtime());
                timeRec.stop();
                textView.setText("Recorded Successfully");
            }
        });

        askRuntimePermission();

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = format.format(new Date());

        String file_name = "/recording " + date + ".3gp";
        recordedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file_name);
    }

    private void startRecording(){
        if (recordedFile.exists()) {
            recordedFile.delete();
        }
        if (mediaPlayer != null) {
            mediaPlayer = null;
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordedFile.getAbsolutePath());

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Stop recording after 5 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (recorder != null) {
                    recBtn.setChecked(false);
                }
            }
        }, 5000);
    }

    private void stopRecording(){
        recorder.stop();
        recorder.release();
        recorder = null;
        nextBtn.setVisibility(View.VISIBLE);
        play.setVisibility(View.VISIBLE);
    }

   private void play(){

       try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(String.valueOf(recordedFile));
                mediaPlayer.prepare();
            }
            mediaPlayer.start();
           mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
               @Override
               public void onCompletion(MediaPlayer mp) {
                   play.setImageResource(R.drawable.play);
               }
           });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private byte[] getVoiceData() {
        byte[] voiceData = null;
        try {
            FileInputStream fis = new FileInputStream(recordedFile);
            voiceData = new byte[(int) recordedFile.length()];
            fis.read(voiceData); // read file into bytes[]
            fis.close();
        } catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"Record a voice", Toast.LENGTH_SHORT).show();
        }
        return voiceData;
    }

    private void goToResponseActivity() {
        byte[] imageData = getIntent().getByteArrayExtra("imageData");
        byte[] voiceData = getVoiceData();
        if (imageData != null && voiceData != null) {
            RequestBody faceRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageData);
            RequestBody voiceRequestBody = RequestBody.create(MediaType.parse("audio/3gpp"), voiceData);

            final CountDownLatch latch = new CountDownLatch(2);
            final String[] faceResponse = new String[1];
            final String[] voiceResponse = new String[1];

            // Create ProgressDialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("In progress...");
            progressDialog.show();
//            // Create ProgressBar
//            ProgressBar progressBar = findViewById(R.id.progressBar);
//            progressBar.setVisibility(View.VISIBLE);

            makeFaceRequest(FaceRetrofitClient.getService(), faceRequestBody, 0, faceResponse, latch);
            makeVoiceRequest(VoiceRetrofitClient.getService(), voiceRequestBody, 0, voiceResponse, latch);

            new Thread(() -> {
                try {
                    latch.await();
                    runOnUiThread(() -> {
                        Intent intent = new Intent(RecordActivity.this, ResponseActivity.class);
                        intent.putExtra("faceResponse", faceResponse[0]);
                        intent.putExtra("voiceResponse", voiceResponse[0]);
                        recordedFile.delete();
                        progressDialog.cancel();
//                        progressBar.setVisibility(View.GONE);
                        play.setVisibility(View.GONE);
                        startActivity(intent);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void makeFaceRequest(FaceAgeEstimatorService service, RequestBody requestBody,
                                 int retryCount, String[] my_response, CountDownLatch latch) {
        Call<List<FaceAgeEstimate>> call = service.estimateFaceAge(requestBody,
                                "Bearer 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");
        call.enqueue(new Callback<List<FaceAgeEstimate>>() {
            @Override
            public void onResponse(Call<List<FaceAgeEstimate>> call, Response<List<FaceAgeEstimate>> response) {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    my_response[0] = gson.toJson(response.body());
                    latch.countDown();
                } else if (response.code() == 503 && retryCount < 10) { // Retry up to 10 times
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            makeFaceRequest(service, requestBody, retryCount + 1, my_response, latch);
                        }
                    }, 2000); // Delay of 2 seconds
                } else {
                    // Handle error response
                    Toast.makeText(RecordActivity.this, "Error: " + response.errorBody(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FaceAgeEstimate>> call, Throwable t) {
                // Handle failure
                Toast.makeText(RecordActivity.this, "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeVoiceRequest(VoiceAgeEstimatorService service, RequestBody requestBody,
                                  int retryCount, String[] my_response, CountDownLatch latch) {
        Call<List<VoiceAgeEstimate>> call = service.estimateVoiceAge(requestBody,
                                    "Bearer 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");
        call.enqueue(new Callback<List<VoiceAgeEstimate>>() {
            @Override
            public void onResponse(Call<List<VoiceAgeEstimate>> call, Response<List<VoiceAgeEstimate>> response) {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    my_response[0] = gson.toJson(response.body());
                    latch.countDown();
                } else if (response.code() == 503 && retryCount < 10) { // Retry up to 10 times
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            makeVoiceRequest(service, requestBody, retryCount + 1, my_response, latch);
                        }
                    }, 2000); // Delay of 2 seconds
                } else {
                    // Handle error response
                    Toast.makeText(RecordActivity.this, "Error: " + response.errorBody(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VoiceAgeEstimate>> call, Throwable t) {
                // Handle failure
                Toast.makeText(RecordActivity.this, "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void askRuntimePermission() {
        // Request for mic runtime permission
        if (ContextCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(RecordActivity.this, new String[]{
                    Manifest.permission.RECORD_AUDIO
            }, 100);
        }

        if (ContextCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(RecordActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        }
    }
    private void initViews() {
        textView = findViewById(R.id.textView);
        backBtn = findViewById(R.id.backBtn);
        nextBtn = findViewById(R.id.nextBtn);
        play = findViewById(R.id.play);
        recBtn = findViewById(R.id.recBtn);
        timeRec = findViewById(R.id.timeRec);
        recGif = findViewById(R.id.recGif);
    }
}