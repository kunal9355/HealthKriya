package com.kunal.healthkriya.ui.donation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.donation.DonationEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {

    public interface OnDonationClickListener {
        void onDonationClick(DonationEntity item);
    }

    private final List<DonationEntity> items = new ArrayList<>();
    private final Set<String> bestMatchClientIds = new HashSet<>();
    private final boolean showStatus;
    private final boolean compactWidth;
    private final boolean showSmartMatch;
    private final OnDonationClickListener clickListener;

    public DonationAdapter(boolean showStatus,
                           boolean compactWidth,
                           OnDonationClickListener clickListener) {
        this(showStatus, compactWidth, false, clickListener);
    }

    public DonationAdapter(boolean showStatus,
                           boolean compactWidth,
                           boolean showSmartMatch,
                           OnDonationClickListener clickListener) {
        this.showStatus = showStatus;
        this.compactWidth = compactWidth;
        this.showSmartMatch = showSmartMatch;
        this.clickListener = clickListener;
    }

    public void setBestMatchClientIds(Set<String> highlightedIds) {
        bestMatchClientIds.clear();
        if (highlightedIds != null) {
            bestMatchClientIds.addAll(highlightedIds);
        }
        notifyDataSetChanged();
    }

    public void update(List<DonationEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_card, parent, false);
        if (compactWidth) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            params.width = (int) (parent.getResources().getDisplayMetrics().density * 280);
            view.setLayoutParams(params);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationEntity item = items.get(position);

        holder.txtTitle.setText(DonationUiUtils.buildTitle(item));
        holder.txtType.setText(DonationUiUtils.buildTypeLabel(item));
        holder.txtLocation.setText(DonationUiUtils.buildLocation(item));
        holder.txtTime.setText(DonationUiUtils.buildRelativeTime(item));

        if (item.helpCount > 0) {
            holder.txtHelpCount.setVisibility(View.VISIBLE);
            holder.txtHelpCount.setText(item.helpCount + " ready");
        } else {
            holder.txtHelpCount.setVisibility(View.GONE);
        }

        holder.badgeAction.setText(DonationUiUtils.actionLabel(item.action));
        DonationUiUtils.applyActionChip(holder.badgeAction, item.action);

        if (showStatus) {
            holder.badgeStatus.setVisibility(View.VISIBLE);
            holder.badgeStatus.setText(DonationUiUtils.statusLabel(item.status));
            DonationUiUtils.applyStatusChip(holder.badgeStatus, item.status);
            holder.badgeUrgency.setVisibility(View.GONE);
        } else {
            holder.badgeStatus.setVisibility(View.GONE);
            holder.badgeUrgency.setVisibility(View.VISIBLE);
            holder.badgeUrgency.setText(DonationUiUtils.urgencyLabel(item.urgency));
            DonationUiUtils.applyUrgencyChip(holder.badgeUrgency, item.urgency);
        }

        holder.typeRibbon.setCardBackgroundColor(
                DonationUiUtils.typeSurfaceColor(holder.itemView.getContext(), item.type)
        );
        holder.txtType.setTextColor(
                DonationUiUtils.typeAccentColor(holder.itemView.getContext(), item.type)
        );

        boolean isCritical = DonationEntity.URGENCY_CRITICAL.equals(item.urgency);
        if (!showStatus && isCritical) {
            holder.rootCard.setStrokeWidth(dpToPx(holder.itemView, 1));
            holder.rootCard.setStrokeColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.donation_emergency_stroke)
            );
        } else {
            holder.rootCard.setStrokeWidth(0);
        }

        boolean shouldShowMatch = showSmartMatch
                && item.clientId != null
                && bestMatchClientIds.contains(item.clientId);
        holder.txtSmartMatch.setVisibility(shouldShowMatch ? View.VISIBLE : View.GONE);
        if (shouldShowMatch) {
            DonationUiUtils.applyMatchChip(holder.txtSmartMatch);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDonationClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int dpToPx(View view, int dp) {
        return (int) (dp * view.getResources().getDisplayMetrics().density);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView rootCard;
        MaterialCardView typeRibbon;
        TextView txtTitle;
        TextView txtType;
        TextView badgeAction;
        TextView badgeUrgency;
        TextView txtLocation;
        TextView txtSmartMatch;
        TextView txtTime;
        TextView txtHelpCount;
        TextView badgeStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootCard = (MaterialCardView) itemView;
            typeRibbon = itemView.findViewById(R.id.typeRibbon);
            txtTitle = itemView.findViewById(R.id.txtDonationTitle);
            txtType = itemView.findViewById(R.id.txtDonationType);
            badgeAction = itemView.findViewById(R.id.badgeAction);
            badgeUrgency = itemView.findViewById(R.id.badgeUrgency);
            txtLocation = itemView.findViewById(R.id.txtDonationLocation);
            txtSmartMatch = itemView.findViewById(R.id.txtSmartMatch);
            txtTime = itemView.findViewById(R.id.txtDonationTime);
            txtHelpCount = itemView.findViewById(R.id.txtHelpCount);
            badgeStatus = itemView.findViewById(R.id.badgeStatus);
        }
    }
}
