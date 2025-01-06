package com.nahagos.nahagos.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

public class Tables {
    @Entity(tableName = "stops")
    public static class Stop {
        @PrimaryKey
        @ColumnInfo(name = "stop_id")
        public int id;

        @ColumnInfo(name = "stop_code")
        public int code;

        @ColumnInfo(name = "stop_name")
        @NotNull
        public String name;

        @ColumnInfo(name = "stop_desc")
        @NotNull
        public String description;

        @ColumnInfo(name = "stop_lat")
        public double lat;

        @ColumnInfo(name = "stop_lon")
        public double lon;

        @ColumnInfo(name = "location_type")
        public boolean isCentral;

        @ColumnInfo(name = "parent_station")
        public Integer parentStation;

        @ColumnInfo(name = "zone_id")
        public int zoneId;

        public Stop() {
            name = "";
            description = "";
        }
    }
}

