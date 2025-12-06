package com.aragon.medicosya.ui.provider.clients;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aragon.medicosya.R;
import com.aragon.medicosya.databinding.ItemClientBinding;
import com.aragon.medicosya.models.User;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.VH> {

    public interface OnClientClick {
        void onClientClick(User client);
    }

    private final List<User> items = new ArrayList<>();
    private final OnClientClick listener;

    public ClientAdapter(OnClientClick listener) {
        this.listener = listener;
    }

    public void setItems(List<User> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemClientBinding b = ItemClientBinding.inflate(
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
        private final ItemClientBinding b;

        VH(ItemClientBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(User u) {
            b.tvName.setText(u.getName() != null ? u.getName() : "Cliente");
            b.tvEmail.setText(u.getEmail() != null ? u.getEmail() : "");

            b.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onClientClick(u);
            });
        }
    }
}
