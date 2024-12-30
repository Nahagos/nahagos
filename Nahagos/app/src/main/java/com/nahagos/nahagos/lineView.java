package com.nahagos.nahagos;

//import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;


import java.util.ArrayList;

public class lineView extends AppCompatActivity {

    private ArrayList<StopTime> stops = new ArrayList<>();

    private ListView stopsList;

    private final int lineId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_line_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        stopsList = findViewById(R.id.stops_list);
        stops.clear();
        //stops.addAll(ServerAPI.get_stops_by_line(getIntent().getIntExtra("trip_id", 0)));
        stops.add(new StopTime("16:19", 1234, "חיפה", false));
        stops.add(new StopTime("17:14", 1255, "הושעיה", true));
        stops.add(new StopTime("18:11", 1289, "מקום שהוא לא הושעיה", false));
        stops.add(new StopTime("20:13", 5324, "טבריה", false));


        try {
            LineViewArrayAdapter stationsAdapter = new LineViewArrayAdapter(getBaseContext(), R.layout.line_view_stop_element, (StopTime[]) stops.toArray());
            stopsList.setAdapter(stationsAdapter);
        }
        catch (Exception e) {
            if (e.getMessage() != null)
                Log.d("", e.getMessage());
        }

    }
}