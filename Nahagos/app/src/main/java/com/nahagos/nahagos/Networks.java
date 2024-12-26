package com.nahagos.nahagos;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Networks {
    private static final String TAG = "HTTP";

    private static String sessionCookie; // To store the session cookie

    // Helper method to set up a connection
    private static HttpURLConnection setupConnection(String urlString, String method) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000); // 5sec timeout
        connection.setReadTimeout(5000);

        if (sessionCookie != null) {
            connection.setRequestProperty("Cookie", sessionCookie); // Send the session cookie
        }

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
    public static String httpGetReq(String urlString) {
        HttpURLConnection connection = null;

        try {
            connection = setupConnection(urlString, "GET");
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                Log.d(TAG, "Response: " + response);
                return response;
            }
            else {
                Log.e(TAG, "HTTP GET request failed with response code: " + responseCode);
                return "Error: HTTP " + responseCode;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in HTTP GET request", e);
            return "Error: " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String httpPostReq(String urlString, String postData) {
        HttpURLConnection connection = null;

        try {
            connection = setupConnection(urlString, "POST");

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
                String response = readResponse(connection);
                Log.d(TAG, "Response: " + response);
                return response;
            }
            else {
                Log.e(TAG, "HTTP POST request failed with response code: " + responseCode);
                return "Error: HTTP " + responseCode; // Return error code if not OK;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in HTTP POST request", e);
            return "Error: " + e.getMessage(); // Return exception message
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
