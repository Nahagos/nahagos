package com.nahagos.nahagos;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.nahagos.nahagos.datatypes.Line;
import com.nahagos.nahagos.datatypes.Schedule;
import com.nahagos.nahagos.datatypes.StopTime;



public class ServerAPI {
    private static final String ROOT_URL = "http://" + BuildConfig.SERVER_IP + ":8000";
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
            return ROOT_URL + url;
        }
    }


    public static boolean passengerLogin(String username, String password) {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\"}";
        Gson response = Networks.httpPostReq(Endpoint.PASSENGER_LOGIN.getUrl(), jsonBody, Gson.class);
        Log.d(TAG, "login response: " + response);
        return response != null;

    }

    // Register a new passenger - client method
    public static boolean passengerSignup(String username, String password) {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\"}";
        return Networks.httpPostReq(Endpoint.REGISTER.getUrl(), jsonBody);
    }

    public static boolean driverLogin(String username, String password, String id) {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\"," + "\"id\":\"" + id + "\"}";
        return Networks.httpPostReq(Endpoint.DRIVER_LOGIN.getUrl(), jsonBody);

    }

    //get lines from a given station - client method
    public static Line[] getLinesByStation(int stopId) {
        Line[] response = Networks.httpGetReq(Endpoint.GET_LINES_BY_STATION.getUrl() + stopId, Line[].class);
        if (response == null)
            return new Line[0];
        return response;
    }

    //passenger method - wait for a passenger at a given station
    public static boolean waitForMe(String trip_id, int stop_id) {
        String jsonBody = "{\"trip_id\": \"" + trip_id + "\", \"stop_id\":" + stop_id + "}";
        return Networks.httpPostReq(Endpoint.WAIT_FOR_ME.getUrl(), jsonBody);
    }

    //register for a line - driver method
    public static boolean registerForLine(String trip_id) {
        String jsonBody = "{\"trip_id\": \"" + trip_id + "\"}";
        return Networks.httpPostReq(Endpoint.REGISTER_FOR_LINE.getUrl(), jsonBody);
    }

    //get driver schedule - driver method
    public static Schedule getDriverSchedule() {
        Schedule response = Networks.httpGetReq(Endpoint.GET_DRIVER_SCHEDULE.getUrl(), Schedule.class);
        Log.d(TAG, "Driver Schedule: " + response);
        return response;
    }


    //get stopping stations - driver method
    public static int[] getStoppingStations() {
        int[] response = Networks.httpGetReq(Endpoint.GET_STOPPING_STATIONS.getUrl(), int[].class);
        if (response == null)
            return new int[0];
        return response;
    }

    // get line stops - client method
    public static StopTime[] getStopsByLine(String trip_id) {
        StopTime[] response = Networks.httpGetReq(Endpoint.GET_STOPS_BY_LINE.getUrl() + trip_id, StopTime[].class);
        if (response == null)
            return new StopTime[0];
        return response;
    }

    // get shape of a given line - driver method
    public static LatLng[] getLineShape(String trip_id) {
        LatLng[] response = Networks.httpGetReq(Endpoint.GET_LINE_SHAPE.getUrl() + trip_id, LatLng[].class);
        if (response == null)
            return new LatLng[0];
        return response;
    }
}
