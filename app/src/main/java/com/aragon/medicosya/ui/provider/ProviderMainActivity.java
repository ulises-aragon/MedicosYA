package com.aragon.medicosya.ui.provider;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aragon.medicosya.R;
import com.aragon.medicosya.databinding.ActivityProviderMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProviderMainActivity extends AppCompatActivity {

    private ActivityProviderMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProviderMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.i("ProviderMainActivity", "Creating activity...");

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_provider_appointments, R.id.navigation_provider_clients, R.id.navigation_provider_availability, R.id.navigation_provider_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Log.i("ProviderMainActivity", "Loaded activity!");
    }
}