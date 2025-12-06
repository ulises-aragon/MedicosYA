package com.aragon.medicosya.ui.provider.appointments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aragon.medicosya.databinding.FragmentProviderAppointmentsBinding;
import com.aragon.medicosya.models.Appointment;
import com.aragon.medicosya.repository.AppointmentRepository;
import com.aragon.medicosya.ui.client.appointments.AppointmentAdapter;

import com.aragon.medicosya.ui.provider.clients.ClientDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AppointmentsFragment extends Fragment
        implements AppointmentAdapter.OnAppointmentClick,
        AppointmentBottomSheet.Listener {

    private FragmentProviderAppointmentsBinding binding;
    private AppointmentRepository repo;
    private AppointmentAdapter adapter;
    private DocumentReference providerRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderAppointmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repo = new AppointmentRepository();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_LONG).show();
            return;
        }
        providerRef = FirebaseFirestore.getInstance()
                .collection("providers")
                .document(uid);

        adapter = new AppointmentAdapter(this);
        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAppointments.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadAppointments);

        loadAppointments();
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.swipeRefresh.setRefreshing(loading);
    }

    private void renderList(List<Appointment> list) {
        adapter.setItems(list);
        boolean empty = list == null || list.isEmpty();
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvAppointments.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void loadAppointments() {
        if (providerRef == null) return;
        setLoading(true);
        repo.getAppointmentsForProvider(providerRef, list -> {
            setLoading(false);
            renderList(list);
        }, e -> {
            setLoading(false);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onAppointmentClick(Appointment appt) {
        if (appt.getId() == null) {
            Toast.makeText(requireContext(), "Cita sin ID", Toast.LENGTH_SHORT).show();
            return;
        }
        AppointmentBottomSheet sheet =
                AppointmentBottomSheet.newInstance(appt.getId());
        sheet.show(getParentFragmentManager(), "provider_appt_sheet");
    }

    @Override
    public void onAppointmentUpdated() {
        loadAppointments();
    }

    @Override
    public void onOpenClient(String clientId) {
        Intent i = new Intent(requireContext(), ClientDetailActivity.class);
        i.putExtra("clientId", clientId);
        i.putExtra("providerId", providerRef.getId());
        startActivity(i);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
