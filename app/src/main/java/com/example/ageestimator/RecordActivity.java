package com.example.ageestimator;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;
import android.widget.ToggleButton;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import android.Manifest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

public class RecordActivity extends AppCompatActivity {

    ImageButton backBtn, play;
    ToggleButton recBtn;
    Chronometer timeRec;
    GifImageView recGif;

    File recordedFile;
    private MediaRecorder recorder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        play = findViewById(R.id.play);
        play.setOnClickListener(v -> play());

        recBtn = findViewById(R.id.recBtn);
        timeRec = findViewById(R.id.timeRec);
        recGif = findViewById(R.id.recGif);

        askRuntimePermission();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = format.format(new Date());

        String file_name = "/recording " + date + ".3gp";
        recordedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file_name);


        recBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                try {
                    startRecording();
                    recGif.setVisibility(View.VISIBLE);
                    play.setVisibility(View.VISIBLE);
                    timeRec.setBase(SystemClock.elapsedRealtime());
                    timeRec.start();
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this,"couldn't record", Toast.LENGTH_SHORT).show();
                }
            } else {
                stopRecording();
                recGif.setVisibility(View.GONE);
                timeRec.setBase(SystemClock.elapsedRealtime());
                timeRec.stop();
            }
        });
    }

    private void startRecording(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recorder.setOutputFile(recordedFile);
        }
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
    }

   private void play(){
       MediaPlayer mediaPlayer = new MediaPlayer();

       try {
           mediaPlayer.setDataSource(String.valueOf(recordedFile));
           mediaPlayer.prepare();
           mediaPlayer.start();
           recordedFile.delete();
       }
       catch (IOException e){
           e.printStackTrace();
       }
   }

    private void askRuntimePermission(){
        Dexter.withContext(getBaseContext()).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

//                Toast.makeText(getBaseContext(), "Granted!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                permissionToken.continuePermissionRequest();
            }
        }).check();
    }
}