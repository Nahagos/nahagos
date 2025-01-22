package com.nahagos.nahagos.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
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
import com.nahagos.nahagos.server.ServerAPI;


public class Login extends AppCompatActivity {
    private boolean isDriverGlobal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent stationsMapActivity = new Intent(this, StationsMap.class),
                driverScheduleActivity = new Intent(this, DriverSchedule.class);
        ImageView imgPoint = (ImageView) findViewById(R.id.imageView2);
        EditText usernameObj = findViewById(R.id.usernameField);
        EditText passwordObj = findViewById(R.id.passwordField);
        EditText driverIdObj = findViewById(R.id.idDriver);
        CheckBox isDriver = findViewById(R.id.checkBoxIsDriver);
        CheckBox rememberMe = findViewById(R.id.checkBoxRememberMe);
        TextView createAccountText = findViewById(R.id.createAccountText);

        createAccountText.setPaintFlags(createAccountText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        createAccountText.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUp.class);
            startActivity(intent);
        });

        isDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isDriverGlobal = isChecked;
            driverIdObj.setVisibility(isChecked ? TextView.VISIBLE : TextView.GONE);
        });

        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String storedUsername = sharedPreferences.getString("username", null);
        String storedPassword = sharedPreferences.getString("password", null);
        int storedDriverId = sharedPreferences.getInt("driverId", -1);

        if (storedUsername != null && storedPassword != null) {
            new Thread(() -> {
                boolean loginSuccess;
                if (storedDriverId != -1) {
                    loginSuccess = ServerAPI.driverLogin(storedUsername, storedPassword, storedDriverId);
                } else {
                    loginSuccess = ServerAPI.passengerLogin(storedUsername, storedPassword);
                }

                if (loginSuccess) {
                    runOnUiThread(() -> {
                        Intent intent = storedDriverId != -1 ? driverScheduleActivity : stationsMapActivity;
                        startActivity(intent);
                        finish(); // Finish the login activity to prevent back navigation
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(Login.this, "Stored credentials are invalid. Please log in again.", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }


        Button button = findViewById(R.id.loginButton);
        button.setOnClickListener(v -> {
            String username = usernameObj.getText().toString().trim();
            String password = passwordObj.getText().toString().trim();
            String driverId = driverIdObj.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty() && (!isDriverGlobal || !driverId.isEmpty())) {
                Log.d("Login", Boolean.toString(isDriverGlobal));

                new Thread(() -> {
                    if (isDriverGlobal)
                    {
                        if (ServerAPI.driverLogin(username, password, Integer.parseInt(driverId)))
                        {
                            if (rememberMe.isChecked()) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", username);
                                editor.putString("password", password);
                                editor.putInt("driverId", Integer.parseInt(driverId));
                                editor.apply();
                            }
                            runOnUiThread(() -> startActivity(driverScheduleActivity));
                        }
                        else
                        {
                            runOnUiThread(() -> Toast.makeText(Login.this, "Username, id or password incorrect", Toast.LENGTH_SHORT).show());
                        }
                    }
                    else if (ServerAPI.passengerLogin(username, password))
                    {
                        if (rememberMe.isChecked()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.apply();
                        }
                        runOnUiThread(() -> startActivity(stationsMapActivity));
                    }
                    else {
                        runOnUiThread(() -> Toast.makeText(Login.this, "Username or password incorrect", Toast.LENGTH_SHORT).show());
                    }

                }).start();
            } else {
                Toast.makeText(Login.this, "make sure you fill all of your fields ಥ_ಥ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}