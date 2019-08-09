package com.maxdemarzi;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface FullContactAPI {

    @POST("v3/person.enrich")
    //Call<ResponseBody> enrich(@Body RequestBody body);
    Call<Map<String, Object>> enrich(@Body RequestBody body);
}
