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
import com.nahagos.nahagos.ServerAPI;
import com.nahagos.nahagos.db.Tables.Stop;

import java.util.ArrayList;
import java.util.List;

public class LocationDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STOP_NAME = "stop";

    public static LocationDetailsBottomSheet newInstance(Stop stop) {
        LocationDetailsBottomSheet fragment = new LocationDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STOP_NAME, stop);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_bottom_sheet_layout, container, false);

        // Retrieve data from arguments
        assert getArguments() != null;
        Stop stop = (Stop) getArguments().getSerializable(ARG_STOP_NAME);
        assert stop != null;

        // Bind data to views
        TextView locationName = view.findViewById(R.id.location_name);
        TextView locationDetails = view.findViewById(R.id.location_details);

        locationName.setText(stop.getTitle());
        locationDetails.setText(getString(R.string.location_details, String.valueOf(stop.lat), String.valueOf(stop.lon)));

        var lines = new ArrayList<LineData>();
        var linesAdapter = new LinesAdapter(lines);

        RecyclerView linesRecyclerView = view.findViewById(R.id.recycler_view);
        linesRecyclerView.setAdapter(linesAdapter);
        linesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        new Thread(() -> {
            // TODO: Fetch lines from server
            var fakeLines = List.of(
                    new LineData("tripid1", "שדרות שז''ר/בנייני האומה-ירושלים<->אוניברסיטת אריאל/כביש 31-אריאל-21\n", 290, "10:12", "Egged", false),
                    new LineData("tripid2", "ת. רכבת יבנה מערב-יבנה<->ת. רכבת יבנה מזרח-יבנה-1#\n", 3, "10:30", "Dan", true),
                    new LineData("tripid3", "נווה זוהר/מועצה אזורית-נווה זוהר<->ת. מרכזית ירושלים/הורדה-ירושלים-15\n", 486, "10:57", "Kavim", true),
                    new LineData("tripid4", "ת. רכבת יבנה מערב-יבנה<->ת. רכבת יבנה מזרח-יבנה-1#\n", 3, "11:30", "Dan", true),
                    new LineData("tripid5", "ת. מרכזית המפרץ/רציפים בינעירוני-חיפה<->ת.מרכזית עפולה/הורדה-עפולה-13\n", 301, "11:32", "Kavim", false),
                    new LineData("tripid6", "מכללת הרמלין/המחקר-נתניה<->ת. מרכזית חדרה/רציפים-חדרה-21\n", 57, "12:30", "Dan", true)
            );

            lines.clear();
            lines.addAll(fakeLines);

            requireActivity().runOnUiThread(() -> {
                linesAdapter.notifyItemRangeChanged(0, lines.size());
            });
        }).start();

        return view;
    }


}

