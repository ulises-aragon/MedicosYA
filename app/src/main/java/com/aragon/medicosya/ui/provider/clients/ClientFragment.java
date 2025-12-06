package com.aragon.medicosya.ui.provider.clients;

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

import com.aragon.medicosya.databinding.FragmentProviderClientsBinding;
import com.aragon.medicosya.models.Appointment;
import com.aragon.medicosya.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientFragment extends Fragment implements ClientAdapter.OnClientClick {

    private FragmentProviderClientsBinding binding;
    private FirebaseFirestore db;
    private String providerId;
    private DocumentReference providerRef;
    private ClientAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderClientsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        providerId = FirebaseAuth.getInstance().getUid();

        if (providerId == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_LONG).show();
            return;
        }

        providerRef = db.collection("providers").document(providerId);

        adapter = new ClientAdapter(this);
        binding.rvClients.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClients.setAdapter(adapter);

        loadClients();
    }

    private void setLoading(boolean loading) {
        // simple: mostrar sólo el texto de "sin clientes"
        // si quieres un ProgressBar, añade uno en el layout
    }

    private void loadClients() {
        setLoading(true);
        db.collection("appointments")
                .whereEqualTo("provider", providerRef)
                .get()
                .addOnSuccessListener(qs -> {
                    Set<DocumentReference> clientRefs = new HashSet<>();
                    List<Appointment> apps = qs.toObjects(Appointment.class);
                    for (int i = 0; i < qs.size(); i++) {
                        DocumentSnapshot ds = qs.getDocuments().get(i);
                        DocumentReference cRef = ds.getDocumentReference("client");
                        if (cRef != null) clientRefs.add(cRef);
                    }

                    if (clientRefs.isEmpty()) {
                        setLoading(false);
                        adapter.setItems(new ArrayList<>());
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                        binding.rvClients.setVisibility(View.GONE);
                        return;
                    }

                    // Cargar los docs de users
                    fetchClients(new ArrayList<>(clientRefs));
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void fetchClients(List<DocumentReference> refs) {
        List<User> clients = new ArrayList<>();
        final int total = refs.size();
        final int[] done = {0};

        for (DocumentReference ref : refs) {
            ref.get().addOnSuccessListener(ds -> {
                done[0]++;
                User u = ds.toObject(User.class);
                if (u != null) {
                    u.setUid(ds.getId());
                    clients.add(u);
                }
                if (done[0] == total) {
                    onClientsLoaded(clients);
                }
            }).addOnFailureListener(e -> {
                done[0]++;
                if (done[0] == total) {
                    onClientsLoaded(clients);
                }
            });
        }
    }

    private void onClientsLoaded(List<User> list) {
        setLoading(false);
        adapter.setItems(list);
        boolean empty = list == null || list.isEmpty();
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvClients.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClientClick(User client) {
        Intent i = new Intent(requireContext(), ClientDetailActivity.class);
        i.putExtra("clientId", client.getUid());
        i.putExtra("providerId", providerId);
        startActivity(i);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

