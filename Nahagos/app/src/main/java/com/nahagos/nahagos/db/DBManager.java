package com.nahagos.nahagos.db;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.nahagos.nahagos.db.Tables.Stop;

import java.util.List;

public class DBManager {
    private final AppDatabase db;

    public DBManager(Context context) {
        db = Room.databaseBuilder(context,
                        AppDatabase.class, "gtfs")
                .createFromAsset("gtfs.db")
                .allowMainThreadQueries()
                .build();
    }

    public StopsDao stopsDao() {
        return db.stopsDao();
    }


    @Dao
    public static abstract class StopsDao {
        @Query("SELECT * FROM stops")
        public abstract List<Stop> getAll();

        @Query("SELECT * FROM stops WHERE stop_id IN (:stopIds)")
        public abstract List<Stop> searchByIds(int[] stopIds);

        @Query("SELECT * FROM stops WHERE stop_lat BETWEEN :lat1 AND :lat2 AND stop_lon BETWEEN :lon1 AND :lon2")
        public abstract List<Stop> searchByLatLongRange_sql(double lat1, double lat2, double lon1, double lon2);

        public List<Stop> searchByLatLongRange(double lat1, double lat2, double lon1, double lon2) {
            return searchByLatLongRange_sql(Math.min(lat1, lat2), Math.max(lat1, lat2), Math.min(lon1, lon2), Math.max(lon1, lon2));
        }

        @Query("SELECT * FROM stops WHERE stop_name LIKE '%' || :name || '%' LIMIT :limit")
        public abstract List<Stop> searchByName(String name, int limit);

        public List<Stop> searchByName(String name) {
            return searchByName(name, 10);
        }

        @Insert
        public abstract void insertAll(Stop... stops);

        @Delete
        public abstract void delete(Stop stop);
    }

    @Database(entities = {Stop.class}, version = 1)
    public abstract static class AppDatabase extends RoomDatabase {
        public abstract StopsDao stopsDao();
    }
}
