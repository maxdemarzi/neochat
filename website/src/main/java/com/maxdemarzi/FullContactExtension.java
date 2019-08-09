package com.maxdemarzi;

import com.typesafe.config.Config;
import io.jooby.*;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.Nonnull;

public class FullContactExtension implements Extension {
    @Override
    public void install(@Nonnull Jooby application) throws Exception {
        Environment env = application.getEnvironment();
        Config conf = env.getConfig();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(chain -> chain.proceed(
                chain.request().newBuilder()
                        .addHeader("Authorization","Bearer " + conf.getString("fullcontact.key"))
                        .build()));
        OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://api.fullcontact.com/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        FullContactAPI fullContactAPI = retrofit.create(FullContactAPI.class);
        ServiceRegistry registry = application.getServices();
        registry.put(FullContactAPI.class, fullContactAPI);
    }
}
