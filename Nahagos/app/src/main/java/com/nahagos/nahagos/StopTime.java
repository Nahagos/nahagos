package com.nahagos.nahagos;

public class StopTime {
    public StopTime(String t, int stop_id, String stop_name, boolean to_stop) {
        time = t;
        stopId = stop_id;
        stopName = stop_name;
        toStop = to_stop;
    }

    public String time;
    public int stopId;
    public String stopName;
    public double lat;
    public double lon;
    public boolean toStop;
}
