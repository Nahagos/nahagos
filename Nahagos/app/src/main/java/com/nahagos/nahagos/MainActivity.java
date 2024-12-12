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

import android.widget.EditText;
import android.widget.Button;
import android.util.Log;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.nahagos.nahagos.Networks;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.TextView;
import org.json.JSONObject;
import android.widget.CheckBox;
import okhttp3.*;
import com.nahagos.nahagos.R;
import com.nahagos.nahagos.ServerAPI;
import android.content.Context;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        TextView titleText = findViewById(R.id.welcomeText);

        titleText.setText("Welcome to Nahagos!");
        ImageView imgPoint = (ImageView) findViewById(R.id.imageView2);
        imgPoint.setX(-7);
        imgPoint.setY(280);

        EditText usernameObj = findViewById(R.id.usernameField);
        EditText passwordObj = findViewById(R.id.passwordField);

        ServerAPI serverAPI = new ServerAPI(this);

        EditText driverId = findViewById(R.id.idDriver);
        CheckBox isDriver = findViewById(R.id.checkBoxIsDriver);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        isDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        Log.d("MainActivity", "kaki");
                        driverId.setVisibility(TextView.VISIBLE);
                    }
                    else{
                        driverId.setVisibility(TextView.GONE);
                    }
                });
        Button button = findViewById(R.id.loginButton); // Ensure a button exists in your layout
        button.setOnClickListener(v -> {
            // Get the text from EditText
            String username = usernameObj.getText().toString();
            String password = passwordObj.getText().toString();

            serverAPI.login("username", "password");
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}