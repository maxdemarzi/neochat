package com.maxdemarzi;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface FullContactAPI {

    @POST("v3/person.enrich")
    Call<Map<String, Object>> enrich(@Body RequestBody body);
}
