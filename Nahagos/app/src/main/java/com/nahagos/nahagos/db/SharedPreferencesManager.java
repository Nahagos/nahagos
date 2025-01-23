package com.nahagos.nahagos.db;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREFS_NAME = "user_info";
    private final SharedPreferences sharedPreferences;

    public SharedPreferencesManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserCredentials(String username, String password, Integer driverId) {
        SharedPreferences.Editor editor = sharedPreferences.edit().putString("username", username).putString("password", password);
        if (driverId != null) {
            editor.putInt("driverId", driverId);
        } else {
            editor.remove("driverId");
        }
        editor.apply();
    }

    public String getUsername() {
        return sharedPreferences.getString("username", null);
    }

    public String getPassword() {
        return sharedPreferences.getString("password", null);
    }

    public int getDriverId() {
        return sharedPreferences.getInt("driverId", -1);
    }

    public void clearUserCredentials() {
        sharedPreferences.edit().clear().apply();
    }
}
