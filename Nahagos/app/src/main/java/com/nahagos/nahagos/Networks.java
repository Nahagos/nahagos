package com.nahagos.nahagos;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Networks {
    private static final String TAG = "HTTP";

    private static String sessionCookie;

    private static final Gson gson = new Gson();


    // Helper method to set up a connection
    private static HttpURLConnection setupConnection(String urlString, String method) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        if (sessionCookie != null) {
            connection.setRequestProperty("Cookie", sessionCookie);
        }

        if ("POST".equalsIgnoreCase(method)) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
        }

        return connection;
    }

    // Helper method to read response
    private static String readResponse(HttpURLConnection connection) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        return response.toString();
    }

    // Helper method to save cookies
    private static void saveCookies(HttpURLConnection connection) {
        String cookieHeader = connection.getHeaderField("Set-Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                if (cookie.startsWith("cookies_and_milk=")) {
                    sessionCookie = cookie;
                    Log.d(TAG, "Session Cookie saved: " + sessionCookie);
                    break;
                }
            }
        }
    }

    // Method for HTTP GET request
    // Method with deserializer (used when needed)
    public static <T> T httpGetReq(String requestUrl, Class<T> responseType, JsonDeserializer<T> deserializer) {
        Gson gson = deserializer != null
                ? new GsonBuilder().registerTypeAdapter(responseType, deserializer).create()
                : Networks.gson;

        HttpURLConnection connection = null;
        try {
            // Setup connection
            connection = setupConnection(requestUrl, "GET");
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Response Code: " + responseCode);
            // Read response
            String response = readResponse(connection);
            Log.d(TAG, "Response: " + response);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Deserialize
                return gson.fromJson(response, responseType);
            } else {
                Log.e(TAG, "HTTP GET request failed with response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in HTTP GET request", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Method without deserializer (default)
    public static <T> T httpGetReq(String requestUrl, Class<T> responseType) {
        return httpGetReq(requestUrl, responseType, null);
    }


    public static <T> T httpPostReq(String requestUrl, String postData, Class<T> responseType) {
        HttpURLConnection connection = null;
        try {
            // Setup connection
            connection = setupConnection(requestUrl, "POST");

            // Write data to the output stream
            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                writer.write(postData);
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                if (sessionCookie == null) {
                    saveCookies(connection);
                }
                // Read and parse the response
                String response = readResponse(connection);
                Log.d(TAG, "Response: " + response);
                // Convert the response to the specified class type
                return gson.fromJson(response, responseType);
            } else {
                Log.e(TAG, "HTTP POST request failed with response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in HTTP POST request", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
