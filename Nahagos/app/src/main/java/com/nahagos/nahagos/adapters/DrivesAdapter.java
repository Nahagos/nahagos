package com.nahagos.nahagos.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.activities.DriverSchedule.Line;

import java.util.List;

public class DrivesAdapter extends RecyclerView.Adapter<DrivesAdapter.DriveViewHolder> {
    private final List<Line> drives;
    private final Context context;

    public DrivesAdapter(List<Line> drives, Context context) {
        this.drives = drives;
        this.context = context;
    }

    @NonNull
    @Override
    public DriveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_drive, parent, false);
        return new DriveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriveViewHolder holder, int position) {
        Line drive = drives.get(position);
        holder.titleTextView.setText(drive.lineNum + " - " + drive.lineDescription);
        holder.subtitleTextView.setText("Departure time: " + drive.departureTime + "\nArrival time: " + drive.arrivalTime);
    }

    @Override
    public int getItemCount() {
        return drives.size();
    }

    public static class DriveViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, subtitleTextView;

        public DriveViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            subtitleTextView = itemView.findViewById(R.id.textViewSubtitle);
        }
    }
}
