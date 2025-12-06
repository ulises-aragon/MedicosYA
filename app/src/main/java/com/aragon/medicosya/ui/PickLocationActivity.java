package com.aragon.medicosya.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.aragon.medicosya.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PickLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LNG = "extra_lng";

    private GoogleMap mMap;
    private LatLng selectedLatLng = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        Button btnConfirm = findViewById(R.id.btnConfirmLocation);
        btnConfirm.setOnClickListener(v -> confirmLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // posición inicial: si te mandan una existente, úsala; si no, un default (por ej. San Salvador)
        double lat = getIntent().getDoubleExtra(EXTRA_LAT, 13.6929);
        double lng = getIntent().getDoubleExtra(EXTRA_LNG, -89.2182);
        LatLng initial = new LatLng(lat, lng);

        selectedLatLng = initial;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initial, 13f));
        mMap.addMarker(new MarkerOptions().position(initial));

        mMap.setOnMapClickListener(point -> {
            selectedLatLng = point;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(point));
        });
    }

    private void confirmLocation() {
        if (selectedLatLng == null) {
            Toast.makeText(this, "Selecciona un punto en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_LAT, selectedLatLng.latitude);
        data.putExtra(EXTRA_LNG, selectedLatLng.longitude);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}