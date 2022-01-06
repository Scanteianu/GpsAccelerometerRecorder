package com.example.potholes;

import android.location.Location;
import android.location.LocationListener;

import androidx.annotation.NonNull;

public class GPSListener  implements LocationListener {
    public double latitude =0; //null island
    public double longitude =0; //we really boolean out here
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }
}
