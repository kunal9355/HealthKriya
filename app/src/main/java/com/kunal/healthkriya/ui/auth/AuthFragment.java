package com.kunal.healthkriya.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.kunal.healthkriya.R;

public class AuthFragment extends Fragment {

    private AuthViewModel viewModel;

    private EditText edtEmail, edtPassword, edtPhone;
    private View phoneInputLayout;
    private MaterialButton btnPrimary;
    private TextView txtToggle, txtForgot;

    private boolean isLoginMode = true;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtPhone = view.findViewById(R.id.edtPhone);
        phoneInputLayout = view.findViewById(R.id.phoneInputLayout);
        btnPrimary = view.findViewById(R.id.btnPrimary);
        txtToggle = view.findViewById(R.id.txtToggle);
        txtForgot = view.findViewById(R.id.txtForgot);

        if (edtEmail == null || edtPassword == null || btnPrimary == null || txtToggle == null) {
            throw new IllegalStateException("fragment_auth is missing required views");
        }

        updateUI();

        btnPrimary.setOnClickListener(v -> submit());
        txtToggle.setOnClickListener(v -> toggleMode());
        if (txtForgot != null) {
            txtForgot.setOnClickListener(v -> {
                if (!isAdded()) {
                    return;
                }
                NavController navController = NavHostFragment.findNavController(this);
                if (navController.getCurrentDestination() != null
                        && navController.getCurrentDestination().getId() == R.id.authFragment) {
                    navController.navigate(R.id.action_auth_to_forgotPassword);
                }
            });
        }
        View card = view.findViewById(R.id.cardContainer);

        card.setTranslationY(300);
        card.setAlpha(0f);

        card.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        observeState();
    }

    private void submit() {
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(requireContext(), "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLoginMode) {
            viewModel.login(email, pass);
        } else {
            String phone = edtPhone != null ? edtPhone.getText().toString().trim() : "";
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(requireContext(), "Phone is required for sign up", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.register(email, pass, phone);
        }
    }

    private void observeState() {
        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            switch (state) {
                case LOGIN_SUCCESS:
                case REGISTER_SUCCESS:
                    if (!isAdded()) {
                        viewModel.clearAuthState();
                        return;
                    }
                    NavController navController = NavHostFragment.findNavController(this);
                    if (navController.getCurrentDestination() != null
                            && navController.getCurrentDestination().getId() == R.id.authFragment) {
                        navController.navigate(R.id.action_auth_to_home);
                    }
                    viewModel.clearAuthState();
                    break;

                case USER_EXISTS:
                    Toast.makeText(requireContext(),
                            "Account already exists. Please login.",
                            Toast.LENGTH_LONG).show();
                    isLoginMode = true;
                    updateUI();
                    viewModel.clearAuthState();
                    break;

                case ERROR:
                    Toast.makeText(requireContext(),
                            "Authentication failed",
                            Toast.LENGTH_SHORT).show();
                    viewModel.clearAuthState();
                    break;
            }
        });
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;

        if (isLoginMode) {
            animateToLogin();
        } else {
            animateToSignup();
        }
    }
    private void animateToLogin() {

        btnPrimary.setText("Login");
        txtToggle.setText("New user? Create account");

        if (txtForgot != null) {
            txtForgot.setVisibility(View.VISIBLE);
            txtForgot.setAlpha(0f);
            txtForgot.animate().alpha(1f).setDuration(200).start();
        }

        phoneInputLayout.animate()
                .alpha(0f)
                .translationY(50)
                .setDuration(200)
                .withEndAction(() -> phoneInputLayout.setVisibility(View.GONE))
                .start();
    }


    private void animateToSignup() {

        btnPrimary.setText("Sign Up");
        txtToggle.setText("Already have an account? Login");

        if (txtForgot != null) {
            txtForgot.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> txtForgot.setVisibility(View.GONE))
                    .start();
        }

        phoneInputLayout.setVisibility(View.VISIBLE);
        phoneInputLayout.setAlpha(0f);
        phoneInputLayout.setTranslationY(50);

        phoneInputLayout.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void updateUI() {
        if (isLoginMode) {
            btnPrimary.setText("Login");
            txtToggle.setText("New user? Create account");
            if (phoneInputLayout != null) phoneInputLayout.setVisibility(View.GONE);
            if (txtForgot != null) txtForgot.setVisibility(View.VISIBLE);
        } else {
            btnPrimary.setText("Sign Up");
            txtToggle.setText("Already have an account? Login");
            if (phoneInputLayout != null) phoneInputLayout.setVisibility(View.VISIBLE);
            if (txtForgot != null) txtForgot.setVisibility(View.GONE);
        }
    }

}
