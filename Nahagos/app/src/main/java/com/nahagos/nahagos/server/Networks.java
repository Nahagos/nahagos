package com.nahagos.nahagos.server;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Networks {
    private static final String TAG = "HTTP";


    // Helper method to set up a connection
    private static HttpURLConnection setupConnection(String urlString, String method) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000); // 5sec timeout
        connection.setReadTimeout(5000);

        if ("POST".equalsIgnoreCase(method)) {
            connection.setDoOutput(true); // Enable output for POST
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

    // Method for HTTP GET request
    public static void httpGetReq(String urlString) {
        HttpURLConnection connection = null;

        try {
            connection = setupConnection(urlString, "GET");
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);

                Log.d(TAG, "Response: " + response);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in HTTP GET request", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static int httpPostReq(String urlString, String postData) {
        HttpURLConnection connection = null;
        int responseCode = -1;
        try {
            connection = setupConnection(urlString, "POST");

            // Write data to the output stream
            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                writer.write(postData);
                writer.flush();
            }

            responseCode = connection.getResponseCode();
            Log.d(TAG, "Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                String response = readResponse(connection);
                Log.d(TAG, "Response: " + response);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in HTTP POST request", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            return responseCode;
        }
    }
}
