package com.aragon.medicosya.ui.provider.clients;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aragon.medicosya.databinding.ActivityClientDetailBinding;
import com.aragon.medicosya.models.Appointment;
import com.aragon.medicosya.repository.AppointmentRepository;
import com.aragon.medicosya.repository.UserRepository;
import com.aragon.medicosya.ui.client.appointments.AppointmentAdapter;
import com.aragon.medicosya.ui.client.provider.ProviderDetailActivity;
import com.aragon.medicosya.ui.provider.appointments.AppointmentBottomSheet;
import com.aragon.medicosya.ui.provider.booking.BookingActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClientDetailActivity extends AppCompatActivity
        implements AppointmentAdapter.OnAppointmentClick {

    private ActivityClientDetailBinding binding;
    private final UserRepository userRepository = new UserRepository();

    private DocumentReference clientRef;
    private DocumentReference providerRef;

    private AppointmentAdapter adapter;

    public static Intent newIntent(Context ctx, String clientId, String providerId) {
        Intent i = new Intent(ctx, ProviderDetailActivity.class);
        i.putExtra("clientId", clientId);
        i.putExtra("providerId", providerId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClientDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String clientId = getIntent().getStringExtra("clientId");
        String providerId = getIntent().getStringExtra("providerId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        clientRef = db.collection("users").document(clientId);
        providerRef = db.collection("providers").document(providerId);

        adapter = new AppointmentAdapter(this);
        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAppointments.setAdapter(adapter);

        binding.btnNewAppointment.setOnClickListener(v -> {
            Intent i = new Intent(this, BookingActivity.class);
            i.putExtra("providerId", providerId);
            i.putExtra("clientId", clientId);
            startActivity(i);
        });

        binding.exitClientDetail.setOnClickListener(v -> finish());

        loadClient();
        loadHistory();
    }

    private void loadClient() {
        userRepository.getUserFromReference(clientRef, user -> {
            if (user == null) return;
            binding.tvClientName.setText(user.getName());
            binding.tvClientEmail.setText(user.getEmail());
        }, e -> {
            Toast.makeText(this, "Error cargando cliente: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void loadHistory() {
        userRepository.getAppointmentsForClientWithProvider(clientRef, providerRef, list -> {
            adapter.setItems(list);
        }, e -> {
            Toast.makeText(this, "Error cargando historial: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onAppointmentClick(Appointment appointment) {
        AppointmentBottomSheet sheet = AppointmentBottomSheet.newInstance(appointment.getId());
        sheet.show(getSupportFragmentManager(), "provider_appt_from_client");
    }
}
