package com.aragon.medicosya.ui.client.appointments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aragon.medicosya.databinding.FragmentAppointmentsBinding;
import com.aragon.medicosya.models.Appointment;

import java.util.List;

public class AppointmentsFragment extends Fragment implements AppointmentAdapter.OnAppointmentClick {

    private FragmentAppointmentsBinding binding;
    private AppointmentsViewModel viewModel;
    private AppointmentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAppointmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AppointmentsViewModel.class);

        adapter = new AppointmentAdapter(this);
        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAppointments.setAdapter(adapter);

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(Boolean.TRUE.equals(isLoading));
            binding.progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getAppointments().observe(getViewLifecycleOwner(), this::renderList);

        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadAppointments());

        viewModel.loadAppointments();
    }

    private void renderList(List<Appointment> list) {
        adapter.setItems(list);
        boolean empty = (list == null || list.isEmpty());
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvAppointments.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onAppointmentClick(Appointment appt) {
        // TO-DO
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
