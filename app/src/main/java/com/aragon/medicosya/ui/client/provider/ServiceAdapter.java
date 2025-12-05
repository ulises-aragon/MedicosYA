package com.aragon.medicosya.ui.client.provider;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aragon.medicosya.databinding.ItemServiceBinding;
import com.aragon.medicosya.models.Service;

import java.util.List;
import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.VH> {

    public interface ServiceClickListener {
        void onServiceClick(Service service);
    }

    private List<Service> services;
    private final ServiceClickListener listener;

    public ServiceAdapter(List<Service> services, ServiceClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    public void setServices(List<Service> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceBinding b = ItemServiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(services.get(position));
    }

    @Override
    public int getItemCount() {
        return services == null ? 0 : services.size();
    }

    class VH extends RecyclerView.ViewHolder {
        final ItemServiceBinding binding;
        VH(ItemServiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Service service) {
            binding.tvServiceName.setText(service.getName());
            binding.tvServiceDesc.setText(service.getDescription() != null ? service.getDescription() : "");
            String meta = service.getDuration() + " min · $" + String.format(Locale.getDefault(), "%.2f", service.getPriceCents() / 100f);
            binding.tvServiceMeta.setText(meta);

            binding.tvBooking.setOnClickListener(v -> {
                if (listener != null) listener.onServiceClick(service);
            });
        }
    }
}

