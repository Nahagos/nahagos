package com.nahagos.nahagos;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
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

import com.nahagos.nahagos.databinding.LineViewStopElementBinding;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class LineViewArrayAdapter extends ArrayAdapter<Pair<StopTime, Boolean>> {
    private final Context context;
    private final boolean isDriver;
    private final int enableStopButton;

    public LineViewArrayAdapter(@NonNull Context c, int resource, @NonNull ArrayList<Pair<StopTime, Boolean>> stops, boolean isDriver, int stopId, String trip_id) {
        super(c, R.layout.line_view_stop_element, stops);
        this.context = c;
        this.isDriver = isDriver;
        this.enableStopButton = stopId;
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

        if (isDriver || (!isDriver && current.first.stop_id != enableStopButton)) {
            stopButton.setVisibility(View.INVISIBLE);
            handImg.setVisibility(current.second ? View.VISIBLE : View.INVISIBLE);
        }

        stopButton.setOnClickListener((v) -> {
           try {
               if (((LineView) context).onButtonClicked(current.first.stop_id)) {
                   stopButton.setVisibility(View.INVISIBLE);
                   handImg.setVisibility(View.VISIBLE);
               }
           } catch(RuntimeException ignored) {
               Log.d("Array adapter Error", Objects.requireNonNull(ignored.getMessage()));
           }
        });

        String stopNameId = current.first.stop_name + " | " + current.first.stop_id;
        stopName.setText(stopNameId);
        stopTime.setText(current.first.time);
        return convertView;
    }
}