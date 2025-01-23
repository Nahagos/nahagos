package com.nahagos.nahagos.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nahagos.nahagos.R;
import com.nahagos.nahagos.server.ServerAPI;
import com.nahagos.nahagos.adapters.LinesAdapter;
import com.nahagos.nahagos.datatypes.Line;
import com.nahagos.nahagos.db.Tables.Stop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StopDetails extends BottomSheetDialogFragment {

    private static final String ARG_STOP_NAME = "stop";

    ArrayList<Line> lines = new ArrayList<>();

    private Thread fetchLinesThread;

    public static StopDetails newInstance(Stop stop) {
        StopDetails fragment = new StopDetails();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STOP_NAME, stop);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_stop_details, container, false);

        var args = getArguments();
        assert args != null;
        Stop stop = (Stop) getArguments().getSerializable(ARG_STOP_NAME);
        assert stop != null;

        TextView name = view.findViewById(R.id.stop_headline);
        TextView description = view.findViewById(R.id.stop_description);

        name.setText(getString(R.string.stop_headline, stop.name, stop.code));
        description.setText(getString(R.string.stop_description, stop.lat, stop.lon));

        var linesAdapter = new LinesAdapter(stop, lines, requireContext());

        RecyclerView linesRecyclerView = view.findViewById(R.id.lines);
        linesRecyclerView.setAdapter(linesAdapter);
        linesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        interruptFetchLinesThread();
        fetchLinesThread = new Thread(() -> {
            var linesFromServer = ServerAPI.getLinesByStation(stop.id);
            if (linesFromServer == null) {
                Log.e("StopDetails", "Failed to get lines from server");
                return;
            }
            var activity = getActivity();
            if (activity == null) return;
            lines.addAll(mergeLines(Arrays.asList(linesFromServer)));
            activity.runOnUiThread(() -> linesAdapter.notifyItemRangeChanged(0, lines.size()));
        });
        fetchLinesThread.start();

        return view;
    }

    private void interruptFetchLinesThread() {
        if (fetchLinesThread != null) {
            fetchLinesThread.interrupt();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        interruptFetchLinesThread();
    }

    private List<Line> mergeLines(List<Line> lines) {
        return lines.stream().collect(Collectors.groupingBy(l -> l.name + l.num))
                .values().stream().map(group -> {
                    Line line = group.get(0);
                    line.departure = group.stream().sorted(Comparator.comparing(l -> l.departure))
                            .limit(3).map(l -> l.isLive ? calcTimeFromNow(l.departure) : l.departure).collect(Collectors.joining(", "));
                    return line;
                }).sorted((l1, l2) -> {
                    if(l1.isNahagos && !l2.isNahagos) return -1;
                    if(!l1.isNahagos && l2.isNahagos) return 1;
                    if (l1.isLive && !l2.isLive) return -1;
                    if (!l1.isLive && l2.isLive) return 1;
                    if (l1.isLive)
                        return
                                Integer.parseInt(l1.departure.split(",")[0].replaceAll("[^0-9]", "")) -
                                        Integer.parseInt(l2.departure.split(",")[0].replaceAll("[^0-9]", ""));
                    return l1.departure.compareTo(l2.departure);

                })
                .collect(Collectors.toList());
    }

    private String calcTimeFromNow(String time) {
        String[] parts = time.split(":");
        Calendar cal = Calendar.getInstance();
        int[] fields = new int[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND};
        for (int i = 0; i < parts.length; i++) {
            cal.set(fields[i], Integer.parseInt(parts[i]));
        }
        long diff = cal.getTime().getTime() - System.currentTimeMillis();
        return getString(R.string.in_x_minutes, diff / 60000);
    }
}
