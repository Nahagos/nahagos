package com.nahagos.nahagos.linechoose;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nahagos.nahagos.R;
import com.nahagos.nahagos.db.Tables.Stop;

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

        // Bind data to views
        TextView locationName = view.findViewById(R.id.location_name);
        TextView locationDetails = view.findViewById(R.id.location_details);

        locationName.setText(stop.getTitle());
        locationDetails.setText(getString(R.string.location_details, String.valueOf(stop.lat), String.valueOf(stop.lon)));

        return view;
    }
}

