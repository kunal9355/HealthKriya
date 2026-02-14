package com.kunal.healthkriya.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.repository.AppRepository;

public class OnboardingFragment extends Fragment {

    private OnboardingViewModel viewModel;
    private ViewPager2 viewPager;
    private MaterialButton btnNext, btnSkip;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_onboarding, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);

        viewPager = view.findViewById(R.id.viewPager);
        btnNext = view.findViewById(R.id.btnNext);
        btnSkip = view.findViewById(R.id.btnSkip);
        TabLayout dots = view.findViewById(R.id.dotsIndicator);

        OnboardingAdapter adapter = new OnboardingAdapter(viewModel.getPages());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(dots, viewPager, (tab, position) -> {}).attach();

        btnSkip.setOnClickListener(v -> goAuth());

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                goAuth();
            }
        });
    }

    private void goAuth() {

        AppRepository.getInstance().setOnboardingSeen();

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_onboardingFragment_to_authFragment);
    }
}
