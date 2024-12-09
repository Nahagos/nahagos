package com.nahagos.nahagos.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

public class Tables {

    /**
     * stop_id, stop_code, stop_name, stop_desc, stop_lat, stop_lon, location_type, parent_station, zone_id
     */
    @Entity
    public static class Stops{
        @ColumnInfo(name = "stop_id")
        public int stopId;

        @ColumnInfo(name = "stop_code")
        public String stopCode;

        @ColumnInfo(name = "stop_name")
        public String stopName;

        @ColumnInfo(name = "stop_desc")
        public String stopDesc;

        @ColumnInfo(name = "stop_lat")
        public double stopLat;

        @ColumnInfo(name = "stop_lon")
        public double stopLon;

        @ColumnInfo(name = "location_type")
        public int locationType;

        @ColumnInfo(name = "parent_station")
        public String parentStation;

        @ColumnInfo(name = "zone_id")
        public String zoneId;
    }
}
