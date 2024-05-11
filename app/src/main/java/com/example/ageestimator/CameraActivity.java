package com.example.ageestimator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;

import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;

import com.google.gson.Gson;

import okhttp3.MediaType;
import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.RequestBody;

public class CameraActivity extends AppCompatActivity {

    ImageView imageView;
    ImageButton capBtn, nextBtn;

    public static class RetrofitClient {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imageView);
        capBtn = findViewById(R.id.capBtn);
        nextBtn = findViewById(R.id.nextBtn);

        nextBtn.setOnClickListener(v -> goToRecordActivity());

        // Request for camera runtime permission
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }

        capBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 100);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 570, 730, false);
            imageView.setImageBitmap(resizedBitmap);
        }
    }
//
//    private void goToRecordActivity(){
//        FaceAgeEstimatorService service = RetrofitClient.getService();
//        Drawable drawable = imageView.getDrawable();
//        if (drawable == null || !(drawable instanceof BitmapDrawable)) {
//            // No image has been set in the ImageView, or the image is not a BitmapDrawable
//            Toast.makeText(CameraActivity.this, "No image available for upload", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//        byte[] bitmapdata = bos.toByteArray();
//        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), bitmapdata);
//        Call<List<FaceAgeEstimate>> call = service.estimateFaceAge(requestBody, "Bearer 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");
//        call.enqueue(new Callback<List<FaceAgeEstimate>>() {
//            @Override
//            public void onResponse(Call<List<FaceAgeEstimate>> call, Response<List<FaceAgeEstimate>> response) {
//                if (response.isSuccessful()) {
////                    Gson gson = new Gson();
////                    String responseString = gson.toJson(response.body());
////                    Intent intent = new Intent(CameraActivity.this, ResponseActivity.class);
////                    intent.putExtra("response", responseString);
////                    startActivity(intent);
//                    List<FaceAgeEstimate> estimates = response.body();
//                    FaceAgeEstimate highestScoreEstimate = null;
//                    for (FaceAgeEstimate estimate : estimates) {
//                        if (highestScoreEstimate == null || estimate.score > highestScoreEstimate.score) {
//                            highestScoreEstimate = estimate;
//                        }
//                    }
//                    if (highestScoreEstimate != null) {
//                        Intent intent = new Intent(CameraActivity.this, RecordActivity.class);
//                        intent.putExtra("response", highestScoreEstimate.label);
//                        startActivity(intent);
//                    } else {
//                        Toast.makeText(CameraActivity.this, "No estimates received", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    // TODO: handle the error
//                    Toast.makeText(CameraActivity.this, "Error: " + response.errorBody(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<FaceAgeEstimate>> call, Throwable t) {
//                // TODO: handle the failure
//                Toast.makeText(CameraActivity.this, "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void goToRecordActivity(){
        FaceAgeEstimatorService service = RetrofitClient.getService();
        Drawable drawable = imageView.getDrawable();
        if (drawable == null || !(drawable instanceof BitmapDrawable)) {
            Toast.makeText(CameraActivity.this, "No image available for upload", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), bitmapdata);
        makeRequest(service, requestBody, 0);
    }

    private void makeRequest(FaceAgeEstimatorService service, RequestBody requestBody, int retryCount) {
        Call<List<FaceAgeEstimate>> call = service.estimateFaceAge(requestBody, "Bearer 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");
        call.enqueue(new Callback<List<FaceAgeEstimate>>() {
            @Override
            public void onResponse(Call<List<FaceAgeEstimate>> call, Response<List<FaceAgeEstimate>> response) {
                if (response.isSuccessful()) {
                    List<FaceAgeEstimate> estimates = response.body();
                    FaceAgeEstimate highestScoreEstimate = null;
                    for (FaceAgeEstimate estimate : estimates) {
                        if (highestScoreEstimate == null || estimate.score > highestScoreEstimate.score) {
                            highestScoreEstimate = estimate;
                        }
                    }
                    if (highestScoreEstimate != null) {
                        Intent intent = new Intent(CameraActivity.this, RecordActivity.class);
                        intent.putExtra("response", highestScoreEstimate.label);
                        startActivity(intent);
                    } else {
                        Toast.makeText(CameraActivity.this, "No estimates received", Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() == 503 && retryCount < 10) { // Retry up to 10 times
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            makeRequest(service, requestBody, retryCount + 1);
                        }
                    }, 2000); // Delay of 2 seconds
                } else {
                    // Handle error response
                    Toast.makeText(CameraActivity.this, "Error: " + response.errorBody(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FaceAgeEstimate>> call, Throwable t) {
                // Handle failure
                Toast.makeText(CameraActivity.this, "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
