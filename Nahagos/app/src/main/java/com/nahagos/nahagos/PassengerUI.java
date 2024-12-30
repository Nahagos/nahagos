package com.nahagos.nahagos;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nahagos.nahagos.databinding.ActivityPassengerUiBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


// GPS location imports
import android.location.Location;

public class PassengerUI extends FragmentActivity {

    private static final float ZOOM_SHOW_STOPS = 15.5F;
    private static final int STOP_ID_NOT_FOUND = -1;
    private static final LatLng ISRAEL = new LatLng(30.974998182290868, 34.69264616803752);
    private static final float START_ZOOM = 15.5F;
    private static final float STOP_ZOOM = 16.5F;

    private GoogleMap map;
    private ArrayAdapter<SearchStopResult> adapter;
    private ListView suggestionList;

    private JSONArray stops;

    private ArrayList<SearchStopResult> lastSearchRes = new ArrayList<>();

    private final ArrayList<Marker> stopMarkers = new ArrayList<>();
    public LatLng startingPoint = ISRAEL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPassengerUiBinding binding = ActivityPassengerUiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this::onMapReady);

        suggestionList = findViewById(R.id.suggestions);
        SearchView search = findViewById(R.id.search);

        adapter = new ArrayAdapter<>(getBaseContext(), R.layout.list_sample_element, R.id.textView, lastSearchRes);

        suggestionList.setAdapter(adapter);
        try {
            stops = getStops();
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }

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
                lastSearchRes.clear();
                try {
                    lastSearchRes = searchStations(newText);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // This is the part of the code where we update the list of suggestions, based on the search results
                if (lastSearchRes.isEmpty()) {
                    lastSearchRes.add(new SearchStopResult(STOP_ID_NOT_FOUND, getString(R.string.stop_not_found)));
                }

                adapter.clear();
                adapter.addAll(lastSearchRes);
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        // When a list item (i.e. search result) is clicked, move to its place.
        suggestionList.setOnItemClickListener((parent, view, position, id) -> {
            int stopId = lastSearchRes.get((int) id).first;
            if (stopId == STOP_ID_NOT_FOUND)
                return;

            try {
                JSONObject stop = stops.getJSONObject(stopId);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(stop.getDouble("stop_lat"), stop.getDouble("stop_lon")), STOP_ZOOM));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /*
        The function searches for stations that have the q in their name, and returns their index in the list.
    */
    ArrayList<SearchStopResult> searchStations(String q) throws JSONException {
        ArrayList<SearchStopResult> out = new ArrayList<>();
        for (int i = 0; i < stops.length(); i++) {
            if (stops.getJSONObject(i).getString("stop_name").contains(q)) {
                out.add(new SearchStopResult(i, stops.getJSONObject(i).getString("stop_name")));
            }
        }
        return out;
    }

    /*
        The function gets the stops from the json file, and returns JSONArray of them.
        To be changed, for SQL-suitable objects
    */
    private JSONArray getStops() throws IOException, JSONException {
        BufferedReader reader =new BufferedReader(new InputStreamReader(getAssets().open("stops.json")));
        StringBuilder output = new StringBuilder();
        String tmp;
        while ((tmp = reader.readLine()) != null) {
            output.append(tmp);
        }
        return new JSONArray(output.toString());
    }

    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this::onMarkerClick);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, START_ZOOM));

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);;
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


        map.setOnCameraMoveListener(() -> {
            /* if moved, you need to show the markers that are in the view.
             how the algorithm works:
             there is a list of stopmarkers, those are the markers that we show right now.
             every time the screen moves, if the markers are not in range, delete them from the map (and our list)
             then, if the zoom is big enough - search for stops that are in range of the screen, add to the list, and show them.
             */
            // Search for markers that are shown in the map and not in range, and remove them.
            LatLngBounds mapViewBounds = map.getProjection().getVisibleRegion().latLngBounds;
            for (int i = 0; i < stopMarkers.size(); i++) {
                LatLng markerPos = stopMarkers.get(i).getPosition();
                if (markerPos.latitude > mapViewBounds.northeast.latitude ||
                        markerPos.latitude < mapViewBounds.southwest.latitude ||
                        markerPos.longitude > mapViewBounds.northeast.longitude ||
                        markerPos.longitude < mapViewBounds.southwest.longitude) {
                    stopMarkers.get(i).remove();
                    stopMarkers.remove(i);
                }
            }
            // if the zoom is big enough, search for stops that are in range, and show them on the map
            if (map.getCameraPosition().zoom >= ZOOM_SHOW_STOPS) {
                for (int i = 0; i < stops.length(); i++) {
                    try {
                        LatLng stopLatLng = new LatLng(stops.getJSONObject(i).getDouble("stop_lat"), stops.getJSONObject(i).getDouble("stop_lon"));
                        if (stopLatLng.latitude <= mapViewBounds.northeast.latitude &&
                                stopLatLng.latitude >= mapViewBounds.southwest.latitude &&
                                stopLatLng.longitude <= mapViewBounds.northeast.longitude &&
                                stopLatLng.longitude >= mapViewBounds.southwest.longitude) {
                            stopMarkers.add(map.addMarker(new MarkerOptions()
                                    .position(new LatLng(stops.getJSONObject(i).getDouble("stop_lat"), stops.getJSONObject(i).getDouble("stop_lon")))
                                    .title(stops.getJSONObject(i).getString("stop_name") + " | " + stops.getJSONObject(i).getString("stop_code"))));
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public boolean onMarkerClick(@NonNull Marker marker) {
        // TODO: show lines arriving/planned for selected station.
        return false;
    }
}