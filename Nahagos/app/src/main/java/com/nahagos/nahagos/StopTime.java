package com.nahagos.nahagos;

public class StopTime {
    public String time;
    public int stopId;
    public String stopName;
    public double lat;
    public double lon;
    public boolean to_stop;

    public StopTime(String time, int stopId, String stopName, double lat, double lon, boolean to_stop) {
        this.time = time;
        this.stopId = stopId;
        this.stopName = stopName;
        this.lat = lat;
        this.lon = lon;
        this.to_stop = to_stop;
    }
}
