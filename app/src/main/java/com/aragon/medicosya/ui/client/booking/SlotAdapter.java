package com.aragon.medicosya.ui.client.booking;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aragon.medicosya.R;
import com.aragon.medicosya.databinding.ItemSlotBinding;

import java.util.ArrayList;
import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.VH> {

    public interface SlotClick {
        void onSlotSelected(int position, BookingActivity.Slot slot);
    }

    private final List<BookingActivity.Slot> items = new ArrayList<>();
    private final SlotClick listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public SlotAdapter(SlotClick listener) {
        this.listener = listener;
    }

    public void setSlots(List<BookingActivity.Slot> slots) {
        items.clear();
        if (slots != null) items.addAll(slots);
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;

        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSlotBinding b = ItemSlotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position), position == selectedPosition);
    }

    @Override
    public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        final ItemSlotBinding binding;
        VH(ItemSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BookingActivity.Slot slot, boolean isSelected) {
            binding.tvTime.setText(slot.getLabel());
            int backgroundColor = isSelected ?
                    binding.getRoot().getContext().getColor(R.color.action_dim) :
                    binding.getRoot().getContext().getColor(R.color.white);
            int textColor = isSelected ?
                    binding.getRoot().getContext().getColor(R.color.white) :
                    binding.getRoot().getContext().getColor(R.color.darker_gray);

            binding.tvTime.setTextColor(textColor);
            binding.getRoot().setCardBackgroundColor(backgroundColor);
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onSlotSelected(getAbsoluteAdapterPosition(), slot);
            });
        }
    }
}
