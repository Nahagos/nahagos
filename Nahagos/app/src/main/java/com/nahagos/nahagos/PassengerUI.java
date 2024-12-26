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
import android.widget.AdapterView;
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

    private final float ZOOM_SHOW_STOPS = 15.5F;
    private static final int STOP_ID_NOT_FOUND = -1;
    private final LatLng ISRAEL = new LatLng(30.974998182290868, 34.69264616803752);
    private final float START_ZOOM = 15.5F;
    private final float STOP_ZOOM = 16.5F;

    private GoogleMap _map;
    private ActivityPassengerUiBinding binding;
    private ArrayAdapter<SearchStopResult> adapter;
    private ListView suggestionList;

    private JSONArray _stops;

    private ArrayList<SearchStopResult> _lastSearchRes = new ArrayList<>();

    private ArrayList<Marker> _stopMarkers = new ArrayList<>();
    public LatLng startingPoint = ISRAEL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPassengerUiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this::onMapReady);

        suggestionList = findViewById(R.id.suggestions);
        SearchView search = findViewById(R.id.search);

        adapter = new ArrayAdapter<>(getBaseContext(), R.layout.list_sample_element, R.id.textView, _lastSearchRes);

        try {
            _stops = getStops();
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }

        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Make search list visible or not based on whether the user is typing
                suggestionList.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Search for q in the SQL, and update suggestion list as needed.
                _lastSearchRes.clear();
                try {
                    _lastSearchRes = searchStations(newText);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // This is the part of the code where we update the list of suggestions, based on the search results
                if (_lastSearchRes.isEmpty()) {
                    _lastSearchRes.add(new SearchStopResult(STOP_ID_NOT_FOUND, getString(R.string.stop_not_found)));
                }

                adapter.clear();
                adapter.addAll(_lastSearchRes);
                adapter.notifyDataSetChanged();

                suggestionList.setAdapter(adapter);

                return false;
            }
        });

        // When a list item (i.e. search result) is clicked, move to its place.
        suggestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int stopId = _lastSearchRes.get((int) id).first;
                if (stopId == STOP_ID_NOT_FOUND)
                    return;

                try {
                    JSONObject stop = _stops.getJSONObject(stopId);
                    _map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(stop.getDouble("stop_lat"), stop.getDouble("stop_lon")), STOP_ZOOM));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /*
        The function searches for stations that have the q in their name, and returns their index in the list.
    */
    ArrayList<SearchStopResult> searchStations(String q) throws JSONException {
        ArrayList<SearchStopResult> out = new ArrayList<>();
        for (int i = 0; i < _stops.length(); i++) {
            if (_stops.getJSONObject(i).getString("stop_name").contains(q)) {
                out.add(new SearchStopResult(i, _stops.getJSONObject(i).getString("stop_name")));
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
        _map = googleMap;
        _map.setOnMarkerClickListener(this::onMarkerClick);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        _map.setMyLocationEnabled(true);
        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, START_ZOOM));

        FusedLocationProviderClient fusedLocationProviderClient = fusedLocationProviderClient =     LocationServices.getFusedLocationProviderClient(this);;
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    // Get the location
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    startingPoint = new LatLng(latitude, longitude);
                    _map.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, START_ZOOM));
                    Log.d("PassengerUI",
                            "KAKIIIIIIIIIIIIIIIIII Latitude: " + latitude + ", Longitude: " + longitude);

                }

            }
            });


        _map.setOnCameraMoveListener(() -> {
            /* if moved, you need to show the markers that are in the view.
             how the algorithm works:
             there is a list of stopmarkers, those are the markers that we show right now.
             every time the screen moves, if the markers are not in range, delete them from the map (and our list)
             then, if the zoom is big enough - search for stops that are in range of the screen, add to the list, and show them.
             */
            // Search for markers that are shown in the map and not in range, and remove them.
            LatLngBounds mapViewBounds = _map.getProjection().getVisibleRegion().latLngBounds;
            for (int i = 0; i < _stopMarkers.size(); i++) {
                LatLng markerPos = _stopMarkers.get(i).getPosition();
                if (markerPos.latitude > mapViewBounds.northeast.latitude ||
                        markerPos.latitude < mapViewBounds.southwest.latitude ||
                        markerPos.longitude > mapViewBounds.northeast.longitude ||
                        markerPos.longitude < mapViewBounds.southwest.longitude) {
                    _stopMarkers.get(i).remove();
                    _stopMarkers.remove(i);
                }
            }
            // if the zoom is big enough, search for stops that are in range, and show them on the map
            if (_map.getCameraPosition().zoom >= ZOOM_SHOW_STOPS) {
                for (int i = 0; i < _stops.length(); i++) {
                    try {
                        LatLng stopLatLng = new LatLng(_stops.getJSONObject(i).getDouble("stop_lat"), _stops.getJSONObject(i).getDouble("stop_lon"));
                        if (stopLatLng.latitude <= mapViewBounds.northeast.latitude &&
                                stopLatLng.latitude >= mapViewBounds.southwest.latitude &&
                                stopLatLng.longitude <= mapViewBounds.northeast.longitude &&
                                stopLatLng.longitude >= mapViewBounds.southwest.longitude) {
                            _stopMarkers.add(_map.addMarker(new MarkerOptions()
                                    .position(new LatLng(_stops.getJSONObject(i).getDouble("stop_lat"), _stops.getJSONObject(i).getDouble("stop_lon")))
                                    .title(_stops.getJSONObject(i).getString("stop_name") + " | " + _stops.getJSONObject(i).getString("stop_code"))));
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