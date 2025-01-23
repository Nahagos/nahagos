package com.nahagos.nahagos.datatypes;

import com.google.gson.annotations.SerializedName;

public class Line {
    @SerializedName("tripId")
    public String trip_id;
    public String name;
    public String num;
    public String departure;
    public String operator;
    public boolean isNahagos;

    // Constructor
    public Line(String trip_id, String name, String line_num, String departure, String operator, boolean isNahagos) {
        this.trip_id = trip_id;
        this.name = name;
        this.num = line_num;
        this.departure = departure;
        this.operator = operator;
        this.isNahagos = isNahagos;
    }


    // toString method
    @Override
    public String toString() {
        return "Line{" +
                "trip_id='" + trip_id + '\'' +
                ", name='" + name + '\'' +
                ", num=" + num +
                ", departure='" + departure + '\'' +
                ", operator='" + operator + '\'' +
                ", isNahagos=" + isNahagos +
                '}';
    }
}
