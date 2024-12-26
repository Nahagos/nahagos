package com.nahagos.nahagos;

public class Line {
    private String tripId;       // Identifier for the trip
    private String name;         // Name of the line
    private int lineNum;         // Line number
    private String departure;    // Departure time
    private String operator;     // Operator of the line
    private boolean isNahagos;   // Whether it is Nahagos line or not

    // Constructor
    public Line(String tripId, String name, int lineNum, String departure, String operator, boolean isNahagos) {
        this.tripId = tripId;
        this.name = name;
        this.lineNum = lineNum;
        this.departure = departure;
        this.operator = operator;
        this.isNahagos = isNahagos;
    }

    // Getters and setters
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean isNahagos() {
        return isNahagos;
    }

    public void setNahagos(boolean isNahagos) {
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
