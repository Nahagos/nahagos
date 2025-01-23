package com.nahagos.nahagos.server;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Networks {
    private static final String TAG = "HTTP";

    private static String sessionCookie;

    private static final Gson gson = new Gson();

    public static HttpURLConnection setupConnection(String urlString, String method) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        if (sessionCookie != null)
            connection.setRequestProperty("Cookie", sessionCookie);

        if ("POST".equalsIgnoreCase(method)) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
        }

        return connection;
    }

    private static String readResponse(HttpURLConnection connection) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                responseBuilder.append(line);
            return responseBuilder.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private static void saveCookies(HttpURLConnection connection) {
        String cookieHeader = connection.getHeaderField("Set-Cookie");
        if (cookieHeader == null) return;
        Arrays.stream(cookieHeader.split(";")).filter(cookie -> cookie.startsWith("cookies_and_milk=")).findFirst().ifPresent(cookie -> {
            sessionCookie = cookie;
            Log.d(TAG, "Session Cookie saved: " + sessionCookie);
        });
    }

    public static <T> T httpGetReq(String requestUrl, Class<T> responseType) {
        HttpURLConnection connection = null;
        try {
            connection = setupConnection(requestUrl, "GET");
            int responseCode = connection.getResponseCode();
            String response = readResponse(connection);

            if (responseCode != HttpURLConnection.HTTP_OK)
                throw new IOException("HTTP GET request to " + requestUrl + " failed with response code: " + responseCode + " and body:" + response);
            return gson.fromJson(response, responseType);
        } catch (Exception e) {
            Log.e(TAG, "Error in HTTP GET request", e);
            return null;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }


    public static <T> T httpPostReq(String requestUrl, String postData, Class<T> responseType) {
        HttpURLConnection connection = null;
        try {
            connection = setupConnection(requestUrl, "POST");
            connection.setFixedLengthStreamingMode(postData.getBytes().length);

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(postData);
            }
            int responseCode = connection.getResponseCode();
            String response = readResponse(connection);
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED)
                throw new IOException("HTTP POST request to " + requestUrl + " failed with response code: " + responseCode + " and body: " + response);

            saveCookies(connection);

            return responseType == Boolean.class ?
                    responseType.cast(Boolean.TRUE) :
                    gson.fromJson(response, responseType);
        } catch (Exception e) {
            Log.e(TAG, "Error in HTTP POST request", e);
            return null;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }
}
