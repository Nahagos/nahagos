package com.nahagos.nahagos.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Tables {
    @Entity(tableName = "stops")
    public static class Stop implements ClusterItem, Serializable {
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

        @Ignore
        public Stop() {
            name = "";
            description = "";
        }

        @Ignore
        public Stop(int id, @NonNull String name){
            this();
            this.id = id;
            this.name = name;
        }

        public Stop(int id, @NonNull String name, @NonNull String description, double lat, double lon, boolean isCentral, Integer parentStation, int zoneId) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.lat = lat;
            this.lon = lon;
            this.isCentral = isCentral;
            this.parentStation = parentStation;
            this.zoneId = zoneId;
        }

        @NonNull
        public LatLng getPosition() {
            return new LatLng(lat, lon);
        }


        @NonNull
        @Override
        public String getTitle() {
            return name;
        }

        @Nullable
        @Override
        public String getSnippet() {
            return description;
        }

        @NonNull
        @Override
        public String toString() {
            return name + " (" + id + ")";
        }

    }
}

