package com.nahagos.nahagos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.server.ServerAPI;


public class Login extends AppCompatActivity {
    private boolean isDriverGlobal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ServerAPI serverAPI = new ServerAPI(this);
        setContentView(R.layout.activity_main);

        Intent stationsMapActivity = new Intent(this, StationsMap.class),
                driverScheduleActivity = new Intent(this, DriverSchedule.class);
        ImageView imgPoint = (ImageView) findViewById(R.id.imageView2);
        EditText usernameObj = findViewById(R.id.usernameField);
        EditText passwordObj = findViewById(R.id.passwordField);
        EditText driverIdObj = findViewById(R.id.idDriver);
        TextView emptyFields = findViewById(R.id.emptyFields_id);
        CheckBox isDriver = findViewById(R.id.checkBoxIsDriver);

        isDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isDriverGlobal = isChecked;
            driverIdObj.setVisibility(isChecked ? TextView.VISIBLE : TextView.GONE);
        });

        Button button = findViewById(R.id.loginButton);
        button.setOnClickListener(v -> {
            String username = usernameObj.getText().toString().trim();
            String password = passwordObj.getText().toString().trim();
            String driverId = driverIdObj.getText().toString().trim();
            emptyFields.setVisibility(TextView.GONE);

            if (!username.isEmpty() && !password.isEmpty() && (!isDriverGlobal || !driverId.isEmpty())) {
                Log.d("Login", Boolean.toString(isDriverGlobal));

                new Thread(() -> {
                    if (isDriverGlobal) {
                        if (serverAPI.driverLogin(username, password, driverId))
                            runOnUiThread(() -> startActivity(stationsMapActivity));
                    } else if (serverAPI.passengerLogin(username, password))
                        runOnUiThread(() -> startActivity(driverScheduleActivity));
                }).start();
            } else {
                emptyFields.setVisibility(TextView.VISIBLE);
            }
        });
    }
}