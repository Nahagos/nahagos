package com.nahagos.nahagos;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Networks {
     public void makeHttpRequest(String urlString) {
        HttpURLConnection connection = null;

        try {
            // Create URL object
            URL url = new URL(urlString);

            // Open connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); // or "POST", "PUT", etc.
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000);

            // Send the request
            int responseCode = connection.getResponseCode();
            Log.d("HTTP", "Response Code: " + responseCode);

            // Read the response
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                Log.d("HTTP", "Response: " + response.toString());
            }

        } catch (Exception e) {
            Log.e("HTTP", "Error in HTTP request", e);
        } finally {
            if (connection != null) {
                connection.disconnect(); // Close the connection
            }
        }
    }
}

}
