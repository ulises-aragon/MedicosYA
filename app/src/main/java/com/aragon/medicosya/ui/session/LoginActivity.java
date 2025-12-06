package com.aragon.medicosya.ui.session;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.aragon.medicosya.databinding.ActivityLoginBinding;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.aragon.medicosya.enums.AuthField;
import com.aragon.medicosya.enums.UserRole;
import com.aragon.medicosya.models.User;
import com.aragon.medicosya.ui.client.ClientMainActivity;
import com.aragon.medicosya.ui.provider.ProviderMainActivity;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel;
    private ActivityLoginBinding binding;

    private SharedPreferences prefs;
    public static final String PREFS_NAME = "medicosya_prefs";
    public static final String KEY_REMEMBER = "remember_session";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("LoginActivity", "Creando vista...");
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rememberSession.setChecked(prefs.getBoolean(KEY_REMEMBER, false));

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.loginLoading.setVisibility(View.VISIBLE);
                binding.loginInteraction.setVisibility(View.INVISIBLE);
                binding.loginBttn.setEnabled(false);
            } else {
                binding.loginLoading.setVisibility(View.GONE);
                binding.loginInteraction.setVisibility(View.VISIBLE);
                binding.loginBttn.setEnabled(true);
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
                onLoginSuccess(user);
            }
        });

        boolean remember = prefs.getBoolean(KEY_REMEMBER, false);
        if (remember) {
            Log.i("LoginActivity", "Intentando iniciar sesion automaticamente...");
            viewModel.loadCurrentUserIfExists();
        }

        binding.loginBttn.setOnClickListener(v -> {
            clearFieldErrors();

            String email = binding.emailField.getText().toString().trim();
            String password = binding.passwordField.getText().toString();

            prefs.edit().putBoolean(KEY_REMEMBER, binding.rememberSession.isChecked()).apply();

            viewModel.attemptLogin(email, password);
        });

        binding.registerBttn.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void applyFieldErrors(Map<AuthField, String> errors) {
        String emailErr = errors != null ? errors.get(AuthField.EMAIL) : null;
        binding.emailField.setError(emailErr);

        String passErr = errors != null ? errors.get(AuthField.PASSWORD) : null;
        binding.passwordField.setError(passErr);
    }

    private void clearFieldErrors() {
        binding.emailField.setError(null);
        binding.passwordField.setError(null);
    }

    private void onLoginSuccess(User user) {
        UserRole role = user.getRoleEnum();
        Intent intent;
        if (role == UserRole.PROVIDER) {
            intent = new Intent(this, ProviderMainActivity.class);
        } else if (role == UserRole.ADMIN) {
            //intent = new Intent(this, AdminMainActivity.class);
            intent = new Intent(this, ClientMainActivity.class);
        } else {
            intent = new Intent(this, ClientMainActivity.class);
        }

        Log.i("LoginActivity", "User ID: " + user.getUid());

        intent.putExtra("userId", user.getUid());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}