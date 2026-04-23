package com.kunal.healthkriya.data.model.donation;

public class HelpInterest {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACCEPTED = "accepted";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_CANCELLED = "cancelled";

    private String requestId;
    private String helperId;
    private String status;
    private long createdAt;
    private long updatedAt;

    public HelpInterest() {
        long now = System.currentTimeMillis();
        this.requestId = "";
        this.helperId = "";
        this.status = STATUS_PENDING;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public HelpInterest(String requestId, String helperId, String status) {
        this();
        this.requestId = requestId;
        this.helperId = helperId;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getHelperId() {
        return helperId;
    }

    public void setHelperId(String helperId) {
        this.helperId = helperId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
