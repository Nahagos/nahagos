package com.nahagos.nahagos.datatypes;

public class StopTime {
    public String time;
    public int stop_id;
    public String stop_name;
    public double lat;
    public double lon;
    public boolean to_stop;

    public StopTime(String time, int stop_id, String stop_name, double lat, double lon, boolean to_stop) {
        this.time = time;
        this.stop_id = stop_id;
        this.stop_name = stop_name;
        this.lat = lat;
        this.lon = lon;
        this.to_stop = to_stop;
    }
}
