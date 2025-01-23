package com.nahagos.nahagos.fragments;

import android.os.Bundle;
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

public class StopDetails extends BottomSheetDialogFragment {

    private static final String ARG_STOP_NAME = "stop";

    ArrayList<Line> lines = new ArrayList<>();

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

        name.setText(getString(R.string.stop_headline, stop.name, stop.id));
        description.setText(getString(R.string.stop_description, String.valueOf(stop.lat), String.valueOf(stop.lon)));

        var linesAdapter = new LinesAdapter(stop, lines, requireContext());

        RecyclerView linesRecyclerView = view.findViewById(R.id.lines);
        linesRecyclerView.setAdapter(linesAdapter);
        linesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        new Thread(() -> {
            lines.addAll(Arrays.asList(ServerAPI.getLinesByStation(stop.id)));
            requireActivity().runOnUiThread(() -> linesAdapter.notifyItemRangeChanged(0, lines.size()));
        }).start();

        return view;
    }
}
