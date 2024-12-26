package com.nahagos.nahagos;
import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.nahagos.nahagos.Networks;

public class ServerAPI {
    private String PASSENGER_LOGIN_URL = "/passenger/login/";
    private String DRIVER_LOGIN_URL = "/driver/login/";
    private String ROOT_URL;

    // Constructor accepting Context
    public ServerAPI(Context context) {
        ROOT_URL = "http://" + context.getString(R.string.server_ip) + ":8000";
    }

    public void passengerLogin(String username, String password)
    {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\"}";
        Networks.httpPostReq(ROOT_URL + PASSENGER_LOGIN_URL, jsonBody);
    }

    public void driverLogin(String username, String password, String id)
    {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\","+"\"id\":\""+id+"\"}";
        Networks.httpPostReq(ROOT_URL + DRIVER_LOGIN_URL, jsonBody);
    }

    public Line[] getLinesByStation(int stopId) {
        String url = ROOT_URL + "/lines-by-station/" + stopId;
        String response = Networks.httpGetReq(url);

        if (response.startsWith("Error:")) {
            Log.e(TAG, response); // Log the error
            return null; // Handle the error as needed (e.g., return null or notify the user)
        }

        try {
            Gson gson = new Gson();
            return gson.fromJson(response, Line[].class); // Parse the JSON into Line[] array
        } catch (Exception e) {
            Log.e(TAG, "Error parsing response to Line[]", e);
            return null; // Handle parsing errors as needed
        }
    }

}
