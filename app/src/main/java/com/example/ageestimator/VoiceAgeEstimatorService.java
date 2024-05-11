package com.example.ageestimator;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface VoiceAgeEstimatorService {
    @POST("/models/versae/wav2vec2-base-finetuned-coscan-age_group")
    Call<List<VoiceAgeEstimate>> estimateVoiceAge(@Body RequestBody voice, @Header("Authorization") String authHeader);

}
