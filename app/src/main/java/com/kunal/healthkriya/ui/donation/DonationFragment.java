package com.kunal.healthkriya.ui.donation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.kunal.healthkriya.R;

public class DonationFragment extends Fragment {
    public static final String ARG_MODE = "arg_mode";
    public static final String MODE_BLOOD = "blood";
    public static final String MODE_MEDICINE = "medicine";
    private boolean isNavigating;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_donation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View cardBlood = view.findViewById(R.id.cardBlood);
        View cardMedicine = view.findViewById(R.id.cardMedicine);

        View layoutBloodOptions = view.findViewById(R.id.layoutBloodOptions);
        View layoutMedicineOptions = view.findViewById(R.id.layoutMedicineOptions);

        View tileBloodDonate = view.findViewById(R.id.tileBloodDonate);
        View tileBloodRequest = view.findViewById(R.id.tileBloodRequest);
        View tileMedicineDonate = view.findViewById(R.id.tileMedicineDonate);
        View tileMedicineRequest = view.findViewById(R.id.tileMedicineRequest);

        cardBlood.setOnClickListener(v -> toggleOptionLayout(layoutBloodOptions, layoutMedicineOptions));
        cardMedicine.setOnClickListener(v -> toggleOptionLayout(layoutMedicineOptions, layoutBloodOptions));

        tileBloodDonate.setOnClickListener(v -> animateTileAndNavigate(v, R.id.bloodDonateFragment));
        tileBloodRequest.setOnClickListener(v -> animateTileAndNavigate(v, R.id.bloodRequestFragment));
        tileMedicineDonate.setOnClickListener(v -> animateTileAndNavigate(v, R.id.medicineDonateFragment));
        tileMedicineRequest.setOnClickListener(v -> animateTileAndNavigate(v, R.id.medicineRequestFragment));
    }

    private void toggleOptionLayout(View target, View other) {
        if (other.getVisibility() == View.VISIBLE) {
            hideWithSlide(other);
        }

        if (target.getVisibility() == View.VISIBLE) {
            hideWithSlide(target);
        } else {
            showWithSlide(target);
        }
    }

    private void showWithSlide(View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.setTranslationY(-24f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(240)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void hideWithSlide(View view) {
        view.animate()
                .alpha(0f)
                .translationY(-24f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    view.setAlpha(1f);
                    view.setTranslationY(0f);
                })
                .start();
    }

    private void animateTileAndNavigate(View tile, int destinationId) {
        if (isNavigating) {
            return;
        }
        isNavigating = true;

        tile.animate().cancel();
        tile.animate()
                .scaleX(0.93f)
                .scaleY(0.93f)
                .setDuration(70)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> tile.animate()
                        .scaleX(1.03f)
                        .scaleY(1.03f)
                        .setDuration(110)
                        .setInterpolator(new OvershootInterpolator(1.3f))
                        .withEndAction(() -> tile.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .withEndAction(() -> {
                                    try {
                                        NavHostFragment.findNavController(this).navigate(destinationId);
                                    } finally {
                                        isNavigating = false;
                                    }
                                })
                                .start())
                        .start())
                .start();
    }
}
