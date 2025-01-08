package com.nahagos.nahagos;

import android.os.Bundle;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;


public class DriverScreen extends AppCompatActivity {
    private HashMap<String, List<line_info>> schedule = new HashMap<>();

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_screen);

        List<String> daysOfWeek = Arrays.asList("all week", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        Spinner spinner = findViewById(R.id.spinner_days);
        recyclerView = findViewById(R.id.recycler_view_drives);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysOfWeek);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        schedule = get_schedule_from_server();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDay = daysOfWeek.get(position);
                if(selectedDay.equals("all week"))
                {
                    List<line_info> allDrives = new ArrayList<>();
                    for (List<line_info> drives : schedule.values()) {
                        allDrives.addAll(drives);
                    }

                    if (allDrives != null && !allDrives.isEmpty()) {
                        for (line_info drive : allDrives) {
                            DrivesAdapter adapter = new DrivesAdapter(allDrives, DriverScreen.this);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                    else
                    {
                        Toast.makeText(DriverScreen.this, "ss", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    if (!get_schedule_by_day(selectedDay))
                    {
                        Toast.makeText(DriverScreen.this, "!get_schedule_by_day(selectedDay)", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private HashMap<String, List<line_info>> get_schedule_from_server()
    {
        HashMap<String, List<line_info>> schedule = new HashMap<>();

        schedule.put("Sunday", List.of(new line_info("1", "101", "Jerusalem to Tel Aviv", "08:00", "08:30"), new line_info("1", "103", "modi'in to Kfar Saba", "08:00", "08:30")));
        schedule.put("Monday", List.of(new line_info("2", "202", "Tel Aviv Center to Jerusalem", "09:00", "08:30"), new line_info("2", "202", "Jerusalem to Petah Tikva", "09:00", "08:30"), new line_info("2", "202", "sderot to Tal Shahar", "09:00", "08:30")));
        schedule.put("Tuesday", List.of(new line_info("3", "303", "Dimona to Beer Sheva", "10:00", "08:30"), new line_info("2", "202", "Tal Shahar to modi'in", "09:00", "08:30")));
        schedule.put("Wednesday", List.of(new line_info("4", "404", "Petah Tikva to Kfar Saba", "11:00", "08:30"), new line_info("2", "202", "Petah Tikva to Beer Sheva", "09:00", "08:30")));
        schedule.put("Thursday", List.of(new line_info("5", "505", "modi'in to Dimona", "12:00", "08:30")));
        schedule.put("Friday", List.of(new line_info("6", "606", "Jerusalem to Beer Sheva", "13:00", "08:30"), new line_info("4", "404", "Hoshaia to Tal Shahar", "11:00", "08:30"), new line_info("4", "404", "Tal Shahar to Sderot", "11:00", "08:30")));
        schedule.put("Saturday", List.of(new line_info("7", "707", "Kfar Saba to Petah Tikva", "14:00", "08:30")));

        return schedule;
    }
    private Boolean get_schedule_by_day(String day) {
        List<line_info> drives = schedule.get(day);

        if (drives != null && !drives.isEmpty()) {
            for (line_info drive : drives) {
                DrivesAdapter adapter = new DrivesAdapter(drives, DriverScreen.this);
                recyclerView.setAdapter(adapter);
            }
            return Boolean.TRUE;
        } else {
            Toast.makeText(this, "No drives for " + day, Toast.LENGTH_SHORT).show();
            return Boolean.FALSE;
        }
    }

}
