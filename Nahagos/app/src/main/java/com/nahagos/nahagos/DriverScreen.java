package com.nahagos.nahagos;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class DriverScreen extends AppCompatActivity {
    private List<Line>[] schedule = null;

    private RecyclerView drivesView;
    private ArrayAdapter<String> spinnerAdapter;
    private DrivesAdapter drivesAdapter;
    private final List<Line> toBeDisplayed = new ArrayList<>();

    private final List<String> daysOfWeek;

    public DriverScreen() {
        super();
        daysOfWeek = Arrays.asList("All", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_screen);

        Spinner spinner = findViewById(R.id.spinner_days);
        drivesView = findViewById(R.id.recycler_view_drives);
        drivesView.setLayoutManager(new LinearLayoutManager(this));


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysOfWeek);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        drivesAdapter = new DrivesAdapter(toBeDisplayed, this);
        drivesView.setAdapter(drivesAdapter);

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            schedule = get_schedule_from_server();
            runOnUiThread(() -> {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        toBeDisplayed.clear();
                        toBeDisplayed.addAll(position == 0 ? Arrays.stream(schedule).flatMap(List::stream).collect(Collectors.toList()) : schedule[position - 1]);
                        drivesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                spinner.setSelection(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
            });
        }).start();
    }

    private List<Line>[] get_schedule_from_server() {
        return new List[]{List.of(new Line("1", "101", "Jerusalem to Tel Aviv", "08:00", "08:30"), new Line("1", "103", "modi'in to Kfar Saba", "08:00", "08:30")), List.of(new Line("2", "202", "Tel Aviv Center to Jerusalem", "09:00", "08:30"), new Line("2", "202", "Jerusalem to Petah Tikva", "09:00", "08:30"), new Line("2", "202", "sderot to Tal Shahar", "09:00", "08:30")), List.of(new Line("3", "303", "Dimona to Beer Sheva", "10:00", "08:30"), new Line("2", "202", "Tal Shahar to modi'in", "09:00", "08:30")), List.of(new Line("4", "404", "Petah Tikva to Kfar Saba", "11:00", "08:30"), new Line("2", "202", "Petah Tikva to Beer Sheva", "09:00", "08:30")), List.of(new Line("5", "505", "modi'in to Dimona", "12:00", "08:30")), List.of(new Line("6", "606", "Jerusalem to Beer Sheva", "13:00", "08:30"), new Line("4", "404", "Hoshaia to Tal Shahar", "11:00", "08:30"), new Line("4", "404", "Tal Shahar to Sderot", "11:00", "08:30")), List.of(new Line("7", "707", "Kfar Saba to Petah Tikva", "14:00", "08:30"))};
    }

    public static class Line {
        public String tripId, lineNum, lineDescription, departureTime, arrivalTime;

        public Line(String tripId, String lineNum, String lineDescription, String departureTime, String arrivalTime) {
            this.tripId = tripId;
            this.lineNum = lineNum;
            this.lineDescription = lineDescription;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }

        @NonNull
        @Override
        public String toString() {
            return lineNum + " - " + lineDescription + " - " + departureTime + " - " + arrivalTime;
        }
    }
}
