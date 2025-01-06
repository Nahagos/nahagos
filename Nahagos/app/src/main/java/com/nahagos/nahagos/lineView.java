package com.nahagos.nahagos;

import android.app.MediaRouteButton;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class lineView extends AppCompatActivity {

    private final ArrayList<Pair<StopTime, Boolean>> stops = new ArrayList<>();

    private final int lineId = 0;
    private int myStop = -1;

    private boolean isDriver = false;
    private boolean canStartDrive = false;
    private boolean driveStarted = false;

    private LineViewArrayAdapter stationsAdapter;

    private Handler mainHandler;
    private Thread serverListeningThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_line_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.line_view_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

        ListView stopsList = findViewById(R.id.stops_list);
        Button startDriveBtn = findViewById(R.id.start_drive_btn);
        Button backBtn = findViewById(R.id.back_btn);
        ConstraintLayout layout = findViewById(R.id.line_view_layout);
        TextView title = findViewById(R.id.line_title);
        ImageView nahagosImg = findViewById(R.id.nahagos_img);

        Intent intent = getIntent();
        ServerAPI sapi = new ServerAPI(this);

        backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        String lineColor = intent.getStringExtra("lineColor");
        if (lineColor != null && !lineColor.isEmpty())
            layout.setBackgroundColor(Color.parseColor(lineColor));

        String lineName = intent.getStringExtra("lineName");
        if (lineName == null || lineName.isEmpty())
            lineName = "Error! line name not found";
        title.setText(lineName);

        mainHandler = new Handler(Looper.getMainLooper());

        isDriver = intent.getBooleanExtra("isDriver", false);
        if (isDriver) {
            canStartDrive = intent.getBooleanExtra("canStartDrive", true);
            if (canStartDrive) {
                startDriveBtn.setVisibility(View.VISIBLE);
            }
        }
        else {
            myStop = intent.getIntExtra("stopId", 37352);
            nahagosImg.setVisibility(intent.getBooleanExtra("nahagosOnline", false) ? View.VISIBLE : View.INVISIBLE);
        }

        stops.clear();

        String trip_id = intent.getStringExtra("trip_id");
        if (trip_id == null || trip_id.isEmpty())
            trip_id = "1_238970";
        stops.addAll(Arrays.stream(sapi.get_stops_by_line(trip_id)).map((st) -> {
            return new Pair<StopTime, Boolean>(st, false);
        }).collect(Collectors.toList()));

        stationsAdapter = new LineViewArrayAdapter(getBaseContext(), R.layout.line_view_stop_element, stops, isDriver, myStop);
        stopsList.setAdapter(stationsAdapter);

        String finalTrip_id = trip_id;
        startDriveBtn.setOnClickListener((v) -> {
            boolean worked = true;
            if (isDriver && canStartDrive) {
                driveStarted = true;
                worked = sapi.register_for_line(finalTrip_id);
                listenForStoppingUpdates(intent.getStringExtra("trip_id"), sapi);

                if (worked)
                    nahagosImg.setVisibility(View.VISIBLE);
            }
            // if the user is not the driver, or he can't start a drive, the button shouldn't be visible.
            // of course, the same thing is true if the driver can start a drive, and has started it.
            // anyway - don't show the button.
            if (!worked)
                startDriveBtn.setVisibility(View.INVISIBLE);
        });
    }

    private void listenForStoppingUpdates(String trip_id, ServerAPI sapi) {
        if (trip_id == null || trip_id.isEmpty()) {
            trip_id = "1_238970";
        }
        String finalTrip_id = trip_id;

        serverListeningThread = new Thread(() -> {
            try {
//                boolean t = true;
                while (!Thread.interrupted()) {
                    stops.clear();
                    stops.addAll(Arrays.stream(sapi.get_stops_by_line(finalTrip_id)).map((st) -> {
                        return new Pair<StopTime, Boolean>(st, false);
                    }).collect(Collectors.toList()));

//                    ArrayList<StopTime> tmpstops = new ArrayList<>();
//                    tmpstops.add(new StopTime("16:19", 1234, "חיפה", t));
//                    tmpstops.add(new StopTime("17:14", 1255, "מקום בארץ", !t));
//                    tmpstops.add(new StopTime("18:11", 1289, "מקום שהוא עדיין בארץ", t));
//                    tmpstops.add(new StopTime("20:13", 5324, "טבריה", t));
//                    stops.addAll(tmpstops);

                    mainHandler.post(() -> stationsAdapter.notifyDataSetChanged());

                    //t = !t;

                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                if (e.getMessage() != null)
                    Log.d("Server Exception", e.getMessage());
            }
        });
        serverListeningThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (serverListeningThread != null) {
            serverListeningThread.interrupt();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverListeningThread != null) {
            serverListeningThread.interrupt();
        }
    }
}