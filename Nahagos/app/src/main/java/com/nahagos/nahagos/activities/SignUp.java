package com.nahagos.nahagos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.server.ServerAPI;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent stationsMapActivity = new Intent(this, StationsMap.class);

        EditText usernameObj = findViewById(R.id.usernameField);
        EditText passwordObj = findViewById(R.id.passwordField);
        EditText confirmPasswordObj = findViewById(R.id.confirmPasswordField);
        Button button = findViewById(R.id.signupButton);

        button.setOnClickListener(view -> {
            String username = usernameObj.getText().toString().trim();
            String password = passwordObj.getText().toString().trim();
            String confirmPassword = confirmPasswordObj.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty())
            {
                if (password.equals(confirmPassword))
                {
                    new Thread(() -> {
                        if (ServerAPI.passengerSignup(username, password))
                            runOnUiThread(() -> startActivity(stationsMapActivity));
                        else
                            runOnUiThread(() -> Toast.makeText(SignUp.this, "Username already exists", Toast.LENGTH_SHORT).show());
                    }).start();
                }
                else
                    Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(SignUp.this, "make sure you fill all of your fields ಥ_ಥ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}