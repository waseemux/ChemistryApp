package com.chemistry.android;

public class Prospect {
    String phoneNumber;
    String receivedChemistry;
    long receivedChemistryTime;
    long addedTime;
    long updatedTime;

    // Constructor → for calls to DataSnapshot.getValue(Prospect.class)
    public Prospect() {}

    // Constructor
    public Prospect(String phoneNumber, String receivedChemistry, long receivedChemistryTime, long addedTime, long updatedTime) {
        this.phoneNumber = phoneNumber;
        this.receivedChemistry = receivedChemistry;
        this.receivedChemistryTime = receivedChemistryTime;
        this.addedTime = addedTime;
        this.updatedTime = updatedTime;
    }

    /* ---------- GETTERS ---------- */

    // Get phone number
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Get received chemistry
    public String getReceivedChemistry() {
        return receivedChemistry;
    }

    // Get received chemistry time
    public long getReceivedChemistryTime() {
        return receivedChemistryTime;
    }

    // Get added time
    public long getAddedTime() {
        return addedTime;
    }

    // Get updated time
    public long getUpdatedTime() {
        return updatedTime;
    }

    /* ---------- SETTERS ---------- */

    // Set phone number
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Set received chemistry
    public void setReceivedChemistry(String receivedChemistry) {
        this.receivedChemistry = receivedChemistry;
    }

    // Set received chemistry time
    public void setReceivedChemistryTime(long receivedChemistryTime) {
        this.receivedChemistryTime = receivedChemistryTime;
    }

    // Set added time
    public void setAddedTime(long addedTime) {
        this.addedTime = addedTime;
    }

    // Set updated time
    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }
}