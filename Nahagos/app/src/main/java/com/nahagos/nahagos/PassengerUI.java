package com.nahagos.nahagos;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm;
import com.nahagos.nahagos.databinding.ActivityPassengerUiBinding;
import com.nahagos.nahagos.db.DBManager;
import com.nahagos.nahagos.db.Tables.Stop;
import com.nahagos.nahagos.linechoose.LocationDetailsBottomSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PassengerUI extends FragmentActivity {

    private static final int STOP_ID_NOT_FOUND = -1;
    private static final LatLng ISRAEL = new LatLng(30.974998182290868, 34.69264616803752);
    private static final float START_ZOOM = 15.5F;
    private static final float STOP_ZOOM = 16.5F;

    private DBManager dbManager;

    private GoogleMap map;
    private ArrayAdapter<Stop> adapter;
    private ListView suggestionList;

    private LatLng startingPoint = ISRAEL;

    private ClusterManager<Stop> clusterManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPassengerUiBinding binding = ActivityPassengerUiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this::onMapReady);

        dbManager = new DBManager(this);

        suggestionList = findViewById(R.id.suggestions);
        SearchView search = findViewById(R.id.search);

        adapter = new ArrayAdapter<>(getBaseContext(), R.layout.list_sample_element, R.id.textView, new ArrayList<>());

        suggestionList.setAdapter(adapter);

        search.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            // Make search list visible or not based on whether the user is typing
            suggestionList.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Search for q in the SQL, and update suggestion list as needed.
                var stops = searchStations(newText);
                // This is the part of the code where we update the list of suggestions, based on the search results
                if (stops.isEmpty()) {
                    stops.add(new Stop(STOP_ID_NOT_FOUND, getString(R.string.stop_not_found)));
                }

                adapter.clear();
                adapter.addAll(stops);
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        // When a list item (i.e. search result) is clicked, move to its place.
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

    /*
        The function searches for stations that have the q in their name, and returns their index in the list.
    */
    List<Stop> searchStations(String q) {
        var res = dbManager.stopsDao().searchByName(q);
        System.out.println("searched for " + q + " and found " + res.size() + " results");
        return res;
    }

    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        //TODO: this needs a callback, perms are async
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, START_ZOOM));

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener((task) -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Get the location
                Location location = task.getResult();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                startingPoint = new LatLng(latitude, longitude);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, START_ZOOM));

            }
        });

        clusterManager = new ClusterManager<>(this, map);
        map.setOnCameraIdleListener(clusterManager);
        clusterManager.setOnClusterItemClickListener(this::onStopClick);



        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        clusterManager.setAlgorithm(new NonHierarchicalViewBasedAlgorithm<>(metrics.widthPixels, metrics.heightPixels));


        for (var stop : dbManager.stopsDao().getAll()) {
            clusterManager.addItem(stop);
        }
    }

    public boolean onStopClick(Stop stop) {
        LocationDetailsBottomSheet.newInstance(stop).show(getSupportFragmentManager(), "location_details");
        return false;
    }
}
