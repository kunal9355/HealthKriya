package com.kunal.healthkriya;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
            NavigationUI.setupWithNavController(bottomNav, navController);

            bottomNav.setOnItemSelectedListener(item -> {
                View view = bottomNav.findViewById(item.getItemId());
                if (view != null) {
                    try {
                        view.startAnimation(
                                AnimationUtils.loadAnimation(this, R.anim.nav_pulse)
                        );
                    } catch (Exception ignored) {
                    }
                }
                return NavigationUI.onNavDestinationSelected(item, navController);
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                boolean showBottomNav = id == R.id.homeFragment
                        || id == R.id.myHealthFragment
                        || id == R.id.remindersFragment
                        || id == R.id.profileFragment;
                bottomNav.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
            });
        }
    }
}
