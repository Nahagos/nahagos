package com.nahagos.nahagos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nahagos.nahagos.R;
import com.nahagos.nahagos.datatypes.Line;

import java.util.List;

public class LinesAdapter extends RecyclerView.Adapter<LinesAdapter.LineViewHolder> {

    private final List<Line> lines;

    public LinesAdapter(List<Line> lines) {
        this.lines = lines;
    }

    @NonNull
    @Override
    public LineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.line_list_element, parent, false);
        System.out.println("LinesAdapter.onCreateViewHolder");
        return new LineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LineViewHolder holder, int position) {
        Line line = lines.get(position);
        holder.lineNumber.setText(String.valueOf(line.line_num));
        holder.lineName.setText(line.name);
        holder.lineTime.setText(line.departure);
        holder.lineLive.setVisibility(line.isNahagos ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Clicked on line " + line.line_num, Toast.LENGTH_SHORT).show();
        });
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
