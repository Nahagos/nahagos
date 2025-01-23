package com.nahagos.nahagos.adapters;

import android.content.Context;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.activities.LineView;
import com.nahagos.nahagos.datatypes.Line;
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
    private final LineView context;
    private final boolean isDriver;
    private final int myStop;
    private boolean hasRequestedToStop;
    private final String tripId;
    private final boolean nahagosOnline;

    public LineViewArrayAdapter(@NonNull LineView c, @NonNull ArrayList<Pair<StopTime, Boolean>> stops, boolean isDriver, int stopId, String tripId, boolean nahagos_online) {
        super(c, R.layout.line_view_stop_element, stops);
        this.context = c;
        this.isDriver = isDriver;
        this.myStop = stopId;
        this.tripId = tripId;
        this.nahagosOnline = nahagos_online;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.line_view_stop_element, parent, false);

        TextView stopName = convertView.findViewById(R.id.stop_name);
        TextView stopTime = convertView.findViewById(R.id.stop_time);
        TextView stopNum = convertView.findViewById(R.id.stop_num);
        Button stopButton = convertView.findViewById(R.id.stop_btn);
        ImageView handImg = convertView.findViewById(R.id.stopping_img);

        Pair<StopTime, Boolean> current = getItem(position);
        if (current == null)
            return convertView;

        if (isDriver) {
            stopButton.setVisibility(View.INVISIBLE);
            handImg.setVisibility(current.second ? View.VISIBLE : View.INVISIBLE);
        } else if (myStop != current.first.stop_id || !nahagosOnline) {
            stopButton.setVisibility(View.INVISIBLE);
            handImg.setVisibility(View.INVISIBLE);
        } else if (hasRequestedToStop) {
            stopButton.setVisibility(View.INVISIBLE);
            handImg.setVisibility(View.VISIBLE);
        }
        else {
            stopButton.setVisibility(View.VISIBLE);
            handImg.setVisibility(View.INVISIBLE);
        }

        stopButton.setOnClickListener((v) -> {
           try {
               new Thread(() -> {
                   if (ServerAPI.waitForMe(tripId, current.first.stop_id)) {
                       hasRequestedToStop = true;
                       context.runOnUiThread(() -> {
                           stopButton.setVisibility(View.INVISIBLE);
                           handImg.setVisibility(View.VISIBLE);
                       });
                   }
               }).start();
           } catch(RuntimeException e) {
               Log.d("Array adapter Error", Objects.requireNonNull(e.getMessage()));
           }
        });

        String stopNameStr = current.first.stop_name;
        String stopIdStr = "" + current.first.stop_id;
        stopName.setText(stopNameStr);
        stopTime.setText(current.first.time);
        stopNum.setText(stopIdStr);
        return convertView;
    }
}
