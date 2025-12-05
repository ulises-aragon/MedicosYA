package com.aragon.medicosya.ui.client.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.aragon.medicosya.ui.session.LoginActivity;
import com.aragon.medicosya.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadCurrentUserName();

        binding.accountChangeUsername.setOnClickListener(v -> showChangeUsernameDialog());

        binding.logOutBttn.setOnClickListener(v -> signOutAndGoToLogin());
    }

    private void loadCurrentUserName() {
        String uid = getCurrentUid();
        if (uid == null) {
            binding.accountUsername.setText("Usuario");
            return;
        }

        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            binding.accountUsername.setText(name);
                        } else {
                            binding.accountUsername.setText("Usuario");
                        }
                    }
                });
    }

    private void showChangeUsernameDialog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Nuevo nombre");
        CharSequence current = binding.accountUsername.getText();
        if (current != null && !current.toString().equals("Usuario")) {
            input.setText(current.toString());
            input.setSelection(current.length());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Cambiar nombre")
                .setMessage("Ingrese el nuevo nombre que desea mostrar")
                .setView(input)
                .setPositiveButton("Guardar", (d, which) -> {
                    String newName = input.getText() == null ? "" : input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateUserName(newName);
                })
                .setNegativeButton("Cancelar", (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }

    private void updateUserName(String newName) {
        String uid = getCurrentUid();
        if (uid == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.logOutBttn.setEnabled(false);

        firestore.collection("users").document(uid)
                .update("name", newName)
                .addOnSuccessListener(aVoid -> {
                    binding.accountUsername.setText(newName);
                    Toast.makeText(requireContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show();
                    binding.logOutBttn.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    binding.logOutBttn.setEnabled(true);
                });
    }

    private void signOutAndGoToLogin() {
        auth.signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Nullable
    private String getCurrentUid() {
        if (auth == null) auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return null;
        return auth.getCurrentUser().getUid();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
