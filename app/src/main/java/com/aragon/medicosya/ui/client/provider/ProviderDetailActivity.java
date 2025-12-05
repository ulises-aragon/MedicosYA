package com.aragon.medicosya.ui.client.provider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aragon.medicosya.databinding.ActivityProviderDetailBinding;
import com.aragon.medicosya.models.Provider;
import com.aragon.medicosya.models.Service;
import com.aragon.medicosya.repository.ServiceRepository;
import com.aragon.medicosya.ui.client.booking.BookingActivity;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class ProviderDetailActivity extends AppCompatActivity implements ServiceAdapter.ServiceClickListener {

    private static final String EXTRA_PROVIDER_ID = "providerId";

    private ActivityProviderDetailBinding binding;
    private String providerId;
    private Provider provider;
    private ServiceRepository serviceRepo;
    private ServiceAdapter serviceAdapter;

    public static Intent newIntent(Context ctx, String providerId) {
        Intent i = new Intent(ctx, ProviderDetailActivity.class);
        i.putExtra(EXTRA_PROVIDER_ID, providerId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProviderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        serviceRepo = new ServiceRepository();

        providerId = getIntent().getStringExtra(EXTRA_PROVIDER_ID);
        if (providerId == null) {
            Toast.makeText(this, "Proveedor inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        serviceAdapter = new ServiceAdapter(new ArrayList<>(), this);
        binding.rvServices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServices.setAdapter(serviceAdapter);

        binding.btnOpenBooking.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("providerId", providerId);
            startActivity(intent);
        });

        binding.exitProviderDetail.setOnClickListener(v -> finish());

        loadProvider();
    }

    private void loadProvider() {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("providers").document(providerId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Proveedor no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    provider = doc.toObject(Provider.class);
                    if (provider == null) return;
                    provider.setId(doc.getId());

                    binding.tvName.setText(provider.getName() != null ? provider.getName() : "Sin nombre");
                    binding.tvCity.setText(provider.getAddress() != null && provider.getAddress().getCity() != null ?
                            String.valueOf(provider.getAddress().getCity()) : "");
                    binding.tvRating.setText(String.valueOf(provider.getRating()));

                    loadServices();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void loadServices() {
        serviceRepo.getServicesForProvider(providerId,
                services -> {
                    serviceAdapter.setServices(services);
                },
                e -> {
                    Toast.makeText(this, "Error cargando servicios: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onServiceClick(Service service) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("providerId", providerId);
        intent.putExtra("serviceId", service.getId());
        startActivity(intent);
    }
}
