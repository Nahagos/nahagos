package com.nahagos.nahagos.renderers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.nahagos.nahagos.R;
import com.nahagos.nahagos.db.Tables.Stop;

public class StopRenderer extends DefaultClusterRenderer<Stop>{
    private final Context context;

    public StopRenderer(Context context, GoogleMap map, ClusterManager<Stop> clusterManager) {
        super(context, map, clusterManager);

        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull Stop item, @NonNull MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);


        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bus_stop);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
    }
}
