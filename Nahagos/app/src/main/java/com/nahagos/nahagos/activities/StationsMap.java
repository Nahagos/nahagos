package com.nahagos.nahagos.activities;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.fragment.app.FragmentActivity;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;


import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm;
import com.nahagos.nahagos.R;
import com.nahagos.nahagos.databinding.ActivityStationsMapBinding;
import com.nahagos.nahagos.db.DBManager;
import com.nahagos.nahagos.db.Tables.Stop;
import com.nahagos.nahagos.fragments.StopDetails;
import com.nahagos.nahagos.renderers.StopRenderer;

import java.util.ArrayList;

public class StationsMap extends FragmentActivity {

    private static final int STOP_ID_NOT_FOUND = -1;
    private static final LatLng ISRAEL = new LatLng(30.974998182290868, 34.69264616803752);
    private static final float START_ZOOM = 15.5F;
    private static final float STOP_ZOOM = 16.5F;

    private DBManager dbManager;

    private GoogleMap map;
    private ArrayAdapter<Stop> adapter;
    private ListView suggestionList;


    private ClusterManager<Stop> clusterManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityStationsMapBinding binding = ActivityStationsMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frag_map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this::onMapReady);
        dbManager = new DBManager(this);

        suggestionList = findViewById(R.id.suggestions);
        SearchView search = findViewById(R.id.search);

        adapter = new ArrayAdapter<>(getBaseContext(), R.layout.list_sample_element, R.id.textView, new ArrayList<>());

        suggestionList.setAdapter(adapter);

        search.setOnQueryTextFocusChangeListener((v, hasFocus) -> suggestionList.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE));

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                var stops = dbManager.stopsDao().searchByName(newText);
                if (stops.isEmpty())
                    stops.add(new Stop(STOP_ID_NOT_FOUND, getString(R.string.stop_not_found)));

                adapter.clear();
                adapter.addAll(stops);
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        suggestionList.setOnItemClickListener((parent, view, position, id) -> {
            Stop stop = adapter.getItem(position);
            if (stop == null) return;
            if (stop.id == STOP_ID_NOT_FOUND) return;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(stop.getPosition(), STOP_ZOOM));
            suggestionList.setVisibility(View.INVISIBLE);
            search.clearFocus();

            onStopClick(stop);
        });
    }

    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ISRAEL, START_ZOOM));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        else
            centerMapOnGps();

        clusterManager = new ClusterManager<>(this, map);
        map.setOnCameraIdleListener(clusterManager);
        clusterManager.setOnClusterItemClickListener(this::onStopClick);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        clusterManager.setAlgorithm(new NonHierarchicalViewBasedAlgorithm<>(metrics.widthPixels, metrics.heightPixels));

        clusterManager.setRenderer(new StopRenderer(this, map, clusterManager));

        for (var stop : dbManager.stopsDao().getAll())
            clusterManager.addItem(stop);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        centerMapOnGps();
    }

    private void centerMapOnGps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null)
                    centerMapOnLocation(new LatLng(location.getLatitude(), location.getLongitude()));
            });
        }
    }

    private void centerMapOnLocation(LatLng location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    public boolean onStopClick(Stop stop) {
        StopDetails.newInstance(stop).show(getSupportFragmentManager(), "location_details");
        return false;
    }
}
