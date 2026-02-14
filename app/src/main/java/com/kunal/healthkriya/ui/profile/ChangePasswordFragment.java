package com.kunal.healthkriya.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.repository.AppRepository;

public class ChangePasswordFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText edtCurrentPassword = view.findViewById(R.id.edtCurrentPassword);
        EditText edtNewPassword = view.findViewById(R.id.edtNewPassword);
        MaterialButton btnChange = view.findViewById(R.id.btnChangePassword);

        btnChange.setOnClickListener(v -> {
            String currentPass = edtCurrentPassword.getText().toString().trim();
            String newPass = edtNewPassword.getText().toString().trim();

            if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass)) {
                Toast.makeText(requireContext(),
                        "Current and new password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(requireContext(),
                        "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            AppRepository.getInstance()
                    .changePassword(currentPass, newPass)
                    .observe(getViewLifecycleOwner(), success -> {
                        if (Boolean.TRUE.equals(success)) {
                            Toast.makeText(requireContext(),
                                    "Password changed", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(this).popBackStack();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Password change failed. Check current password.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
