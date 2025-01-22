package com.nahagos.nahagos.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.activities.LineView;
import com.nahagos.nahagos.datatypes.Line;
import com.nahagos.nahagos.db.Tables.Stop;

import java.util.List;

public class LinesAdapter extends RecyclerView.Adapter<LinesAdapter.LineViewHolder> {

    private final Stop referringStop;
    private final List<Line> lines;
    private final Context context;

    public LinesAdapter(Stop referringStop, List<Line> lines, Context context) {
        this.referringStop = referringStop;
        this.lines = lines;
        this.context = context;
    }

    @NonNull
    @Override
    public LineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_element_stop_line, parent, false);
        return new LineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LineViewHolder holder, int position) {
        Line line = lines.get(position);
        holder.lineNumber.setText(String.valueOf(line.line_num));
        holder.lineName.setText(line.name);
        holder.lineTime.setText(line.departure);
        holder.lineLive.setVisibility(line.isNahagos ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> context.startActivity(
                new Intent(context, LineView.class)
                        .putExtra("lineName", line.name)
                        .putExtra("stopId", referringStop.id)
                        .putExtra("tripId", line.trip_id)
                        .putExtra("nahagosOnline", line.isNahagos)
        ));
    }

    @Override
    public int getItemCount() {
        return lines.size();
    }


    public static class LineViewHolder extends RecyclerView.ViewHolder {
        public final TextView lineNumber;
        public final TextView lineName;
        public final TextView lineTime;
        public final ImageView lineLive;

        public LineViewHolder(@NonNull View itemView) {
            super(itemView);
            lineNumber = itemView.findViewById(R.id.line_number);
            lineName = itemView.findViewById(R.id.line_name);
            lineTime = itemView.findViewById(R.id.line_time);
            lineLive = itemView.findViewById(R.id.line_live);
        }
    }
}
