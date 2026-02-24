package com.kunal.healthkriya.ui.reminder;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.ui.reminder.model.MedicineModel;

import java.util.Locale;

public class AddMedicineFragment extends Fragment {

    private TextInputEditText etMedicineName, etDosage;
    private Button btnSelectTime;
    private MaterialButton btnSaveMedicine;

    private int selectedHour = -1;
    private int selectedMinute = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_medicine, container, false);

        etMedicineName = view.findViewById(R.id.etMedicineName);
        etDosage = view.findViewById(R.id.etDosage);
        btnSelectTime = view.findViewById(R.id.btnSelectTime);
        btnSaveMedicine = view.findViewById(R.id.btnSaveMedicine);

        btnSelectTime.setOnClickListener(v -> openTimePicker());

        btnSaveMedicine.setOnClickListener(v -> saveMedicine());

        return view;
    }

    private void openTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    btnSelectTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                },
                12, 0, true
        );
        timePickerDialog.show();
    }

    private void saveMedicine() {
        String name = etMedicineName.getText() != null ? etMedicineName.getText().toString().trim() : "";
        String dosage = etDosage.getText() != null ? etDosage.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etMedicineName.setError("Name required");
            return;
        }
        if (dosage.isEmpty()) {
            etDosage.setError("Dosage required");
            return;
        }
        if (selectedHour == -1) {
            Toast.makeText(getContext(), "Please select time", Toast.LENGTH_SHORT).show();
            return;
        }

        String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);

        MedicineModel model = new MedicineModel(name, dosage, time);

        // Ensure medicineList is accessible or use a repository/ViewModel
        MedicineReminderFragment.medicineList.add(model);

        Toast.makeText(getContext(), "Medicine Added", Toast.LENGTH_SHORT).show();

        Navigation.findNavController(requireView()).popBackStack();
    }
}