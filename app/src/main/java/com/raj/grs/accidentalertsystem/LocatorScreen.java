package com.raj.grs.accidentalertsystem;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocatorScreen extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String lat, longi;
    private float latitude,longitude;
    float x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator_screen);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            try {
                latitude = Float.parseFloat(extras.getString("lat"));
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }
            try {
                longitude =Float.parseFloat(extras.getString("long"));
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }
        }

        Log.d("x:"," "+latitude);Log.d("y:"," "+longitude);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Last Known Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
