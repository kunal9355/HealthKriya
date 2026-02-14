package com.kunal.healthkriya.data.model;

import java.io.Serializable;

public class UserModel implements Serializable {

    private String uid;
    private String email;
    private boolean profileCompleted;
    private long createdAt;

    private String name = "";
    private String phone = "";
    private String age = "";
    private String gender = "";
    private String medicalConditions = "";

    // Required empty constructor for Firestore
    public UserModel() {}

    public UserModel(String uid, String email) {
        this.uid = uid;
        this.email = email;
        this.profileCompleted = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }
}
