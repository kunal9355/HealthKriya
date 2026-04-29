package com.kunal.healthkriya.ui.donation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.donation.DonationEntity;
import com.kunal.healthkriya.data.repository.DonationRepository;

public class DonationDetailFragment extends Fragment {
    public static final String ARG_CLIENT_ID = "arg_client_id";

    private DonationRepository repository;
    private String clientId;
    private DonationEntity currentDonation;
    private DonationRepository.HelpSubscription helpSubscription;

    private TextView txtTitle;
    private TextView txtType;
    private TextView badgeAction;
    private TextView badgeStatus;
    private TextView badgeUrgency;
    private TextView txtStatusMessage;
    private TextView txtCity;
    private TextView txtContact;
    private TextView txtDescription;
    private TextView txtUpdated;
    private TextView txtSpecific;
    private TextView txtHelpCount;
    private View ownerActionGroup;
    private View helperActionGroup;
    private MaterialButton btnEditDonation;
    private MaterialButton btnCancelDonation;
    private MaterialButton btnCompleteDonation;
    private MaterialButton btnCallHelper;
    private MaterialButton btnHelpNow;
    private MaterialButton btnReportDonation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donation_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        clientId = getArguments() != null ? getArguments().getString(ARG_CLIENT_ID) : null;
        repository = new DonationRepository(requireContext());

        txtTitle = view.findViewById(R.id.txtDetailTitle);
        txtType = view.findViewById(R.id.txtDetailType);
        badgeAction = view.findViewById(R.id.badgeDetailAction);
        badgeStatus = view.findViewById(R.id.badgeDetailStatus);
        badgeUrgency = view.findViewById(R.id.badgeDetailUrgency);
        txtStatusMessage = view.findViewById(R.id.txtStatusMessage);
        txtCity = view.findViewById(R.id.txtDetailCity);
        txtContact = view.findViewById(R.id.txtDetailContact);
        txtDescription = view.findViewById(R.id.txtDetailDescription);
        txtUpdated = view.findViewById(R.id.txtDetailUpdated);
        txtSpecific = view.findViewById(R.id.txtDetailSpecific);
        txtHelpCount = view.findViewById(R.id.txtHelpCount);
        ownerActionGroup = view.findViewById(R.id.ownerActionGroup);
        helperActionGroup = view.findViewById(R.id.helperActionGroup);
        btnEditDonation = view.findViewById(R.id.btnEditDonation);
        btnCancelDonation = view.findViewById(R.id.btnCancelDonation);
        btnCompleteDonation = view.findViewById(R.id.btnCompleteDonation);
        btnCallHelper = view.findViewById(R.id.btnCallHelper);
        btnHelpNow = view.findViewById(R.id.btnHelpNow);
        btnReportDonation = view.findViewById(R.id.btnReportDonation);

        if (clientId == null || clientId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Donation not found", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        repository.getDonation(clientId).observe(getViewLifecycleOwner(), item -> {
            if (item == null) {
                return;
            }
            currentDonation = item;
            render(item);
        });

        repository.startRealtimeSync();
    }

    @Override
    public void onDestroyView() {
        if (helpSubscription != null) {
            helpSubscription.cancel();
            helpSubscription = null;
        }
        if (repository != null) {
            repository.stopRealtimeSync();
        }
        super.onDestroyView();
    }

    private void render(DonationEntity item) {
        txtTitle.setText(DonationUiUtils.buildTitle(item));
        txtType.setText(DonationUiUtils.buildTypeLabel(item));
        txtType.setTextColor(DonationUiUtils.typeAccentColor(requireContext(), item.type));

        badgeAction.setText(DonationUiUtils.actionLabel(item.action));
        DonationUiUtils.applyActionChip(badgeAction, item.action);

        badgeStatus.setText(DonationUiUtils.statusLabel(item.status));
        DonationUiUtils.applyStatusChip(badgeStatus, item.status);

        badgeUrgency.setText(DonationUiUtils.urgencyLabel(item.urgency));
        DonationUiUtils.applyUrgencyChip(badgeUrgency, item.urgency);

        txtCity.setText("Location: " + DonationUiUtils.buildLocation(item));
        txtContact.setText(buildContactLine(item));
        txtDescription.setText(DonationUiUtils.hasText(item.description)
                ? item.description.trim()
                : "No extra notes added");
        txtUpdated.setText("Updated " + DonationUiUtils.buildRelativeTime(item));
        txtSpecific.setText(buildSpecificLine(item));

        String currentUserId = repository.getCurrentUserId();
        boolean isOwner = currentUserId != null && currentUserId.equals(item.userId);
        boolean isRequest = DonationEntity.ACTION_REQUEST.equals(item.action);
        boolean isCancelled = DonationEntity.STATUS_CANCELLED.equals(item.status);
        boolean isCompleted = DonationEntity.STATUS_COMPLETED.equals(item.status);
        boolean isExpired = DonationEntity.STATUS_EXPIRED.equals(item.status);
        boolean isActive = DonationEntity.STATUS_ACTIVE.equals(item.status)
                || DonationEntity.STATUS_IN_PROGRESS.equals(item.status);

        bindStatusState(isCancelled, isCompleted, isExpired);
        bindActionGroups(item, isOwner, isRequest, isActive);
        bindHelpSummary(item, isOwner, isRequest && isActive);
    }

    private void bindStatusState(boolean isCancelled, boolean isCompleted, boolean isExpired) {
        if (isCancelled) {
            txtStatusMessage.setVisibility(View.VISIBLE);
            txtStatusMessage.setText("This request is cancelled.");
            return;
        }
        if (isCompleted) {
            txtStatusMessage.setVisibility(View.VISIBLE);
            txtStatusMessage.setText("Help completed.");
            return;
        }
        if (isExpired) {
            txtStatusMessage.setVisibility(View.VISIBLE);
            txtStatusMessage.setText("This request expired due to inactivity.");
            return;
        }
        txtStatusMessage.setVisibility(View.GONE);
    }

    private void bindActionGroups(DonationEntity item, boolean isOwner, boolean isRequest, boolean isActive) {
        if (!isActive) {
            ownerActionGroup.setVisibility(View.GONE);
            helperActionGroup.setVisibility(View.GONE);
            btnHelpNow.setEnabled(false);
            return;
        }

        if (isOwner) {
            ownerActionGroup.setVisibility(View.VISIBLE);
            helperActionGroup.setVisibility(View.GONE);
            btnEditDonation.setOnClickListener(v -> openEditForm(item.clientId));
            btnCancelDonation.setOnClickListener(v ->
                    showConfirmDialog("Cancel this request?", () -> cancelDonation(item.clientId)));
            btnCompleteDonation.setOnClickListener(v ->
                    showConfirmDialog("Mark this as completed?", () -> completeDonation(item.clientId)));
            return;
        }

        ownerActionGroup.setVisibility(View.GONE);
        helperActionGroup.setVisibility(View.VISIBLE);
        btnCallHelper.setOnClickListener(v -> callContact(item.contact));
        btnReportDonation.setOnClickListener(v -> reportDonation(item.clientId));

        if (isRequest) {
            btnHelpNow.setVisibility(View.VISIBLE);
            btnHelpNow.setEnabled(true);
            btnHelpNow.setText("I Will Help");
            btnHelpNow.setOnClickListener(v -> requestHelp(item.clientId));
        } else {
            btnHelpNow.setVisibility(View.GONE);
        }
    }

    private void bindHelpSummary(DonationEntity item, boolean isOwner, boolean isRequest) {
        if (!isRequest) {
            txtHelpCount.setVisibility(View.GONE);
            if (helpSubscription != null) {
                helpSubscription.cancel();
                helpSubscription = null;
            }
            return;
        }

        txtHelpCount.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        if (helpSubscription != null) {
            helpSubscription.cancel();
            helpSubscription = null;
        }

        helpSubscription = repository.listenToHelpSummary(item.clientId, summary -> {
            if (!isAdded() || summary == null) {
                return;
            }
            updateHelpSummaryUi(summary, isOwner);
        });

        repository.fetchHelpSummary(item.clientId, (success, summary, error) -> {
            if (!isAdded() || summary == null) {
                return;
            }
            updateHelpSummaryUi(summary, isOwner);
        });
    }

    private void updateHelpSummaryUi(DonationRepository.HelpSummary summary, boolean isOwner) {
        int helpers = summary.getReadyHelpers();
        if (isOwner) {
            txtHelpCount.setVisibility(View.VISIBLE);
            txtHelpCount.setText(helpers + " people ready to help");
        }

        if (helperActionGroup.getVisibility() == View.VISIBLE && btnHelpNow.getVisibility() == View.VISIBLE) {
            boolean alreadyRequested = summary.isCurrentUserRequested();
            btnHelpNow.setEnabled(!alreadyRequested);
            btnHelpNow.setText(alreadyRequested ? "Helping Requested" : "I Will Help");
        }
    }

    private void openEditForm(String donationClientId) {
        Bundle args = new Bundle();
        args.putString(DonationFormFragment.ARG_CLIENT_ID, donationClientId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_donationDetailFragment_to_donationFormFragment, args);
    }

    private void cancelDonation(String donationClientId) {
        repository.cancelDonation(donationClientId, (success, error) -> {
            if (!isAdded()) {
                return;
            }
            if (success) {
                Toast.makeText(requireContext(), "Request cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Unable to cancel right now", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void completeDonation(String donationClientId) {
        repository.markDonationCompleted(donationClientId, (success, error) -> {
            if (!isAdded()) {
                return;
            }
            if (success) {
                Toast.makeText(requireContext(), "Marked as completed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Unable to complete right now", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestHelp(String requestId) {
        btnHelpNow.setEnabled(false);
        repository.requestHelp(requestId, (success, error) -> {
            if (!isAdded()) {
                return;
            }
            if (success) {
                btnHelpNow.setText("Helping Requested");
                Toast.makeText(requireContext(), "Help request sent", Toast.LENGTH_SHORT).show();
            } else {
                btnHelpNow.setEnabled(true);
                String message = error != null && error.getMessage() != null
                        ? error.getMessage()
                        : "Unable to request help right now";
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reportDonation(String requestId) {
        showConfirmDialog("Report this request?", () ->
                repository.reportDonation(requestId, "suspicious_request", (success, error) -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (success) {
                        Toast.makeText(requireContext(), "Report submitted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Unable to report right now", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void callContact(String contact) {
        if (!DonationUiUtils.hasText(contact)) {
            Toast.makeText(requireContext(), "Contact not available", Toast.LENGTH_SHORT).show();
            return;
        }
        String sanitized = contact.replace(" ", "");
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + sanitized));
        startActivity(intent);
    }

    private void showConfirmDialog(String title, Runnable onConfirm) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage("You can still view it in your history.")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialog, which) -> onConfirm.run())
                .show();
    }

    private String buildContactLine(DonationEntity item) {
        String name = DonationUiUtils.hasText(item.name) ? item.name.trim() : "";
        String contact = DonationUiUtils.hasText(item.contact) ? item.contact.trim() : "";
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(contact)) {
            return name + " • " + contact;
        }
        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        if (!TextUtils.isEmpty(contact)) {
            return contact;
        }
        return "Contact not added";
    }

    private String buildSpecificLine(DonationEntity item) {
        if (DonationEntity.TYPE_BLOOD.equals(item.type)) {
            return DonationUiUtils.hasText(item.bloodGroup)
                    ? "Blood group: " + item.bloodGroup.trim()
                    : "Blood group not added";
        }
        if (DonationUiUtils.hasText(item.medicineName) && DonationUiUtils.hasText(item.medicineExpiry)) {
            return item.medicineName.trim() + " • Expiry " + item.medicineExpiry.trim();
        }
        if (DonationUiUtils.hasText(item.medicineName)) {
            return item.medicineName.trim();
        }
        return "Medicine details not added";
    }
}
