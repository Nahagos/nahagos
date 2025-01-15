package com.nahagos.nahagos.linechoose;

public class LineData{
    public String trip_id;       // Identifier for the trip
    public String name;         // Name of the line
    public int line_num;         // Line number
    public String departure;    // Departure time
    public String operator;     // Operator of the line
    public boolean isNahagos;   // Whether it is Nahagos line or not

    // Constructor
    public LineData(String trip_id, String name, int line_num, String departure, String operator, boolean isNahagos) {
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