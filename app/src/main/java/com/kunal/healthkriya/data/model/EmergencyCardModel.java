package com.kunal.healthkriya.data.model;

import java.io.Serializable;

public class EmergencyCardModel implements Serializable {

    private String name = "";
    private String age = "";
    private String bloodGroup = "";
    private String emergencyContact = "";
    private String medicalConditions = "";
    private String allergies = "";
    private String medicines = "";
    private String address = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = valueOrEmpty(name);
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = valueOrEmpty(age);
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = valueOrEmpty(bloodGroup);
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = valueOrEmpty(emergencyContact);
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = valueOrEmpty(medicalConditions);
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = valueOrEmpty(allergies);
    }

    public String getMedicines() {
        return medicines;
    }

    public void setMedicines(String medicines) {
        this.medicines = valueOrEmpty(medicines);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = valueOrEmpty(address);
    }

    public boolean isEmpty() {
        return name.isEmpty()
                && age.isEmpty()
                && bloodGroup.isEmpty()
                && emergencyContact.isEmpty()
                && medicalConditions.isEmpty()
                && allergies.isEmpty()
                && medicines.isEmpty()
                && address.isEmpty();
    }

    public boolean isReadyForEmergency() {
        return !name.isEmpty()
                && !age.isEmpty()
                && !bloodGroup.isEmpty()
                && !emergencyContact.isEmpty();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
