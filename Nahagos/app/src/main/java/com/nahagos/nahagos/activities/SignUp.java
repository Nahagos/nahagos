package com.nahagos.nahagos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.db.SharedPreferencesManager;
import com.nahagos.nahagos.server.ServerAPI;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferencesManager preferencesManager = new SharedPreferencesManager(this);
        Intent stationsMapActivity = new Intent(this, StationsMap.class);

        EditText usernameObj = findViewById(R.id.usernameField);
        EditText passwordObj = findViewById(R.id.passwordField);
        EditText confirmPasswordObj = findViewById(R.id.confirmPasswordField);
        Button button = findViewById(R.id.signupButton);
        CheckBox rememberMe = findViewById(R.id.checkBoxRememberMe);

        button.setOnClickListener(view -> {
            String username = usernameObj.getText().toString().trim();
            String password = passwordObj.getText().toString().trim();
            String confirmPassword = confirmPasswordObj.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignUp.this, "Make sure you fill all of your fields ಥ_ಥ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                if (ServerAPI.passengerSignup(username, password)) {
                    if (rememberMe.isChecked()) {
                        preferencesManager.saveUserCredentials(username, password, null);
                    }
                    runOnUiThread(() -> startActivity(stationsMapActivity));
                } else {
                    runOnUiThread(() -> Toast.makeText(SignUp.this, "Username already exists", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

    }
}