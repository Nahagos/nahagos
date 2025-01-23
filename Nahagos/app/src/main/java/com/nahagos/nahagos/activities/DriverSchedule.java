package com.nahagos.nahagos.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nahagos.nahagos.adapters.DrivesAdapter;
import com.nahagos.nahagos.R;
import com.nahagos.nahagos.datatypes.Line;
import com.nahagos.nahagos.server.ServerAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class DriverSchedule extends AppCompatActivity {
    private List<List<Line>> schedule;
    private DrivesAdapter drivesAdapter;
    private final List<Line> toBeDisplayed = new ArrayList<>();
    private final List<String> daysOfWeek;

    public DriverSchedule() {
        super();
        daysOfWeek = Arrays.asList("All", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_screen);

        Spinner spinner = findViewById(R.id.spinner_days);
        RecyclerView drivesView = findViewById(R.id.recycler_view_drives);
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
                        toBeDisplayed.addAll(position == 0 ? schedule.stream().flatMap(List::stream).collect(Collectors.toList()) : schedule.get(position - 1));
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

    private List<List<Line>> get_schedule_from_server() {
        Line[][] rawSchedule = ServerAPI.getDriverSchedule(); // Assuming this returns Line[][]
        return Arrays.stream(rawSchedule) // Stream the outer array
                .map(Arrays::asList)     // Convert each inner array (Line[]) to a List<Line>
                .collect(Collectors.toList()); // Collect into a List<List<Line>>
    }



}
