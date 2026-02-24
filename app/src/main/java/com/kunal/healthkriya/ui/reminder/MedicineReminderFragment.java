package com.kunal.healthkriya.ui.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.reminder.ReminderEntity;
import com.kunal.healthkriya.data.repository.ReminderRepository;

import java.util.ArrayList;
import java.util.List;

public class MedicineReminderFragment extends Fragment {
    private static final String ARG_SHOW_ADD_BUTTON = "arg_show_add_button";

    private RecyclerView recyclerReminder;
    private RecyclerView recyclerWeeklyCalendar;
    private FloatingActionButton fabAddMedicine;
    private TextView tvTodayCount;
    private TextView tvAdherence;

    private final List<ReminderEntity> reminderList = new ArrayList<>();
    private MedicineAdapter adapter;
    private WeeklyCalendarAdapter weeklyCalendarAdapter;
    private ReminderRepository reminderRepository;

    private boolean showAddButton = false;
    private boolean showWeeklySection = false;
    private boolean receiverRegistered = false;

    private final Paint swipePaint = new Paint();
    private Drawable deleteDrawable;

    private final BroadcastReceiver reminderChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadData();
        }
    };

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
        showWeeklySection = showAddButton;
        reminderRepository = new ReminderRepository(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_medicine_reminder, container, false);

        recyclerReminder = view.findViewById(R.id.recyclerReminder);
        recyclerWeeklyCalendar = view.findViewById(R.id.recyclerWeeklyCalendar);
        fabAddMedicine = view.findViewById(R.id.fabAddMedicine);
        tvTodayCount = view.findViewById(R.id.tvTodayCount);
        tvAdherence = view.findViewById(R.id.tvAdherence);

        adapter = new MedicineAdapter(reminderList, this::openEditMedicineScreen);
        recyclerReminder.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerReminder.setAdapter(adapter);

        weeklyCalendarAdapter = new WeeklyCalendarAdapter();
        recyclerWeeklyCalendar.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerWeeklyCalendar.setAdapter(weeklyCalendarAdapter);

        deleteDrawable = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete);
        attachSwipeToDelete();

        if (showAddButton) {
            fabAddMedicine.setVisibility(View.VISIBLE);
            fabAddMedicine.setOnClickListener(v -> openAddMedicineScreen());
        } else {
            fabAddMedicine.setVisibility(View.GONE);
        }

        // Keep today's summary visible; hide weekly adherence label.
        tvAdherence.setVisibility(View.GONE);
        if (showWeeklySection) {
            recyclerWeeklyCalendar.setVisibility(View.VISIBLE);
        } else {
            recyclerWeeklyCalendar.setVisibility(View.GONE);
        }

        loadData();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReminderReceiver();
        loadData();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReminderReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (!isAdded()) {
            return;
        }

        reminderRepository.getReminders(reminders -> {
            if (!isAdded()) return;
            adapter.update(reminders);
        });

        reminderRepository.getTodaySummary(summary -> {
            if (!isAdded()) return;
            String text = "Today: " + summary.total
                    + " • Taken " + summary.taken
                    + " • Missed " + summary.missed
                    + " • Pending " + summary.pending;
            tvTodayCount.setText(text);
        });

        if (showWeeklySection) {
            reminderRepository.getWeeklyOverview(overview -> {
                if (!isAdded()) return;
                weeklyCalendarAdapter.update(overview.days);
            });
        }
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

    private void openEditMedicineScreen(ReminderEntity reminder) {
        if (!isAdded() || reminder == null) {
            return;
        }

        View reminderContainer = requireActivity().findViewById(R.id.reminderContainer);
        if (reminderContainer != null) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.reminderContainer, AddMedicineFragment.newInstanceForEdit(reminder.clientId))
                    .addToBackStack("edit_medicine")
                    .commit();
            return;
        }

        Bundle args = new Bundle();
        args.putString(AddMedicineFragment.ARG_CLIENT_ID, reminder.clientId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_remindersFragment_to_addReminderFragment, args);
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                ReminderEntity reminder = adapter.getItem(position);
                if (reminder == null) {
                    adapter.notifyDataSetChanged();
                    return;
                }

                ReminderEntity snapshot = snapshotReminder(reminder);
                reminderRepository.deleteReminder(reminder.clientId, null);

                Snackbar.make(recyclerReminder, "Reminder deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> reminderRepository.restoreReminder(snapshot, null))
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                loadData();
                            }
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX,
                                    float dY,
                                    int actionState,
                                    boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float progress = Math.min(1f, Math.abs(dX) / itemView.getWidth());
                    int bgColor = blendColors(Color.parseColor("#FDE1E1"), Color.parseColor("#E53935"), progress);
                    swipePaint.setColor(bgColor);

                    if (dX > 0) {
                        c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom(), swipePaint);
                    } else {
                        c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), swipePaint);
                    }

                    if (deleteDrawable != null) {
                        int iconMargin = (itemView.getHeight() - deleteDrawable.getIntrinsicHeight()) / 2;
                        int top = itemView.getTop() + iconMargin;
                        int bottom = top + deleteDrawable.getIntrinsicHeight();

                        if (dX > 0) {
                            int left = itemView.getLeft() + iconMargin;
                            int right = left + deleteDrawable.getIntrinsicWidth();
                            deleteDrawable.setBounds(left, top, right, bottom);
                        } else {
                            int right = itemView.getRight() - iconMargin;
                            int left = right - deleteDrawable.getIntrinsicWidth();
                            deleteDrawable.setBounds(left, top, right, bottom);
                        }
                        deleteDrawable.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerReminder);
    }

    private ReminderEntity snapshotReminder(ReminderEntity reminder) {
        return new ReminderEntity(
                reminder.id,
                reminder.clientId,
                reminder.name,
                reminder.dosage,
                reminder.hour,
                reminder.minute,
                reminder.repeatDaily,
                reminder.triggerAt,
                reminder.active,
                reminder.lastTakenAt,
                reminder.updatedAt,
                reminder.syncStatus,
                reminder.deleted
        );
    }

    private int blendColors(int startColor, int endColor, float ratio) {
        float inverseRatio = 1f - ratio;
        int r = Math.round(Color.red(startColor) * inverseRatio + Color.red(endColor) * ratio);
        int g = Math.round(Color.green(startColor) * inverseRatio + Color.green(endColor) * ratio);
        int b = Math.round(Color.blue(startColor) * inverseRatio + Color.blue(endColor) * ratio);
        return Color.rgb(r, g, b);
    }

    private void registerReminderReceiver() {
        if (receiverRegistered || getContext() == null) {
            return;
        }

        IntentFilter filter = new IntentFilter(ReminderRepository.ACTION_REMINDERS_CHANGED);
        ContextCompat.registerReceiver(
                requireContext(),
                reminderChangedReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
        receiverRegistered = true;
    }

    private void unregisterReminderReceiver() {
        if (!receiverRegistered || getContext() == null) {
            return;
        }

        try {
            requireContext().unregisterReceiver(reminderChangedReceiver);
        } catch (Exception ignored) {
        }
        receiverRegistered = false;
    }
}
