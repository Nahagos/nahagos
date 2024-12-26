package com.nahagos.nahagos.db;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.List;

public class DBManager {
    private static DBManager instance;
    private final AppDatabase db;

    private DBManager(android.content.Context context) {
        db = Room.databaseBuilder(context,
                        AppDatabase.class, "gtfs")
                .createFromAsset("gtfs.db")
                .build();
    }

    public static DBManager getInstance(android.content.Context context) {
        if (instance == null) {
            instance = new DBManager(context);
        }
        return instance;
    }

    public StopsDao stopsDao() {
        return db.stopsDao();
    }


    @Dao
    public interface StopsDao {
        @Query("SELECT * FROM stops")
        List<Tables.Stop> getAll();

        @Query("SELECT * FROM stops WHERE stop_id IN (:stopIds)")
        List<Tables.Stop> loadAllByIds(int[] stopIds);

        @Query("SELECT * FROM stops WHERE stop_name LIKE :stopName LIMIT 1")
        Tables.Stop findByName(String stopName);

        @Query("SELECT * FROM stops WHERE stop_lat BETWEEN :lat1 AND :lat2 AND stop_lon BETWEEN :lon1 AND :lon2")
        List<Tables.Stop> getStationsByLatLongRange(double lat1, double lat2, double lon1, double lon2);

        @Insert
        void insertAll(Tables.Stop... stops);

        @Delete
        void delete(Tables.Stop stop);
    }

    @Database(entities = {Tables.Stop.class}, version = 1)
    public abstract static class AppDatabase extends RoomDatabase {
        public abstract StopsDao stopsDao();
    }
}
