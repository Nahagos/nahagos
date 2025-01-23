package com.nahagos.nahagos.datatypes;

import com.google.gson.Gson;

public class Line {
    public String tripId;
    public String name;
    public String num;
    public String departure;
    public String operator;
    public boolean isNahagos;
    public boolean isLive;

    public Line(String tripId, String name, String num, String departure, String operator, boolean isNahagos, boolean isLive) {
        this.tripId = tripId;
        this.name = name;
        this.num = num;
        this.departure = departure;
        this.operator = operator;
        this.isNahagos = isNahagos;
        this.isLive = isLive;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
