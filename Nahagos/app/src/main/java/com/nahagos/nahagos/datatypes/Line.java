package com.nahagos.nahagos.datatypes;

public class Line {
    public String trip_id;
    public String name;
    public String line_num;
    public String departure;
    public String operator;
    public boolean isNahagos;

    // Constructor
    public Line(String trip_id, String name, String line_num, String departure, String operator, boolean isNahagos) {
        this.trip_id = trip_id;
        this.name = name;
        this.line_num = line_num;
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
                ", line_num=" + line_num +
                ", departure='" + departure + '\'' +
                ", operator='" + operator + '\'' +
                ", isNahagos=" + isNahagos +
                '}';
    }
}
