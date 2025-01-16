package com.nahagos.nahagos;

import com.google.android.gms.maps.model.LatLng;
import com.nahagos.nahagos.datatypes.Line;
import com.nahagos.nahagos.datatypes.StopTime;


public class ServerAPI {
    public static boolean passengerLogin(String username, String password) {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\"}";
        return Boolean.TRUE.equals(Networks.httpPostReq(Endpoint.PASSENGER_LOGIN.getUrl(), jsonBody, Boolean.class));
    }

    public static boolean passengerSignup(String username, String password) {
        String jsonBody = "{\"username\": \"" + username + "\", \"password\":\"" + password + "\"}";
        return Boolean.TRUE.equals(Networks.httpPostReq(Endpoint.REGISTER.getUrl(), jsonBody, Boolean.class));
    }


    public static boolean driverLogin(String username, String password, int id) {
        String jsonBody = String.format("{\"username\": \"%s\", \"password\":\"%s\", \"id\":\"%s\"}", username, password, id);
        return Boolean.TRUE.equals(Networks.httpPostReq(Endpoint.DRIVER_LOGIN.getUrl(), jsonBody, Boolean.class));
    }

    public static Line[] getLinesByStation(int stopId) {
        return Networks.httpGetReq(Endpoint.GET_LINES_BY_STATION.getUrl() + stopId, Line[].class);
    }

    public static boolean waitForMe(String tripId, int stopId) {
        String jsonBody = "{\"trip_id\": \"" + tripId + "\", \"stop_id\":" + stopId + "}";
        return Boolean.TRUE.equals(Networks.httpPostReq(Endpoint.WAIT_FOR_ME.getUrl(), jsonBody, Boolean.class));
    }

    public static boolean registerForLine(String tripId) {
        String jsonBody = "{\"trip_id\": \"" + tripId + "\"}";
        return Boolean.TRUE.equals(Networks.httpPostReq(Endpoint.REGISTER_FOR_LINE.getUrl(), jsonBody, Boolean.class));
    }

    public static Line[][] getDriverSchedule() {
        return Networks.httpGetReq(Endpoint.GET_DRIVER_SCHEDULE.getUrl(), Line[][].class);
    }

    public static int[] getStoppingStations() {
        return Networks.httpGetReq(Endpoint.GET_STOPPING_STATIONS.getUrl(), int[].class);
    }

    public static StopTime[] getStopsByLine(String trip_id) {
        return Networks.httpGetReq(Endpoint.GET_STOPS_BY_LINE.getUrl() + trip_id, StopTime[].class);
    }

    public static LatLng[] getLineShape(String trip_id) {
        return Networks.httpGetReq(Endpoint.GET_LINE_SHAPE.getUrl() + trip_id, LatLng[].class);
    }

    private enum Endpoint {
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


        private static final String ROOT_URL = "http://nahagos.lavirz.com:8000";
        private final String path;

        Endpoint(String path) {
            this.path = path;
        }

        public String getUrl() {
            return ROOT_URL + path;
        }
    }
}
