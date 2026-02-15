package com.kunal.healthkriya.ui.mood.entry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kunal.healthkriya.R;

public class EntryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_entry, container, false);

        Button btnSave = view.findViewById(R.id.btnSaveMood);
        btnSave.setOnClickListener(v ->
                Toast.makeText(getContext(), "Mood Saved (Dummy)", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}
