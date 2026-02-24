package com.kunal.healthkriya.ui.reminder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.reminder.ReminderEntity;
import com.kunal.healthkriya.data.repository.ReminderRepository;

import java.util.Locale;

public class AddMedicineFragment extends Fragment {
    public static final String ARG_CLIENT_ID = "arg_client_id";

    private static final String TAG = "AddMedicineFragment";

    private TextInputEditText etMedicineName;
    private TextInputEditText etDosage;
    private Button btnSelectTime;
    private MaterialButton btnSaveMedicine;
    private RadioGroup radioRepeat;
    private TextView tvFormTitle;

    private int selectedHour = -1;
    private int selectedMinute = -1;

    private String editingClientId;
    private ReminderEntity editingReminder;

    private ReminderRepository reminderRepository;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    public static AddMedicineFragment newInstanceForEdit(String clientId) {
        AddMedicineFragment fragment = new AddMedicineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLIENT_ID, clientId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            editingClientId = args.getString(ARG_CLIENT_ID);
        }

        reminderRepository = new ReminderRepository(requireContext());

        notificationPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                    if (!granted && isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                "Notification permission denied. Reminder may not appear.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_medicine, container, false);

        tvFormTitle = view.findViewById(R.id.tvFormTitle);
        etMedicineName = view.findViewById(R.id.etMedicineName);
        etDosage = view.findViewById(R.id.etDosage);
        btnSelectTime = view.findViewById(R.id.btnSelectTime);
        btnSaveMedicine = view.findViewById(R.id.btnSaveMedicine);
        radioRepeat = view.findViewById(R.id.radioRepeat);

        btnSelectTime.setOnClickListener(v -> openTimePicker());
        btnSaveMedicine.setOnClickListener(v -> saveMedicine());

        setupMode();
        return view;
    }

    private void setupMode() {
        if (isEditMode()) {
            tvFormTitle.setText("Edit Medicine");
            btnSaveMedicine.setText("Update Medicine");
            loadReminderForEdit();
        } else {
            tvFormTitle.setText("Add Medicine");
            btnSaveMedicine.setText("Save Medicine");
        }
    }

    private void loadReminderForEdit() {
        if (editingClientId == null) {
            return;
        }

        reminderRepository.getReminderByClientId(editingClientId, reminder -> {
            if (!isAdded()) {
                return;
            }
            if (reminder == null) {
                Toast.makeText(requireContext(), "Reminder not found", Toast.LENGTH_SHORT).show();
                closeScreen();
                return;
            }

            editingReminder = reminder;
            etMedicineName.setText(reminder.name);
            etDosage.setText(reminder.dosage);
            selectedHour = reminder.hour;
            selectedMinute = reminder.minute;
            btnSelectTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));

            radioRepeat.check(reminder.repeatDaily ? R.id.rbDaily : R.id.rbCustom);
        });
    }

    private void openTimePicker() {
        int initialHour = selectedHour >= 0 ? selectedHour : 12;
        int initialMinute = selectedMinute >= 0 ? selectedMinute : 0;

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    btnSelectTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                },
                initialHour,
                initialMinute,
                true
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
        if (selectedHour < 0 || selectedMinute < 0) {
            Toast.makeText(getContext(), "Please select time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode() && editingReminder == null) {
            Toast.makeText(getContext(), "Wait, loading reminder...", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isDailyRepeat = isDailyRepeatSelected();
        ReminderEntity reminder = buildReminderForSave(name, dosage, isDailyRepeat);

        ensureNotificationPermission();
        AlarmScheduler.cancelReminder(requireContext(), reminder.clientId);
        boolean alarmScheduled = AlarmScheduler.scheduleReminder(requireContext(), reminder);
        if (!alarmScheduled) {
            requestExactAlarmPermissionIfNeeded();
        }

        reminderRepository.saveReminder(reminder, (success, error) -> {
            if (!isAdded()) {
                return;
            }

            if (!success) {
                Toast.makeText(requireContext(), "Failed to save reminder", Toast.LENGTH_SHORT).show();
                return;
            }

            String action = isEditMode() ? "updated" : "added";
            if (alarmScheduled) {
                Toast.makeText(requireContext(), "Medicine " + action + ". Alarm set", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(
                        requireContext(),
                        "Medicine " + action + ". Enable exact alarm permission for precise alerts.",
                        Toast.LENGTH_LONG
                ).show();
            }

            closeScreen();
        });
    }

    private ReminderEntity buildReminderForSave(String name, String dosage, boolean isDailyRepeat) {
        long triggerAt = AlarmScheduler.computeInitialTriggerAt(selectedHour, selectedMinute, isDailyRepeat);

        if (isEditMode()) {
            ReminderEntity reminder = editingReminder;
            reminder.name = name;
            reminder.dosage = dosage;
            reminder.hour = selectedHour;
            reminder.minute = selectedMinute;
            reminder.repeatDaily = isDailyRepeat;
            reminder.triggerAt = triggerAt;
            reminder.active = true;
            reminder.deleted = false;
            return reminder;
        }

        return new ReminderEntity(
                ReminderRepository.newClientId(),
                name,
                dosage,
                selectedHour,
                selectedMinute,
                isDailyRepeat,
                triggerAt
        );
    }

    private boolean isEditMode() {
        return editingClientId != null && !editingClientId.trim().isEmpty();
    }

    private void requestExactAlarmPermissionIfNeeded() {
        if (!isAdded() || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return;
        }

        Context context = requireContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
            return;
        }

        try {
            Intent intent = new Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + context.getPackageName())
            );
            startActivity(intent);
        } catch (Exception e) {
            Log.w(TAG, "Unable to open exact alarm permission screen", e);
        }
    }

    private void ensureNotificationPermission() {
        if (!isAdded() || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private boolean isDailyRepeatSelected() {
        if (radioRepeat == null) {
            return true;
        }
        return radioRepeat.getCheckedRadioButtonId() == R.id.rbDaily;
    }

    private void closeScreen() {
        if (!isAdded()) {
            return;
        }

        View reminderContainer = requireActivity().findViewById(R.id.reminderContainer);
        if (reminderContainer != null) {
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        NavHostFragment.findNavController(this).popBackStack();
    }
}
