package com.nahagos.nahagos.adapters;

import android.content.Context;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.datatypes.StopTime;
import com.nahagos.nahagos.server.ServerAPI;

import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.util.Pair;



import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Objects;

public class LineViewArrayAdapter extends ArrayAdapter<Pair<StopTime, Boolean>> {
    private final Context context;
    private final boolean isDriver;
    private final int myStop;
    private final String trip_id;

    public LineViewArrayAdapter(@NonNull Context c, @NonNull ArrayList<Pair<StopTime, Boolean>> stops, boolean isDriver, int stopId, String tripId) {
        super(c, R.layout.line_view_stop_element, stops);
        this.context = c;
        this.isDriver = isDriver;
        this.myStop = stopId;
        this.trip_id = tripId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.line_view_stop_element, parent, false);

        TextView stopName = convertView.findViewById(R.id.stop_name);
        TextView stopTime = convertView.findViewById(R.id.stop_time);
        Button stopButton = convertView.findViewById(R.id.stop_btn);
        ImageView handImg = convertView.findViewById(R.id.stopping_img);

        Pair<StopTime, Boolean> current = getItem(position);
        if (current == null)
            return convertView;

        if (isDriver || (!isDriver && current.first.stop_id != myStop)) {
            stopButton.setVisibility(View.INVISIBLE);
            handImg.setVisibility(current.second && isDriver ? View.VISIBLE : View.INVISIBLE);
        }

        stopButton.setOnClickListener((v) -> {
           try {
               if (ServerAPI.waitForMe(trip_id, current.first.stop_id)) {
                   stopButton.setVisibility(View.INVISIBLE);
                   handImg.setVisibility(View.VISIBLE);
               }
           } catch(RuntimeException e) {
               Log.d("Array adapter Error", Objects.requireNonNull(e.getMessage()));
           }
        });

        String stopNameId = current.first.stop_name + " | " + current.first.stop_id;
        stopName.setText(stopNameId);
        stopTime.setText(current.first.time);
        return convertView;
    }
}
