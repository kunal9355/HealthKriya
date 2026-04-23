package com.kunal.healthkriya.ui.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.kunal.healthkriya.R;

public class SplashFragment extends Fragment {

    private SplashViewModel viewModel;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isNavigated = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        startLogoAnimation(view);

        // Delay navigation slightly to show splash animation
        handler.postDelayed(this::navigateNext, 1800);
    }

    private void navigateNext() {
        if (!isAdded() || isNavigated) return;

        NavController navController = NavHostFragment.findNavController(this);
        if (navController.getCurrentDestination() == null
                || navController.getCurrentDestination().getId() != R.id.splashFragment) {
            return;
        }

        viewModel.decideNext(dest -> {
            if (!isAdded() || isNavigated) return;
            isNavigated = true;

            NavController currentController = NavHostFragment.findNavController(this);
            if (dest == SplashViewModel.Destination.HOME) {
                currentController.navigate(R.id.action_splashFragment_to_homeFragment);
            } else if (dest == SplashViewModel.Destination.AUTH) {
                currentController.navigate(R.id.action_splashFragment_to_authFragment);
            } else {
                currentController.navigate(R.id.action_splashFragment_to_onboardingFragment);
            }
        });
    }

    private void startLogoAnimation(@NonNull View rootView) {
        View logo = rootView.findViewById(R.id.imgLogo);
        if (logo == null) return;

        logo.setAlpha(0f);
        logo.setScaleX(0.85f);
        logo.setScaleY(0.85f);
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000L)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}
