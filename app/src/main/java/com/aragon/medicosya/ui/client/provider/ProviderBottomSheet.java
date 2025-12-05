package com.aragon.medicosya.ui.client.provider;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aragon.medicosya.databinding.FragmentProviderBottomSheetBinding;
import com.aragon.medicosya.models.Provider;
import com.aragon.medicosya.ui.client.booking.BookingActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProviderBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PROVIDER_ID = "arg_provider_id";

    private FragmentProviderBottomSheetBinding binding;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String providerId;

    public static ProviderBottomSheet newInstance(String providerId) {
        ProviderBottomSheet bottomSheet = new ProviderBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PROVIDER_ID, providerId);
        bottomSheet.setArguments(args);
        return bottomSheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            providerId = getArguments().getString(ARG_PROVIDER_ID);
        }

        if (providerId == null) {
            dismiss();
            return;
        }

        loadProviderData(providerId);

        binding.btnViewDetail.setOnClickListener(v -> {
            Intent intent = ProviderDetailActivity.newIntent(requireContext(), providerId);
            startActivity(intent);
            dismiss();
        });

        binding.btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BookingActivity.class);
            intent.putExtra("providerId", providerId);
            startActivity(intent);
            dismiss();
        });
    }

    private void loadProviderData(String id) {
        binding.progress.setVisibility(View.VISIBLE);
        binding.container.setVisibility(View.INVISIBLE);
        firestore.collection("providers").document(id).get()
                .addOnSuccessListener(doc -> {
                    binding.progress.setVisibility(View.GONE);
                    binding.container.setVisibility(View.VISIBLE);

                    if (!doc.exists()) {
                        Toast.makeText(requireContext(), "Proveedor no encontrado", Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }
                    Provider provider = doc.toObject(Provider.class);
                    if (provider == null) {
                        Toast.makeText(requireContext(), "Error cargando proveedor", Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }
                    provider.setId(doc.getId());

                    binding.tvName.setText(provider.getName() == null ? "Sin nombre" : provider.getName());
                    binding.tvCity.setText(provider.getAddress() != null && provider.getAddress().getCity() != null
                            ? String.valueOf(provider.getAddress().getCity()) : "");
                    binding.tvRating.setText(String.valueOf(provider.getRating()));
                })
                .addOnFailureListener(e -> {
                    binding.progress.setVisibility(View.GONE);
                    binding.container.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    dismiss();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
