package com.nahagos.nahagos;
import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerAPI {
    private final String PASSENGER_LOGIN_URL = "/passenger/login/";
    private final String DRIVER_LOGIN_URL = "/driver/login/";
    private final String REGISTER_URL = "/passenger/register/";
    private final String GET_LINES_BY_STATION_URL = "/lines-by-station/";
    private final String WAIT_FOR_ME_URL = "/passenger/wait-for/";
    private final String REGISTER_FOR_LINE_URL = "/driver/register-for/";
    private final String GET_DRIVER_SCHEDULE_URL = "/driver/schedule/";
    private final String GET_STOPPING_STATIONS_URL = "/driver/where-to-stop/";
    private final String GET_STOPS_BY_LINE_URL = "/stops-by-line/";
    private final String GET_LINE_SHAPE = "/line-shape/";
    private final String ROOT_URL;

    // Constructor accepting Context
    public ServerAPI(Context context) {
        ROOT_URL = "http://" + context.getString(R.string.server_ip) + ":8000";
    }

    public boolean passengerLogin(String username, String password)
    {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\"}";
        return Networks.httpPostReq(ROOT_URL + PASSENGER_LOGIN_URL, jsonBody) == 200;
    }

    public boolean driverLogin(String username, String password, String id)
    {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\","+"\"id\":\""+id+"\"}";
        return Networks.httpPostReq(ROOT_URL + DRIVER_LOGIN_URL, jsonBody) == 200;
    }

    //get lines from a given station - client method
    public Line[] get_lines_by_station(int stopId) {
        String url = ROOT_URL + GET_LINES_BY_STATION_URL + stopId;
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

    //passenger method - wait for a passenger at a given station
    public boolean wait_for_me(String trip_id, int stop_id)
    {
        String jsonBody = "{\"trip_id\": \"" + trip_id + "\", \"stop_id\":" + stop_id + "}";
        int response = Networks.httpPostReq(ROOT_URL + WAIT_FOR_ME_URL, jsonBody);
        return response == 200;
    }

    //register for a line - driver method
    public boolean register_for_line(String trip_id)
    {
        String jsonBody = "{\"trip_id\": " + trip_id + "}";
        int response = Networks.httpPostReq(ROOT_URL + REGISTER_FOR_LINE_URL, jsonBody);
        return response == 200;
    }

    //get driver schedule - driver method
    public Gson get_driver_schedule()
    {
        String response = Networks.httpGetReq(ROOT_URL + GET_DRIVER_SCHEDULE_URL);
        if (response.startsWith("Error"))
        {
            Log.e(TAG, response); // Log the error
            return null;
        }
        else
        {
            try
                {
                Gson gson = new Gson();
                return gson.fromJson(response, Gson.class);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Error parsing response to Gson", e);
                return null;
            }
        }

    }

    //get stopping stations - driver method
    public int[] get_stopping_stations()
    {
        String url = ROOT_URL + GET_STOPPING_STATIONS_URL;
        String response = Networks.httpGetReq(url);
        if (response.startsWith("Error:")) {
            Log.e(TAG, response); // Log the error
            return null; // Handle the error as needed (e.g., return null or notify the user)
        }

        try {
            Gson gson = new Gson();
            return gson.fromJson(response, int[].class); // Parse the JSON into int[] array
        } catch (Exception e) {
            Log.e(TAG, "Error parsing response to int[]", e);
            return null; // Handle parsing errors as needed
        }
    }

    // get line stops - client method
    public StopTime[] get_stops_by_line(String trip_id)
    {
        String url = ROOT_URL + GET_STOPS_BY_LINE_URL + trip_id;
        String response = Networks.httpGetReq(url);
        if (response.startsWith("Error:")) {
            Log.e(TAG, response); // Log the error
            return new StopTime[0]; // Handle the error as needed (e.g., return null or notify the user)
        }

        else
        {
            try
            {
                StopTime[] out = new Gson().fromJson(response, StopTime[].class);
                if (out == null)
                    out = new StopTime[0];
                return out;
            }
            catch (Exception e)
            {
                Log.e(TAG, "Error parsing response to Gson", e);
                return new StopTime[0];
            }
        }
    }

    // get shape of a given line - driver method
    public Point[] get_line_shape(int trip_id)
    {
        String response = Networks.httpGetReq(ROOT_URL + GET_LINE_SHAPE + trip_id);
        if (response.startsWith("Error"))
        {
            Log.e(TAG, response); // Log the error
            return null;
        }
        else
        {
            try {
                Gson gson = new Gson();
                JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                if (!jsonResponse.has("Shape")) {
                    Log.e(TAG, "Missing 'Shape' field in response");
                    return null;
                }

                return gson.fromJson(jsonResponse.getAsJsonArray("Shape"), Point[].class);
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "Error parsing 'Shape' field", e);
                return null;
            }
        }
    }
}
