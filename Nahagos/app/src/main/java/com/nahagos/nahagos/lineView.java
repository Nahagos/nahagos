package com.nahagos.nahagos;

//import android.content.Intent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
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

public class lineView extends AppCompatActivity {

    private final ArrayList<StopTime> stops = new ArrayList<>();

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

        ListView stopsList = findViewById(R.id.stops_list);
        Button startDriveBtn = findViewById(R.id.start_drive_btn);
        ConstraintLayout layout = findViewById(R.id.line_view_layout);
        TextView title = findViewById(R.id.line_title);
        Intent intent = getIntent();

        layout.setBackgroundColor(Color.parseColor(intent.getStringExtra("lineColor")));
        title.setText(intent.getStringExtra("lineName"));

        mainHandler = new Handler(Looper.getMainLooper());

        isDriver = intent.getBooleanExtra("isDriver", false);
        if (isDriver) {
            canStartDrive = intent.getBooleanExtra("canStartDrive", true);
            if (canStartDrive) {
                startDriveBtn.setVisibility(View.VISIBLE);
            }
        }
        else {
            myStop = intent.getIntExtra("stopId", 1234);
        }
        stops.clear();

        ArrayList<StopTime> tmpstops = new ArrayList<>();
        tmpstops.add(new StopTime("16:19", 1234, "חיפה", false));
        tmpstops.add(new StopTime("17:14", 1255, "מקום בארץ", true));
        tmpstops.add(new StopTime("18:11", 1289, "מקום שהוא עדיין בארץ", false));
        tmpstops.add(new StopTime("20:13", 5324, "טבריה", false));

        stops.addAll(tmpstops);
        //stops.addAll(ServerAPI.get_stops_by_line(intent.getIntExtra("trip_id", 0)));
        stationsAdapter = new LineViewArrayAdapter(getBaseContext(), R.layout.line_view_stop_element, stops, isDriver, myStop);
        stopsList.setAdapter(stationsAdapter);

        startDriveBtn.setOnClickListener((v) -> {
            if (isDriver && canStartDrive) {
                driveStarted = true;
                // TODO: send server that this driver is the Nahagos of this drive.
                listenForStoppingUpdates();
            }
            // if the user is not the driver, or he can't start a drive, the button shouldn't be visible.
            // of course, the same thing is true if the driver can start a drive, and has started it.
            startDriveBtn.setVisibility(View.INVISIBLE);
        });
    }

    private void listenForStoppingUpdates() {
        serverListeningThread = new Thread(() -> {
            try {
                boolean t = true;
                while (!Thread.interrupted()) {
                    stops.clear();
                    //stops.addAll(ServerAPI.get_stops_by_line(intent.getIntExtra("trip_id", 0)));
                    ArrayList<StopTime> tmpstops = new ArrayList<>();
                    tmpstops.add(new StopTime("16:19", 1234, "חיפה", t));
                    tmpstops.add(new StopTime("17:14", 1255, "מקום בארץ", !t));
                    tmpstops.add(new StopTime("18:11", 1289, "מקום שהוא עדיין בארץ", t));
                    tmpstops.add(new StopTime("20:13", 5324, "טבריה", t));
                    stops.addAll(tmpstops);

                    mainHandler.post(() -> stationsAdapter.notifyDataSetChanged());

                    t = !t;

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