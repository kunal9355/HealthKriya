package com.kunal.healthkriya.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.model.home.HomeDataModel;
import com.kunal.healthkriya.ui.mood.MoodContainerActivity;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    private TextView txtGreeting;
    private View cardMood, cardMedicine, cardDonate, cardEmergency;
    private ScrollView homeScroll;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        txtGreeting = view.findViewById(R.id.txtGreeting);
        cardMood = view.findViewById(R.id.cardMood);
        cardMedicine = view.findViewById(R.id.cardMedicine);
        cardDonate = view.findViewById(R.id.cardDonate);
        cardEmergency = view.findViewById(R.id.cardEmergency);
        homeScroll = view.findViewById(R.id.homeScroll);

        observeData();
        setupClicks();
        setupScrollListener();
        
        // Attach animations
        attachCardAnimation(cardMood);
        attachCardAnimation(cardMedicine);
        attachCardAnimation(cardDonate);
        attachCardAnimation(cardEmergency);
    }

    private void observeData() {
        viewModel.getHomeData().observe(getViewLifecycleOwner(), this::bindData);
    }

    private void bindData(HomeDataModel data) {
        if (data == null) return;

        String name = data.getUser() != null ? data.getUser().getName() : "User";
        txtGreeting.setText("Hi, " + name + " 👋");
    }

    private void setupClicks() {
        cardMood.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MoodContainerActivity.class);
            startActivity(intent);
        });
        cardMedicine.setOnClickListener(v -> showTemp("Medicine"));
        cardDonate.setOnClickListener(v -> showTemp("Donate / Request"));
        cardEmergency.setOnClickListener(v -> showTemp("Emergency"));
    }

    private void setupScrollListener() {
        if (homeScroll == null) return;

        View bottomNavContainer = getActivity() != null ? getActivity().findViewById(R.id.bottomNavContainer) : null;
        if (bottomNavContainer == null) return;

        homeScroll.setOnScrollChangeListener((v, scrollX, scrollY, oldX, oldY) -> {
            if (scrollY > oldY + 10) { // Scrolling down
                bottomNavContainer.animate()
                        .translationY(bottomNavContainer.getHeight() + 100)
                        .setDuration(300)
                        .start();
            } else if (scrollY < oldY - 10) { // Scrolling up
                bottomNavContainer.animate()
                        .translationY(0)
                        .setDuration(300)
                        .start();
            }
        });
    }

    private void showTemp(String msg) {
        Toast.makeText(requireContext(), msg + " clicked", Toast.LENGTH_SHORT).show();
    }

    private void attachCardAnimation(View card) {
        if (card == null) return;
        card.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.card_press));
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.card_release));
            }
            return false;
        });
    }
}
