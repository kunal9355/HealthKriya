package com.kunal.healthkriya.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.model.home.HomeDataModel;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    private TextView txtGreeting;
    private CardView cardMood, cardMedicine, cardDonate, cardEmergency;

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

        observeData();
        setupClicks();
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

        cardMood.setOnClickListener(v ->
                // 🔮 Future navigation
                // NavHostFragment.findNavController(this)
                //        .navigate(R.id.action_home_to_mood);
                showTemp("Mood Tracker")
        );

        cardMedicine.setOnClickListener(v ->
                showTemp("Medicine")
        );

        cardDonate.setOnClickListener(v ->
                showTemp("Donate / Request")
        );

        cardEmergency.setOnClickListener(v ->
                showTemp("Emergency")
        );
    }

    private void showTemp(String msg) {
        android.widget.Toast.makeText(requireContext(),
                msg + " clicked", android.widget.Toast.LENGTH_SHORT).show();
    }
}
