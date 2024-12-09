package com.nahagos.nahagos;

import androidx.fragment.app.FragmentActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PassengerUI extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityPassengerUiBinding binding;

    //private SQLiteDatabase _db;

    private JSONArray _stops;

    private final float ZOOM_SHOW_STOPS=14F;

    private ArrayList<Marker> _stopMarkers;

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
        //_db = SQLiteDatabase.openDatabase("file:///android_asset/stops.sql", null, 0);

        try {
            _stops = getStops();
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng israel = new LatLng(30.974998182290868, 34.69264616803752);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(israel, 8F));
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