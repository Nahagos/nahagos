package com.nahagos.nahagos.activities;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.adapters.LineViewArrayAdapter;
import com.nahagos.nahagos.server.ServerAPI;
import com.nahagos.nahagos.datatypes.StopTime;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class
LineView extends AppCompatActivity {

    private final ArrayList<Pair<StopTime, Boolean>> stops = new ArrayList<>();

    private int myStop = -1;

    private boolean isDriver = false;
    private boolean canStartDrive = false;

    private LineViewArrayAdapter stationsAdapter;

    private Thread serverListeningThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_view);

        ListView stopsList = findViewById(R.id.stops_list);
        Button startDriveBtn = findViewById(R.id.start_drive_btn);
        Button backBtn = findViewById(R.id.back_btn);
        ConstraintLayout layout = findViewById(R.id.line_view_layout);
        TextView title = findViewById(R.id.line_title);
        ImageView nahagosImg = findViewById(R.id.nahagos_img);

        Intent intent = getIntent();

        isDriver = intent.getBooleanExtra("isDriver", false);
        canStartDrive = intent.getBooleanExtra("canStartDrive", false);
        boolean nahagosOnline = intent.getBooleanExtra("nahagosOnline", false);
        String lineColor = intent.getStringExtra("lineColor");
        String lineName = intent.getStringExtra("lineName");
        myStop = intent.getIntExtra("stopId", 0);
        String trip_id = intent.getStringExtra("tripId");

        backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        if (lineColor != null && !lineColor.isEmpty())
            layout.setBackgroundColor(Color.parseColor(lineColor));

        if (lineName == null || lineName.isEmpty())
            lineName = getString(R.string.line_not_found);
        title.setText(lineName);

        if (isDriver && canStartDrive) {
            startDriveBtn.setVisibility(View.VISIBLE);
        }

        if (!isDriver) {
            nahagosImg.setVisibility(nahagosOnline ? View.VISIBLE : View.INVISIBLE);
        }

        stationsAdapter = new LineViewArrayAdapter(this, stops, isDriver, myStop, trip_id);
        stopsList.setAdapter(stationsAdapter);

        if (trip_id != null) {
            if (!trip_id.isEmpty())
                new Thread(()->{
                    StopTime[] stopsFromServer = ServerAPI.getStopsByLine(trip_id);
                    if (stopsFromServer != null) {
                        stops.addAll(Arrays.stream(stopsFromServer).map(
                                (st) -> new Pair<>(st, false)).collect(Collectors.toList())
                        );
                        runOnUiThread(() -> stationsAdapter.notifyDataSetChanged());
                    }
                }).start();
        }

        startDriveBtn.setOnClickListener((v) -> {
            if (isDriver && canStartDrive) {
                new Thread(()-> {
                    if (ServerAPI.registerForLine(trip_id)) {
                        runOnUiThread(() -> {
                            nahagosImg.setVisibility(View.VISIBLE);
                            startDriveBtn.setVisibility(View.INVISIBLE);
                            startListeningForStoppingUpdates();
                        });
                    }
                    else {
                        runOnUiThread(() -> Toast.makeText(this, R.string.cant_start_drive, Toast.LENGTH_SHORT).show());
                    }
                }).start();

            } else {
                // if the user is not the driver, or he can't start a drive, the button shouldn't be visible.
                // of course, the same thing is true if the driver can start a drive, and has started it.
                // anyway - don't show the button.
                startDriveBtn.setVisibility(View.INVISIBLE);
            }

        });
    }

    private void startListeningForStoppingUpdates() {
        serverListeningThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    ArrayList<Integer> toStopStations = Arrays.stream(ServerAPI.getStoppingStations(0, 0)).boxed().collect(Collectors.toCollection(ArrayList::new));
                    for (AtomicInteger i = new AtomicInteger(0); i.get() < stops.size(); i.set(i.get()+1)) {
                        if (toStopStations.stream().anyMatch((j) -> j == stops.get(i.get()).first.stop_id)) {
                            Pair<StopTime, Boolean> newN = new Pair<>(stops.get(i.get()).first, true);
                            stops.set(i.get(), newN);  // Update original list
                        }
                    }

                    runOnUiThread(() -> stationsAdapter.notifyDataSetChanged());

                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                Log.e("LineView", "StartListeningForStoppingUpdates error", e);
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
    protected void onResume() {
        super.onResume();
        if (serverListeningThread != null) {
            serverListeningThread.start();
        }
    }

    protected void endTrip() {
        new Thread(() -> {
            if (ServerAPI.endTrip()) {
                Toast.makeText(this, R.string.endedTrip, Toast.LENGTH_SHORT).show();
                Log.d("OH", "IT WORKED");
            }
            else {
                Log.d("OH NO", "IT DIDNT WORK");
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverListeningThread != null) {
            serverListeningThread.interrupt();
        }
        endTrip();
    }
}
