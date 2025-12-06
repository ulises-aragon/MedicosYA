package com.aragon.medicosya.ui.provider.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aragon.medicosya.databinding.FragmentProviderProfileBinding;
import com.aragon.medicosya.models.Provider;
import com.aragon.medicosya.ui.PickLocationActivity;
import com.aragon.medicosya.ui.session.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProviderProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private DocumentReference providerRef;

    private Double selectedLat = null;
    private Double selectedLng = null;

    private final ActivityResultLauncher<Intent> pickLocationLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    double lat = result.getData().getDoubleExtra(PickLocationActivity.EXTRA_LAT, 0);
                    double lng = result.getData().getDoubleExtra(PickLocationActivity.EXTRA_LNG, 0);
                    selectedLat = lat;
                    selectedLng = lng;
                    updateLocationLabel();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_LONG).show();
            return;
        }
        providerRef = db.collection("providers").document(uid);

        binding.btnSave.setOnClickListener(v -> saveProfile());
        binding.btnPickLocation.setOnClickListener(v -> openLocationPicker());
        binding.logOutBttn.setOnClickListener(v -> signOutAndGoToLogin());

        loadProfile();
    }

    private void signOutAndGoToLogin() {
        auth.signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void openLocationPicker() {
        Intent i = new Intent(requireContext(), PickLocationActivity.class);
        if (selectedLat != null && selectedLng != null) {
            i.putExtra(PickLocationActivity.EXTRA_LAT, selectedLat);
            i.putExtra(PickLocationActivity.EXTRA_LNG, selectedLng);
        }
        pickLocationLauncher.launch(i);
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!loading);
        binding.btnPickLocation.setEnabled(!loading);
    }

    private void loadProfile() {
        setLoading(true);
        providerRef.get()
                .addOnSuccessListener(doc -> {
                    setLoading(false);
                    if (!doc.exists()) return;
                    Provider provider = doc.toObject(Provider.class);
                    if (provider == null) return;

                    GeoPoint location = doc.getGeoPoint("address.location");
                    if (location != null) {
                        selectedLat = location.getLatitude();
                        selectedLng = location.getLongitude();
                    }

                    binding.inputClinicName.setText(provider.getName());

                    String city = provider.getAddress() != null ? provider.getAddress().getCity() : "Ciudad";
                    String address = provider.getAddress() != null ? provider.getAddress().getStreet() : "Direccion";
                    binding.inputCity.setText(city);
                    binding.inputAddress.setText(address);

                    updateLocationLabel();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(requireContext(),
                            "Error cargando perfil: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void updateLocationLabel() {
        if (selectedLat != null && selectedLng != null) {
            String text = String.format("Lat: %.5f, Lng: %.5f", selectedLat, selectedLng);
            binding.tvLocationValue.setText(text);
        } else {
            binding.tvLocationValue.setText("Sin ubicación seleccionada");
        }
    }

    private void saveProfile() {
        String clinicName = getText(binding.inputClinicName);
        String city = getText(binding.inputCity);
        String address = getText(binding.inputAddress);

        if (clinicName.isEmpty()) {
            binding.inputClinicName.setError("Requerido");
            return;
        }

        setLoading(true);
        saveProfileData(clinicName, city, address);
    }

    private void saveProfileData(String clinicName,
                                 String city,
                                 String address) {


        Map<String, Object> data = new HashMap<>();
        data.put("name", clinicName);

        Map<String, Object> addr = new HashMap<>();
        addr.put("city", city);
        addr.put("street", address);
        addr.put("location", new GeoPoint(selectedLat, selectedLng));

        data.put("address", addr);

        providerRef.update(data)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(requireContext(),
                            "Perfil actualizado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(requireContext(),
                            "Error guardando perfil: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private String getText(TextInputEditText ed) {
        return ed.getText() != null ? ed.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
