package com.nahagos.nahagos;

public class line_info {
    String tripId;
    String lineNum;
    String lineDescription;
    String departureTime;
    String arrivalTime;


    public line_info(String tripId, String lineNum, String lineDescription, String departureTime, String arrivalTime) {
        this.tripId = tripId;
        this.lineNum = lineNum;
        this.lineDescription = lineDescription;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    // Getters
    public String getTripId() {
        return tripId;
    }

    public String getLineNum() {
        return lineNum;
    }

    public String getLineDescription() {
        return lineDescription;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    // Functions
    @Override
    public String toString() {
        return "Line: " + lineNum + " - " + lineDescription + "\n" +
                "Departure: " + departureTime + ", Arrival: " + arrivalTime;
    }

    //get functions:

}
