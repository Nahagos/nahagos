package com.nahagos.nahagos.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

public class Tables {
    @Entity
    public static class Stops {
        @PrimaryKey
        @ColumnInfo(name = "stop_id")
        public int stopId;

        @ColumnInfo(name = "stop_code")
        public int stopCode;

        @ColumnInfo(name = "stop_name")
        @NotNull
        public String stopName;

        @ColumnInfo(name = "stop_desc")
        @NotNull
        public String stopDesc;

        @ColumnInfo(name = "stop_lat")
        public double stopLat;

        @ColumnInfo(name = "stop_lon")
        public double stopLon;

        @ColumnInfo(name = "location_type")
        public boolean is_central;

        @ColumnInfo(name = "parent_station")
        public Integer parentStation;

        @ColumnInfo(name = "zone_id")
        public int zoneId;

        public Stops() {
            stopName = "";
            stopDesc = "";
        }
    }
}

