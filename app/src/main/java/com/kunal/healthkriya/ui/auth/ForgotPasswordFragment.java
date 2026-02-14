package com.kunal.healthkriya.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.repository.AppRepository;

public class ForgotPasswordFragment extends Fragment {

    private EditText edtEmail, edtPhone;
    private MaterialButton btnVerify;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPhone = view.findViewById(R.id.edtPhone);
        btnVerify = view.findViewById(R.id.btnVerify);

        btnVerify.setOnClickListener(v -> verify());
    }

    private void verify() {
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        AppRepository.getInstance()
                .verifyEmailPhone(email, phone)
                .observe(getViewLifecycleOwner(), match -> {
                    if (Boolean.TRUE.equals(match)) {
                        AppRepository.getInstance()
                                .sendPasswordResetEmail(email,
                                        () -> Toast.makeText(
                                                requireContext(),
                                                "Reset email sent",
                                                Toast.LENGTH_LONG).show(),
                                        () -> Toast.makeText(
                                                requireContext(),
                                                "Failed to send email",
                                                Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        Toast.makeText(
                                requireContext(),
                                "Details not matched. Create new account.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
