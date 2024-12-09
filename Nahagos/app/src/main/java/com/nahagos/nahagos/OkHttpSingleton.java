package com.nahagos.nahagos;
import okhttp3.OkHttpClient;
public class OkHttpSingleton {
    private static OkHttpClient client;

    private OkHttpSingleton() { }

    public static OkHttpClient getInstance() {
        if (client == null) {
            client = new OkHttpClient.Builder().build();
        }
        return client;
    }
}
