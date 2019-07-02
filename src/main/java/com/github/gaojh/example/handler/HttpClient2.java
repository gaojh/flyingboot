package com.github.gaojh.example.handler;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author 高建华
 * @date 2019-05-16 12:49
 */
public class HttpClient2 {
    public static void main(String[] args) {
        String url = "http://localhost:12345/api/flaginfo";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();
        final Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            assert response.body() != null;
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
