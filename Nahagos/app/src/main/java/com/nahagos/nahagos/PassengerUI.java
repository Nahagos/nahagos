package com.nahagos.nahagos;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
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
import android.location.LocationListener;
import android.location.LocationManager;

public class PassengerUI extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private ActivityPassengerUiBinding binding;
    private SearchView search;
    private ListView suggestionList;

    private JSONArray _stops;

    private ArrayList<SearchStopResult> _last_search_res;

    private boolean _gpsAccessGranted = false;

    private final float ZOOM_SHOW_STOPS = 15.5F;
    private final double H_TO_W_RATIO = 3;

    private ArrayList<Marker> _stopMarkers;
    public LatLng startingPoint = null;
    private LatLng ISRAEL = new LatLng(30.974998182290868, 34.69264616803752);
    private final float START_ZOOM = 15.5F;
    private final float STOP_ZOOM = 15.5F;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        binding = ActivityPassengerUiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        _stopMarkers = new ArrayList<Marker>();

        suggestionList = findViewById(R.id.suggestions);
        _last_search_res = new ArrayList<>();

        try {
            _stops = getStops();
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }

        search = findViewById(R.id.search);
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
                // If the length of the query is less than 2 there are too many stored stations to be searched.
                // So it is better to just search all over again in the array from the JSON.
                // Otherwise, search for q in all of the last searched objects, for optimization.
                if (newText.length() > 2 && !_last_search_res.isEmpty() && _last_search_res.get(0).second.contains(newText.substring(0,newText.length()-1))) {
                    for (int j = 0; j < _last_search_res.size(); j++) {
                        if (!_last_search_res.get(j).second.contains(newText)) {
                            _last_search_res.remove(j);
                        }
                    }
                }
                else {
                    _last_search_res.clear();
                    try {
                        _last_search_res = searchStations(newText);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                // This is the part of the code where we update the list of suggestions, based on the search results
                ArrayAdapter<SearchStopResult> adapter;
                if (_last_search_res.isEmpty()) {
                    _last_search_res.add(new SearchStopResult(-1, "לא נמצאה תחנה מתאימה"));
                    adapter = new ArrayAdapter<>(getBaseContext(), R.layout.list_sample_element, R.id.textView, _last_search_res);
                    suggestionList.setAdapter(adapter);
                }
                else {
                    adapter = new ArrayAdapter<>(getBaseContext(), R.layout.list_sample_element, R.id.textView, _last_search_res);
                    suggestionList.setAdapter(adapter);
                }
                return false;
            }
        });

        // When a list item (i.e. search result) is clicked, move to its place.
        suggestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (_last_search_res.get((int) id).first == -1)
                    return;

                try {
                    JSONObject stop = _stops.getJSONObject(_last_search_res.get((int) id).first);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(stop.getDouble("stop_lat"), stop.getDouble("stop_lon")), STOP_ZOOM));
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
        FusedLocationProviderClient fusedLocationProviderClient = fusedLocationProviderClient =     LocationServices.getFusedLocationProviderClient(this);;
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    // Get the location
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng local_gps = new LatLng(30.974998182290868, 34.69264616803752);
                    startingPoint = local_gps;
                    Log.d("PassengerUI",
                            "KAKIIIIIIIIIIIIIIIIII Latitude: " + latitude + ", Longitude: " + longitude);
                }
                else{
                    Log.d("PassengerUI", "kakai");
                    startingPoint = ISRAEL;
                }

            }
            });



            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        //LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        //Location gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (startingPoint == null)
            startingPoint = ISRAEL;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, START_ZOOM));

        mMap.setOnCameraMoveListener(() -> {
            /* if moved, you need to show the markers that are in the view.
             how the algorithm works:
             there is a list of stopmarkers, those are the markers that we show right now.
             every time the screen moves, if the markers are not in range, delete them from the map (and our list)
             then, if the zoom is big enough - search for stops that are in range of the screen, add to the list, and show them.
             */
            CameraPosition pos = mMap.getCameraPosition();
            double lat = pos.target.latitude, lon = pos.target.longitude;
            // That's the part that turns zoom level to latlon-matching size in map.
            double zoomRadius = Math.pow(2, 8 - pos.zoom);
            // Search for markers that are shown in the map and not in range, and remove them.
            for (int i = 0; i < _stopMarkers.size(); i++) {
                LatLng markerPos = _stopMarkers.get(i).getPosition();
                if (Math.abs(lon-markerPos.longitude) >= zoomRadius || Math.abs(lat-markerPos.latitude) >= zoomRadius*H_TO_W_RATIO) {
                    _stopMarkers.get(i).remove();
                    _stopMarkers.remove(i);
                }
            }
            // if the zoom is big enough, search for stops that are in range, and show them on the map
            if (pos.zoom >= ZOOM_SHOW_STOPS) {
                for (int i = 0; i < _stops.length(); i++) {
                    try {
                        if (Math.abs(lon-_stops.getJSONObject(i).getDouble("stop_lon")) < zoomRadius && Math.abs(lat-_stops.getJSONObject(i).getDouble("stop_lat")) < zoomRadius*H_TO_W_RATIO) {
                            _stopMarkers.add(mMap.addMarker(new MarkerOptions()
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


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        // TODO: show lines arriving/planned for selected station.
        return false;
    }
}