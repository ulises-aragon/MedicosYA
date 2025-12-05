package com.aragon.medicosya.ui.client.appointments;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aragon.medicosya.databinding.ItemAppointmentBinding;
import com.aragon.medicosya.models.Appointment;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    public interface OnAppointmentClick {
        void onAppointmentClick(Appointment appointment);
    }

    private final List<Appointment> items = new ArrayList<>();
    private final OnAppointmentClick listener;

    private final DateTimeFormatter dateTimeFmt =
            DateTimeFormatter.ofPattern("EEE d MMM, HH:mm", new Locale("es"));

    public AppointmentAdapter(OnAppointmentClick listener) {
        this.listener = listener;
    }

    public void setItems(List<Appointment> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppointmentBinding b = ItemAppointmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder {

        private final ItemAppointmentBinding binding;

        VH(ItemAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Appointment appointment) {
            binding.tvServiceName.setText(appointment.getServiceObject() != null ? appointment.getServiceObject().getName() : "Servicio");
            binding.tvProviderName.setText(appointment.getProviderObject() != null ? appointment.getProviderObject().getName() : "");

            binding.tvStatus.setText(appointment.getStatusEnum() != null ? appointment.getStatusEnum().getLabel() : "???");
            binding.tvStatus.setBackgroundColor(appointment.getStatusEnum() != null ? appointment.getStatusEnum().getColor() : 0);

            if (appointment.getStartAt() != null) {
                ZonedDateTime zdt = appointment.getStartAt().toDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault());
                binding.tvDateTime.setText(dateTimeFmt.format(zdt));
            } else {
                binding.tvDateTime.setText("?? ?? ??, ??:??");
            }

            if (appointment.getPayment() != null) {
                binding.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", appointment.getPayment().getAmountCents() / 100f));
            } else {
                binding.tvPrice.setText("$????");
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onAppointmentClick(appointment);
            });
        }
    }
}
