package com.spldeolin.allison1875.docanalyzer.util;

import com.spldeolin.allison1875.base.util.JsonUtils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author Deolin 2021-01-01
 */
public class RetrofitUtils {

    private static final Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(JacksonConverterFactory.create(JsonUtils.createObjectMapper()));

    public static <T> T createApi(Class<T> apiType) {
        return retrofitBuilder.build().create(apiType);
    }

    public static <T> T createApi(String baseUrl, Class<T> apiType) {
        return retrofitBuilder.baseUrl(baseUrl).build().create(apiType);
    }

}