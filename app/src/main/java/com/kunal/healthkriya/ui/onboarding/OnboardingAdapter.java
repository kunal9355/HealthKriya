package com.kunal.healthkriya.ui.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kunal.healthkriya.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageVH> {

    private final List<OnboardingViewModel.Page> pages;

    public OnboardingAdapter(List<OnboardingViewModel.Page> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public PageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);
        return new PageVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageVH holder, int position) {
        OnboardingViewModel.Page p = pages.get(position);
        holder.img.setImageResource(p.imageRes);
        holder.title.setText(p.title);
        holder.desc.setText(p.desc);
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class PageVH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title, desc;

        PageVH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgOnboard);
            title = itemView.findViewById(R.id.txtTitle);
            desc = itemView.findViewById(R.id.txtDesc);
        }
    }
}
