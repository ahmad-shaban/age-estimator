package com.example.ageestimator;

import java.util.List;

import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FaceAgeEstimatorService {
    @POST("/models/nateraw/vit-age-classifier")
    Call<List<FaceAgeEstimate>> estimateFaceAge(@Body RequestBody image, @Header("Authorization") String authHeader);
}
