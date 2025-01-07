package com.nahagos.nahagos;
import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.nahagos.nahagos.datatypes.Line;
import com.nahagos.nahagos.datatypes.StopTime;


public class ServerAPI {
    public enum Endpoint {
        PASSENGER_LOGIN("/passenger/login/"),
        DRIVER_LOGIN("/driver/login/"),
        REGISTER("/passenger/register/"),
        GET_LINES_BY_STATION("/lines-by-station/"),
        WAIT_FOR_ME("/passenger/wait-for/"),
        REGISTER_FOR_LINE("/driver/drive/register/"),
        GET_DRIVER_SCHEDULE("/driver/schedule/"),
        GET_STOPPING_STATIONS("/driver/where-to-stop/"),
        GET_STOPS_BY_LINE("/stops-by-line/"),
        GET_LINE_SHAPE("/line-shape/");

        private final String url;

        Endpoint(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

    private final String ROOT_URL;

    // Constructor accepting Context
    public ServerAPI(Context context) {
        ROOT_URL = "http://" + context.getString(R.string.server_ip) + ":8000";
    }

    public boolean passengerLogin(String username, String password)
    {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\"}";
        Gson response = Networks.httpPostReq(ROOT_URL + Endpoint.PASSENGER_LOGIN.getUrl(), jsonBody, Gson.class);
        Log.d(TAG, "login response: " + response);
        return response != null;

    }

    // Register a new passenger - client method
    public boolean passenger_signup(String username, String password)
    {
         String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\"}";
         Gson response = Networks.httpPostReq(ROOT_URL + Endpoint.REGISTER.getUrl(), jsonBody, Gson.class);
         return response != null;
    }

    public boolean driverLogin(String username, String password, String id)
    {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\","+"\"id\":\""+id+"\"}";
        Gson response = Networks.httpPostReq(ROOT_URL + Endpoint.DRIVER_LOGIN.getUrl(), jsonBody, Gson.class);
        Log.d(TAG, "login response: " + response);
        return response != null;

    }

    //get lines from a given station - client method
    public Line[] get_lines_by_station(int stopId) {
        String url = ROOT_URL + Endpoint.GET_LINES_BY_STATION.getUrl() + stopId;
        Line[] response = Networks.httpGetReq(url, Line[].class);
        if (response == null)
            return new Line[0];
        return response;
    }

    //passenger method - wait for a passenger at a given station
    public boolean wait_for_me(String trip_id, int stop_id)
    {
        String jsonBody = "{\"trip_id\": \"" + trip_id + "\", \"stop_id\":" + stop_id + "}";
        Gson response = Networks.httpPostReq(ROOT_URL + Endpoint.WAIT_FOR_ME.getUrl(), jsonBody, Gson.class);
        return response != null;
    }

    //register for a line - driver method
    public boolean register_for_line(String trip_id)
    {
        String jsonBody = "{\"trip_id\": \"" + trip_id + "\"}";
        Gson response = Networks.httpPostReq(ROOT_URL + Endpoint.REGISTER_FOR_LINE.getUrl(), jsonBody, Gson.class);
        return response != null;
    }

    //get driver schedule - driver method
    public Gson get_driver_schedule()
    {
        Gson response = Networks.httpGetReq(ROOT_URL + Endpoint.GET_DRIVER_SCHEDULE.getUrl(), Gson.class);
        return response;
    }

    //get stopping stations - driver method
    public int[] get_stopping_stations()
    {
        String url = ROOT_URL + Endpoint.GET_STOPPING_STATIONS.getUrl();
        int[] response = Networks.httpGetReq(url, int[].class);
        if (response == null)
            return new int[0];
        return response;
    }

    // get line stops - client method
    public StopTime[] get_stops_by_line(String trip_id)
    {
        String url = ROOT_URL + Endpoint.GET_STOPS_BY_LINE.getUrl() + trip_id;
        StopTime[] response = Networks.httpGetReq(url, StopTime[].class);
        if (response == null)
            return new StopTime[0];
        return  response;
    }

    // get shape of a given line - driver method
    public LatLng[] get_line_shape(String trip_id)
    {
        LatLng[] response = Networks.httpGetReq(ROOT_URL + Endpoint.GET_LINE_SHAPE.getUrl() + trip_id, LatLng[].class);
        if (response == null)
            return new LatLng[0];
        return response;
    }
}
