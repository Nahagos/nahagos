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

    private boolean isCheckedGlobal = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ServerAPI serverAPI = new ServerAPI(this);
        setContentView(R.layout.activity_main);

        ImageView imgPoint = (ImageView) findViewById(R.id.imageView2);
        EditText usernameObj = findViewById(R.id.usernameField);
        EditText passwordObj = findViewById(R.id.passwordField);
        EditText driverIdObj = findViewById(R.id.idDriver);
        CheckBox isDriver = findViewById(R.id.checkBoxIsDriver);

        // safe mode for networking in android
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

        isDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    isCheckedGlobal = isChecked;
                    if (isChecked) {
                        Log.d("MainActivity", "kaki");
                        driverIdObj.setVisibility(TextView.VISIBLE);
                    }
                    else{
                        driverIdObj.setVisibility(TextView.GONE);
                    }
                });

        Button button = findViewById(R.id.loginButton);
        button.setOnClickListener(v -> {

            String username  = usernameObj.getText().toString();
            String password  = passwordObj.getText().toString();
            String driverId  =  driverIdObj.getText().toString();

            if (username != null && password != null) {
                Log.d("MainActivity","not null");
                Log.d("MainActivity", Boolean.toString(isCheckedGlobal));

                if (!isCheckedGlobal) {
                    serverAPI.passengerLogin(username, password);
                    Log.d("MainActivity","login");
                }
                else
                    serverAPI.driverLogin(username, password, driverId);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}