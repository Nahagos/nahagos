package com.nahagos.nahagos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.db.SharedPreferencesManager;
import com.nahagos.nahagos.server.ServerAPI;


public class Login extends AppCompatActivity {
    private boolean isDriverGlobal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent stationsMapActivity = new Intent(this, StationsMap.class),
                driverScheduleActivity = new Intent(this, DriverSchedule.class);
        ImageView imgPoint = findViewById(R.id.imageView2);
        EditText usernameObj = findViewById(R.id.usernameField);
        EditText passwordObj = findViewById(R.id.passwordField);
        EditText driverIdObj = findViewById(R.id.idDriver);
        CheckBox isDriver = findViewById(R.id.checkBoxIsDriver);
        CheckBox rememberMe = findViewById(R.id.checkBoxRememberMe);
        TextView createAccountText = findViewById(R.id.createAccountText);

        createAccountText.setOnClickListener(v -> startActivity(new Intent(this, SignUp.class)));

        isDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isDriverGlobal = isChecked;
            driverIdObj.setVisibility(isChecked ? TextView.VISIBLE : TextView.GONE);
        });

        SharedPreferencesManager preferencesManager = new SharedPreferencesManager(this);

        String storedUsername = preferencesManager.getUsername();
        String storedPassword = preferencesManager.getPassword();
        int storedDriverId = preferencesManager.getDriverId();

        if (storedUsername != null && storedPassword != null) {
            usernameObj.setText(storedUsername);
            passwordObj.setText(storedPassword);
            rememberMe.setChecked(true);
            if (isDriverGlobal && storedDriverId != -1) {
                driverIdObj.setText(String.valueOf(storedDriverId));
            }
        }


        Button button = findViewById(R.id.loginButton);
        button.setOnClickListener(v -> {
            String username = usernameObj.getText().toString().trim();
            String password = passwordObj.getText().toString().trim();
            String driverId = driverIdObj.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || (isDriverGlobal && driverId.isEmpty())) {
                Toast.makeText(Login.this, "Make sure you fill all of your fields ಥ_ಥ", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                if (isDriverGlobal && ServerAPI.driverLogin(username, password, Integer.parseInt(driverId))) {
                    if (rememberMe.isChecked()) {
                        preferencesManager.saveUserCredentials(username, password, Integer.parseInt(driverId));
                    }
                    runOnUiThread(() -> startActivity(driverScheduleActivity));
                } else if (ServerAPI.passengerLogin(username, password)) {
                    if (rememberMe.isChecked()) {
                        preferencesManager.saveUserCredentials(username, password, null);
                    }
                    runOnUiThread(() -> startActivity(stationsMapActivity));
                } else {
                    runOnUiThread(() -> Toast.makeText(Login.this, "Some fields are incorrect", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }
}