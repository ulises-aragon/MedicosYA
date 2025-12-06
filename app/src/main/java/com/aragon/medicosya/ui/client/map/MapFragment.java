package com.aragon.medicosya.ui.client.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aragon.medicosya.R;
import com.aragon.medicosya.models.Address;
import com.aragon.medicosya.models.Provider;
import com.aragon.medicosya.ui.client.provider.ProviderBottomSheet;
import com.aragon.medicosya.ui.client.provider.ProvidersViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private BitmapDescriptor providerIcon;
    private FusedLocationProviderClient fusedLocationClient;
    private ProvidersViewModel viewModel;
    private final Map<String, Marker> markersByProvider = new HashMap<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted && map != null) {
                    enableMyLocation();
                    moveCameraToUserLocation();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        Log.i("Map Fragment", "Creating view...");
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        Log.i("Map Fragment", "Creating map...");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        Log.i("Map Fragment", "Observing providers...");
        viewModel = new ViewModelProvider(requireActivity()).get(ProvidersViewModel.class);

        viewModel.getProviders().observe(getViewLifecycleOwner(), providers -> {
            addMarkers(providers);
        });

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        providerIcon = BitmapDescriptorFactory.fromResource(R.drawable.outline_apartment_24);
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_no_poi));
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            moveCameraToUserLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        map.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof String) {
                String providerId = (String) tag;
                openProviderDetail(providerId);
            }
            return true;
        });

        Log.i("Map Fragment", "Loading providers!");
        viewModel.loadProviders();
    }

    private void enableMyLocation() {
        if (map == null) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
    }

    private void addMarkers(List<Provider> providers) {
        if (map == null || providers == null) return;

        map.clear();
        markersByProvider.clear();

        for (Provider p : providers) {
            Address address = p.getAddress();
            if (address == null) continue;
            if (address.getLocation() == null) continue;
            LatLng pos = new LatLng(address.getLocation().getLatitude(), address.getLocation().getLongitude());
            MarkerOptions mo = new MarkerOptions()
                    .position(pos)
                    .title(p.getName())
                    .snippet(address.getCity() != null ? address.getCity() : "");

            Marker marker = map.addMarker(mo);
            if (marker != null) {
                marker.setTag(p.getId());
                markersByProvider.put(p.getId(), marker);
            }
        }
    }

    private void openProviderDetail(String providerId) {
        ProviderBottomSheet sheet = ProviderBottomSheet.newInstance(providerId);
        sheet.show(getChildFragmentManager(), "provider_bottom_sheet");
    }

    @SuppressLint("MissingPermission")
    private void moveCameraToUserLocation() {
        if (map == null) return;
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            LatLng target;
            if (location != null) {
                target = new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                target = new LatLng(13.6929, -89.2182);
            }

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 15f));
        });
    }
}
