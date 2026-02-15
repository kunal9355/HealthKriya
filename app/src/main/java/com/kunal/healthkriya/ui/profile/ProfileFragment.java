package com.kunal.healthkriya.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.kunal.healthkriya.R;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;

    // Header
    private TextView txtName, txtEmail, txtPhone;

    // Rows
    private View rowChangePassword, rowMedical, rowEmergency, rowLogout;
    private Switch themeSwitch;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Header views
        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);

        // Rows
        rowChangePassword = view.findViewById(R.id.rowChangePassword);
        rowMedical = view.findViewById(R.id.rowMedical);
        rowEmergency = view.findViewById(R.id.rowEmergency);
        rowLogout = view.findViewById(R.id.rowLogout);
        themeSwitch = view.findViewById(R.id.switchTheme);

        observeUser();
        setupClicks();
        setupThemeSwitch();
    }

    private void observeUser() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            txtName.setText(user.getName() != null ? user.getName() : "User");
            txtEmail.setText(user.getEmail());
            txtPhone.setText(user.getPhone() != null ? user.getPhone() : "Not added");
        });
    }

    private void setupClicks() {
        rowChangePassword.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_changePassword)
        );

        rowMedical.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.myHealthFragment)
        );

        rowEmergency.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_emergency)
        );

        rowLogout.setOnClickListener(v -> {
            viewModel.logout();
            NavController navController = NavHostFragment.findNavController(this);
            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build();
            navController.navigate(R.id.authFragment, null, options);
        });
    }

    private void setupThemeSwitch() {
        if (getContext() == null) return;

        SharedPreferences prefs =
                requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        boolean isDark = prefs.getBoolean("dark_mode", false);
        themeSwitch.setChecked(isDark);

        themeSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
            prefs.edit().putBoolean("dark_mode", checked).apply();

            AppCompatDelegate.setDefaultNightMode(
                    checked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
    }
}
