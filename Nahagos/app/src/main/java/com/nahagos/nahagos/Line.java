package com.nahagos.nahagos;

public class Line {
    public String tripId;       // Identifier for the trip
    public String name;         // Name of the line
    public int lineNum;         // Line number
    public String departure;    // Departure time
    public String operator;     // Operator of the line
    public boolean isNahagos;   // Whether it is Nahagos line or not

    // Constructor
    public Line(String tripId, String name, int lineNum, String departure, String operator, boolean isNahagos) {
        this.tripId = tripId;
        this.name = name;
        this.lineNum = lineNum;
        this.departure = departure;
        this.operator = operator;
        this.isNahagos = isNahagos;
    }


    // toString method
    @Override
    public String toString() {
        return "Line{" +
                "tripId='" + tripId + '\'' +
                ", name='" + name + '\'' +
                ", lineNum=" + lineNum +
                ", departure='" + departure + '\'' +
                ", operator='" + operator + '\'' +
                ", isNahagos=" + isNahagos +
                '}';
    }
}
