package com.nahagos.nahagos;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import java.lang.reflect.Array;
import java.util.ArrayList;

public class PassengerUI extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityPassengerUiBinding binding;
    private SearchView search;
    private ListView suggestionList;

    private JSONArray _stops;

    private ArrayList<StopResult> _last_search_res;

    private boolean _gpsAccessGranted = false;

    private final float ZOOM_SHOW_STOPS=14.5F;

    private ArrayList<Marker> _stopMarkers;
    private LatLng ISRAEL;
    private final float START_ZOOM = 8F;
    private final float STOP_ZOOM = 15.5F;

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
        ISRAEL = new LatLng(30.974998182290868, 34.69264616803752);

        suggestionList = findViewById(R.id.suggestions);
        _last_search_res = new ArrayList<>();

        try {
            _stops = getStops();
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        search = findViewById(R.id.search);
        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
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
                ArrayList<Integer> stop_ids = new ArrayList<>();
                try {
                    stop_ids = searchStations(newText);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if (newText.length() > 2 && !_last_search_res.isEmpty() && _last_search_res.get(0).second.contains(newText.substring(0,newText.length()-1))) {
                    for (int j = 0; j < _last_search_res.size(); j++) {
                        if (!_last_search_res.get(j).second.contains(newText)) {
                            _last_search_res.remove(j);
                        }
                    }
                }
                else {
                    _last_search_res.clear();
                    for (int i = 0; i < stop_ids.size(); i++) {
                        try {
                            _last_search_res.add(new StopResult(stop_ids.get(i), _stops.getJSONObject(stop_ids.get(i)).getString("stop_name")));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                ArrayAdapter<StopResult> adapter;
                if (stop_ids.isEmpty()) {
                    _last_search_res.add(new StopResult(-1, "לא נמצאה תחנה מתאימה"));
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

    ArrayList<Integer> searchStations(String q) throws JSONException {
        ArrayList<Integer> out = new ArrayList<Integer>();
        for (int i = 0; i < _stops.length(); i++) {
            if (_stops.getJSONObject(i).getString("stop_name").contains(q)) {
                out.add(i);
            }
        }
        return out;
    }

    private JSONArray getStops() throws IOException, JSONException {
        BufferedReader reader =new BufferedReader(new InputStreamReader(getAssets().open("stops.json")));
        StringBuilder output = new StringBuilder();
        String tmp;
        while ((tmp = reader.readLine()) != null) {
            output.append(tmp);
        }
        return new JSONArray(output.toString());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng startingPoint = null;
        /*fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            startingPoint = new LatLng(location.getLatitude(), location.getLatitude());
                        }
                    }
                });
*/
        if (startingPoint == null)
            startingPoint = ISRAEL;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, START_ZOOM));

        mMap.setOnCameraMoveListener(() -> {
            CameraPosition pos = mMap.getCameraPosition();
            double lat = pos.target.latitude, lon = pos.target.longitude;
            double zoomRadius = Math.pow(2, 8 - pos.zoom);
            for (int i = 0; i < _stopMarkers.size(); i++) {
                LatLng markerPos = _stopMarkers.get(i).getPosition();
                if (Math.abs(lon-markerPos.longitude) >= zoomRadius || Math.abs(lat-markerPos.latitude) >= zoomRadius) {
                    _stopMarkers.get(i).remove();
                    _stopMarkers.remove(i);
                }
            }
            if (pos.zoom >= ZOOM_SHOW_STOPS) {
                for (int i = 0; i < _stops.length(); i++) {
                    try {
                        if (Math.abs(lon-_stops.getJSONObject(i).getDouble("stop_lon")) < zoomRadius && Math.abs(lat-_stops.getJSONObject(i).getDouble("stop_lat")) < zoomRadius)
                            _stopMarkers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(_stops.getJSONObject(i).getDouble("stop_lat"), _stops.getJSONObject(i).getDouble("stop_lon")))));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

}