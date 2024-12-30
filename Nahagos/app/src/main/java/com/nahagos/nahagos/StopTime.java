package com.nahagos.nahagos;

public class StopTime {
    private String time;
    private int stopId;
    private String stopName;
    private double lat;
    private double lon;
    private boolean to_stop;

    public StopTime(String time, int stopId, String stopName, double lat, double lon, boolean to_stop) {
        this.time = time;
        this.stopId = stopId;
        this.stopName = stopName;
        this.lat = lat;
        this.lon = lon;
        this.to_stop = to_stop;
    }

    public String getTime() {
        return time;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public boolean isTo_stop() {
        return to_stop;
    }

    public void setTo_stop(boolean to_stop) {
        this.to_stop = to_stop;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public int getStopId() {
        return stopId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
