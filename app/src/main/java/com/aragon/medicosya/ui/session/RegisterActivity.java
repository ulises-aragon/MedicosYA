package com.aragon.medicosya.ui.session;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.aragon.medicosya.databinding.ActivityRegisterBinding;
import com.aragon.medicosya.enums.AuthField;
import com.aragon.medicosya.enums.UserRole;
import com.aragon.medicosya.models.User;
import com.aragon.medicosya.ui.client.ClientMainActivity;
import com.aragon.medicosya.util.ViewModelFactory;

import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.registerLoading.setVisibility(View.VISIBLE);
                binding.registerBttn.setVisibility(View.GONE);
                binding.registerBttn.setEnabled(false);
            } else {
                binding.registerLoading.setVisibility(View.GONE);
                binding.registerBttn.setVisibility(View.VISIBLE);
                binding.registerBttn.setEnabled(true);
            }
        });

        viewModel.getToastError().observe(this, event -> {
            if (event == null) return;
            String msg = event.getContentIfNotHandled();
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getFieldErrors().observe(this, this::applyFieldErrors);

        viewModel.getNavigateEvent().observe(this, event -> {
            if (event == null) return;
            User user = event.getContentIfNotHandled();
            if (user != null) {
                onRegisterSuccess(user);
            }
        });

        binding.registerBttn.setOnClickListener(v -> {
            clearFieldErrors();
            String name = binding.nameField.getText() == null ? "" : binding.nameField.getText().toString().trim();
            String email = binding.emailField.getText() == null ? "" : binding.emailField.getText().toString().trim();
            String password = binding.passwordField.getText() == null ? "" : binding.passwordField.getText().toString();
            String confirm = binding.confirmPasswordField.getText() == null ? "" : binding.confirmPasswordField.getText().toString();

            viewModel.attemptRegister(name, email, password, confirm);
        });

        binding.signInBttn.setOnClickListener(v -> {
            finish();
        });
    }

    private void applyFieldErrors(Map<AuthField, String> errors) {
        String nameErr = errors != null ? errors.get(AuthField.NAME) : null;
        if (nameErr != null) binding.nameField.setError(nameErr);
        else binding.nameField.setError(null);

        String emailErr = errors != null ? errors.get(AuthField.EMAIL) : null;
        if (emailErr != null) binding.emailField.setError(emailErr);
        else binding.emailField.setError(null);

        String passErr = errors != null ? errors.get(AuthField.PASSWORD) : null;
        if (passErr != null) binding.passwordField.setError(passErr);
        else binding.passwordField.setError(null);

        String confErr = errors != null ? errors.get(AuthField.CONFIRM_PASSWORD) : null;
        if (confErr != null) binding.confirmPasswordField.setError(confErr);
        else binding.confirmPasswordField.setError(null);
    }

    private void clearFieldErrors() {
        binding.nameField.setError(null);
        binding.emailField.setError(null);
        binding.passwordField.setError(null);
        binding.confirmPasswordField.setError(null);
    }

    private void onRegisterSuccess(User user) {
        Intent intent = new Intent(this, ClientMainActivity.class);
        startActivity(intent);
        finish();
    }
}