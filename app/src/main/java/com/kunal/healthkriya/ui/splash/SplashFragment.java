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
    private final Runnable navigateRunnable = this::navigateNext;

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

        handler.postDelayed(navigateRunnable, 1500);
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(navigateRunnable);
        super.onDestroyView();
    }

    private void navigateNext() {
        if (!isAdded()) {
            return;
        }

        NavController navController = NavHostFragment.findNavController(this);
        if (navController.getCurrentDestination() == null
                || navController.getCurrentDestination().getId() != R.id.splashFragment) {
            return;
        }

        SplashViewModel.Destination dest = viewModel.decideNext();

        if (dest == SplashViewModel.Destination.HOME) {
            navController.navigate(R.id.action_splashFragment_to_homeFragment);
        } else if (dest == SplashViewModel.Destination.AUTH) {
            navController.navigate(R.id.action_splashFragment_to_authFragment);
        } else {
            navController.navigate(R.id.action_splashFragment_to_onboardingFragment);
        }
    }

    private void startLogoAnimation(@NonNull View rootView) {
        View logo = rootView.findViewById(R.id.imgLogo);
        if (logo == null) return;

        float travelY = 18f * rootView.getResources().getDisplayMetrics().density;
        logo.setAlpha(0f);
        logo.setScaleX(0.92f);
        logo.setScaleY(0.92f);
        logo.setTranslationY(travelY);
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(900L)
                .setStartDelay(80L)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
