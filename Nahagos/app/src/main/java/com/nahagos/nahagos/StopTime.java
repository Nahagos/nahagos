package com.nahagos.nahagos;

public class StopTime {
    public String time;
    public int stop_id;
    public String stop_name;
    public double stop_lat;
    public double stop_lon;

    public StopTime(String time, int stopId, String stopName, double lat, double lon) {
        this.time = time;
        this.stop_id = stopId;
        this.stop_name = stopName;
        this.stop_lat = lat;
        this.stop_lon = lon;
    }
}
