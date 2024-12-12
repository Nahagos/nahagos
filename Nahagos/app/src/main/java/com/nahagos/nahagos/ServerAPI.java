package com.nahagos.nahagos;
import android.content.Context;
import com.nahagos.nahagos.Networks;

public class ServerAPI {
    private String LOGIN_URL = "/login/";
    private String ROOT_URL;

    // Constructor accepting Context
    public ServerAPI(Context context) {
        ROOT_URL = "http://" + context.getString(R.string.server_ip) + ":8000";
    }

    public static void login(String username, String password)
    {
        String jsonBody = "{\"username\": '" + username + "', \"password\":'" + password + "'}";
        Networks.httpPostReq(ROOT_URL+LOGIN_URL, jsonBody);

    }
}
