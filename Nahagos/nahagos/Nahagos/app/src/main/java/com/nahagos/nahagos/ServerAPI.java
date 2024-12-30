package com.nahagos.nahagos;
import android.content.Context;
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
}
