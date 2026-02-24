package com.kunal.healthkriya.ui.reminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.ui.reminder.model.MedicineModel;

import java.util.ArrayList;
import java.util.List;

public class MedicineReminderFragment extends Fragment {
    private static final String ARG_SHOW_ADD_BUTTON = "arg_show_add_button";

    private RecyclerView recyclerReminder;
    private FloatingActionButton fabAddMedicine;
    // Changed to public so AddMedicineFragment can access it directly as per existing logic
    public static List<MedicineModel> medicineList = new ArrayList<>();
    private MedicineAdapter adapter;
    private boolean showAddButton = false;

    public static MedicineReminderFragment newInstance(boolean showAddButton) {
        MedicineReminderFragment fragment = new MedicineReminderFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_ADD_BUTTON, showAddButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            showAddButton = args.getBoolean(ARG_SHOW_ADD_BUTTON, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_medicine_reminder, container, false);

        recyclerReminder = view.findViewById(R.id.recyclerReminder);
        fabAddMedicine = view.findViewById(R.id.fabAddMedicine);
        
        adapter = new MedicineAdapter(medicineList);
        recyclerReminder.setAdapter(adapter);

        if (recyclerReminder != null) {
            recyclerReminder.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        if (fabAddMedicine != null) {
            if (showAddButton) {
                fabAddMedicine.setVisibility(View.VISIBLE);
                fabAddMedicine.setOnClickListener(v -> openAddMedicineScreen());
            } else {
                fabAddMedicine.setVisibility(View.GONE);
            }
        }

        return view;
    }

    private void openAddMedicineScreen() {
        if (!isAdded()) return;

        View reminderContainer = requireActivity().findViewById(R.id.reminderContainer);
        if (reminderContainer != null) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.reminderContainer, new AddMedicineFragment())
                    .addToBackStack("add_medicine")
                    .commit();
            return;
        }

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_remindersFragment_to_addReminderFragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
