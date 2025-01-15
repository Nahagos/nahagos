package com.nahagos.nahagos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.server.ServerAPI;


public class MainActivity extends AppCompatActivity {

    private boolean isCheckedGlobal = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ServerAPI serverAPI = new ServerAPI(this);
        setContentView(R.layout.activity_main);

        Intent maps = new Intent(this, PassengerUI.class);
        ImageView imgPoint = (ImageView) findViewById(R.id.imageView2);
        EditText usernameObj = findViewById(R.id.usernameField);
        EditText passwordObj = findViewById(R.id.passwordField);
        EditText driverIdObj = findViewById(R.id.idDriver);
        TextView emptyFields = findViewById(R.id.emptyFields_id);
        CheckBox isDriver = findViewById(R.id.checkBoxIsDriver);



        // safe mode for networking in android
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

        isDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    isCheckedGlobal = isChecked;
                    if (isChecked)
                        driverIdObj.setVisibility(TextView.VISIBLE);
                    
                    else
                        driverIdObj.setVisibility(TextView.GONE);

                });

        Button button = findViewById(R.id.loginButton);
        button.setOnClickListener(v -> {

            String username  = usernameObj.getText().toString().trim();
            String password  = passwordObj.getText().toString().trim();
            String driverId  = driverIdObj.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                Log.d("MainActivity", Boolean.toString(isCheckedGlobal));
                emptyFields.setVisibility(TextView.GONE);

                if (isCheckedGlobal) {
                    if (!driverId.isEmpty()) {
                        emptyFields.setVisibility(TextView.GONE);
                        if (serverAPI.driverLogin(username, password, driverId))
                            startActivity(maps);
                    }
                    else {
                        emptyFields.setVisibility(TextView.VISIBLE);
                    }
                }
                else {
                    emptyFields.setVisibility(TextView.GONE);
                    if (serverAPI.passengerLogin(username, password))
                        startActivity(maps);
                }
            }
            else {
                emptyFields.setVisibility(TextView.VISIBLE);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}