package com.nahagos.nahagos;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;


import androidx.annotation.NonNull;

import java.util.ArrayList;

public class LineViewArrayAdapter extends ArrayAdapter<StopTime> {
    private final Context context;
    private final boolean isDriver;
    private final int enableStopButton;

    public LineViewArrayAdapter(@NonNull Context c, int resource, @NonNull ArrayList<StopTime> stops, boolean isDriver, int stopId) {
        super(c, R.layout.line_view_stop_element, stops);
        context = c;
        this.isDriver = isDriver;
        enableStopButton = stopId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.line_view_stop_element, parent, false);

        TextView stopName = convertView.findViewById(R.id.stop_name);
        TextView stopTime = convertView.findViewById(R.id.stop_time);
        Button stopButton = convertView.findViewById(R.id.stop_btn);

        StopTime current = getItem(position);
        if (current == null)
            return convertView;


        stopButton.setEnabled(!isDriver && !current.toStop && current.stopId == enableStopButton);
        //stopButton.setBackgroundColor(Color.parseColor(current.toStop ? "#FFFFFF" : "#000000"));

        String stopNameId = current.stopName + " | " + current.stopId;
        stopName.setText(stopNameId);
        stopTime.setText(current.time);
        return convertView;
    }
}
