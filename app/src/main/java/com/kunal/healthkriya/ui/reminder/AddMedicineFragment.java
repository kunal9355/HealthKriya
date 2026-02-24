package com.kunal.healthkriya.ui.reminder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.ui.reminder.model.MedicineModel;

import java.util.Calendar;
import java.util.Locale;

public class AddMedicineFragment extends Fragment {
    private static final String TAG = "AddMedicineFragment";

    private TextInputEditText etMedicineName, etDosage;
    private Button btnSelectTime;
    private MaterialButton btnSaveMedicine;

    private int selectedHour = -1;
    private int selectedMinute = -1;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        MedicineReminderFragment.medicineList.add(model);

        ensureNotificationPermission();
        boolean alarmScheduled = scheduleAlarm(name, dosage, time);

        if (alarmScheduled) {
            Toast.makeText(getContext(), "Medicine added. Alarm set 🔔", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(
                    getContext(),
                    "Medicine added, but exact alarm permission is required.",
                    Toast.LENGTH_LONG
            ).show();
        }

        closeScreen();
    }

    private boolean scheduleAlarm(String medicineName, String dosage, String time) {
        Context context = getContext();
        if (context == null) return false;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("medicine_name", medicineName);
        intent.putExtra("medicine_dosage", dosage);
        intent.putExtra("medicine_time", time);

        int requestCode = buildRequestCode(medicineName, selectedHour, selectedMinute);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            requestExactAlarmPermission();
            return false;
        }

        long triggerAt = calendar.getTimeInMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
        return true;
    }

    private void requestExactAlarmPermission() {
        if (!isAdded() || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;

        try {
            Intent intent = new Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + requireContext().getPackageName())
            );
            startActivity(intent);
        } catch (Exception e) {
            Log.w(TAG, "Unable to open exact alarm permission screen", e);
        }
    }

    private void ensureNotificationPermission() {
        if (!isAdded() || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private int buildRequestCode(String name, int hour, int minute) {
        int base = name == null ? 0 : name.hashCode();
        return Math.abs((base * 31) + (hour * 60) + minute);
    }

    private void closeScreen() {
        if (!isAdded()) return;

        View reminderContainer = requireActivity().findViewById(R.id.reminderContainer);
        if (reminderContainer != null) {
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        NavHostFragment.findNavController(this).popBackStack();
    }
}
