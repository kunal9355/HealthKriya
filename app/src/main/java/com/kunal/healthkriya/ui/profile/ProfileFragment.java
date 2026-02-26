package com.kunal.healthkriya.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtPhone;
    private Switch themeSwitch;

    private View rowChangePassword;
    private View rowMedical;
    private View rowCareActivity;
    private View rowHealthRecords;
    private View rowEmergency;
    private View rowLogout;

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

        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        themeSwitch = view.findViewById(R.id.switchTheme);

        rowChangePassword = view.findViewById(R.id.rowChangePassword);
        rowMedical = view.findViewById(R.id.rowMedical);
        rowCareActivity = view.findViewById(R.id.rowCareActivity);
        rowHealthRecords = view.findViewById(R.id.rowHealthRecords);
        rowEmergency = view.findViewById(R.id.rowEmergency);
        rowLogout = view.findViewById(R.id.rowLogout);

        setupRowLabels();
        observeUser();
        setupClicks();
        setupThemeSwitch();
    }

    private void setupRowLabels() {
        configureSettingRow(
                rowChangePassword,
                R.drawable.ic_settings,
                "Change password",
                "Update your account security"
        );
        configureSettingRow(
                rowMedical,
                R.drawable.ic_health,
                "Health form",
                "Update your basic health profile"
        );
        configureSettingRow(
                rowCareActivity,
                R.drawable.ic_donate,
                "Care Activity",
                "View blood and medicine donate/request entries"
        );
        configureSettingRow(
                rowHealthRecords,
                R.drawable.ic_medical,
                "Health Records",
                "View ongoing medicines and conditions"
        );
        configureSettingRow(
                rowEmergency,
                R.drawable.ic_emergency,
                "Emergency details",
                "Manage SOS and emergency info"
        );
    }

    private void observeUser() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                return;
            }

            txtName.setText(user.getName() != null ? user.getName().trim() : "User");
            txtEmail.setText(user.getEmail());
            txtPhone.setText(user.getPhone() != null && !user.getPhone().trim().isEmpty()
                    ? user.getPhone().trim()
                    : "Not added");
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

        rowCareActivity.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_careActivity)
        );

        rowHealthRecords.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_healthRecords)
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
        if (getContext() == null) {
            return;
        }

        SharedPreferences prefs =
                requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        boolean isDark = prefs.getBoolean("dark_mode", false);
        themeSwitch.setChecked(isDark);

        themeSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
            prefs.edit().putBoolean("dark_mode", checked).apply();

            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
    }

    private void configureSettingRow(View row, int iconRes, String title, String subtitle) {
        ImageView icon = row.findViewById(R.id.icon);
        TextView txtTitle = row.findViewById(R.id.title);
        TextView txtSubtitle = row.findViewById(R.id.subtitle);

        icon.setImageResource(iconRes);
        txtTitle.setText(title);
        txtSubtitle.setText(subtitle);
    }
}
