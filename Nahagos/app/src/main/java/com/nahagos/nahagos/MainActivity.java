package com.nahagos.nahagos;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.TextView;
import okhttp3.*;
import com.nahagos.nahagos.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        TextView titleText = findViewById(R.id.welcomeText);

        titleText.setText("Welcome to Nahagos!");
        ImageView imgPoint = (ImageView)findViewById(R.id.imageView2);
        imgPoint.setX(-7);
        imgPoint.setY(280);


        int port = 8000;
        String ip = "172.20.20.36";
        String url = "https://" + ip + ":" + port + "/login:";
        String jsonBody = "{\"name\": \"talShahar\", \"password\": Aa12456}";

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void makeHttpRequest() {
        String urlString = "https://jsonplaceholder.typicode.com/posts"; // Example URL
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
