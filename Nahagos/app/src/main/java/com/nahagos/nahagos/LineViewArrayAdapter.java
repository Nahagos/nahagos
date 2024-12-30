package com.nahagos.nahagos;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.NonNull;

public class LineViewArrayAdapter extends ArrayAdapter<StopTime> {
    private final Context context;
    public LineViewArrayAdapter(@NonNull Context context, int resource, @NonNull StopTime[] stops) {
        super(context, R.layout.line_view_stop_element, stops);
        this.context = context;
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

        stopButton.setEnabled(!current.toStop);
        String stopNameId = current.stopName + " | " + current.stopId;
        stopName.setText(stopNameId);
        stopTime.setText(current.time);
        return convertView;
    }
}
