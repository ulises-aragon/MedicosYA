package com.aragon.medicosya.ui.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.aragon.medicosya.databinding.ActivityBecomeProviderBinding;
import com.aragon.medicosya.enums.UserRole;
import com.aragon.medicosya.models.Address;
import com.aragon.medicosya.models.Provider;
import com.aragon.medicosya.ui.PickLocationActivity;
import com.aragon.medicosya.ui.provider.ProviderMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


public class BecomeProviderActivity extends AppCompatActivity {

    private ActivityBecomeProviderBinding binding;
    private FirebaseFirestore db;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBecomeProviderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        binding.btnPickLocation.setOnClickListener(v -> openLocationPicker());
        binding.btnSubmit.setOnClickListener(v -> submit());
        binding.btnCancelProvider.setOnClickListener(v -> finish());
    }

    private void openLocationPicker() {
        Intent i = new Intent(this, PickLocationActivity.class);
        if (selectedLat != null && selectedLng != null) {
            i.putExtra(PickLocationActivity.EXTRA_LAT, selectedLat);
            i.putExtra(PickLocationActivity.EXTRA_LNG, selectedLng);
        }
        pickLocationLauncher.launch(i);
    }

    private void updateLocationLabel() {
        if (selectedLat != null && selectedLng != null) {
            String text = String.format("Lat: %.5f, Lng: %.5f", selectedLat, selectedLng);
            binding.tvLocationValue.setText(text);
        } else {
            binding.tvLocationValue.setText("Sin ubicación seleccionada");
        }
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(!loading);
        binding.btnPickLocation.setEnabled(!loading);
    }

    private void submit() {
        String clinicName = binding.inputClinicName.getText() != null ?
                binding.inputClinicName.getText().toString().trim() : "";
        String city = binding.inputCity.getText() != null ?
                binding.inputCity.getText().toString().trim() : "";
        String address = binding.inputAddress.getText() != null ?
                binding.inputAddress.getText().toString().trim() : "";

        if (selectedLat == null || selectedLng == null) {
            Toast.makeText(this, "Selecciona una ubicación", Toast.LENGTH_LONG).show();
            return;
        }

        if (clinicName.isEmpty()) {
            binding.inputClinicName.setError("Requerido");
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        saveProvider(uid, clinicName, city, address);
    }

    private void saveProvider(String uid,
                              String clinicName,
                              String city,
                              String address) {

        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                setLoading(false);
                Toast.makeText(this, "Usuario no existe", Toast.LENGTH_LONG).show();
                return;
            }
            Provider provider = new Provider();
            provider.setName(clinicName);

            Address addressObj = new Address();
            addressObj.setCity(city);
            addressObj.setStreet(address);
            addressObj.setLocation(new GeoPoint(selectedLat, selectedLng));
            provider.setAddress(addressObj);
            provider.setRating(5);
            provider.setUser(userRef);

            db.collection("providers").document(uid)
                    .set(provider)
                    .addOnSuccessListener(aVoid -> {
                        db.collection("users").document(uid)
                                .update("role", UserRole.PROVIDER)
                                .addOnSuccessListener(v -> {
                                    setLoading(false);
                                    Toast.makeText(this, "Cuenta convertida a proveedor", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(this, ProviderMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    setLoading(false);
                                    Toast.makeText(this, "Proveedor creado, pero error al actualizar rol: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    finish();
                                });
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "Error guardando proveedor: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }).addOnFailureListener(e -> {
            setLoading(false);
            Toast.makeText(this, "Error cargando usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
