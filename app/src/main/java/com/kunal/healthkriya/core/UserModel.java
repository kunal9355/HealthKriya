package com.kunal.healthkriya.core;

public class UserModel {

    private final String uid;
    private final String email;

    public UserModel(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }
}
