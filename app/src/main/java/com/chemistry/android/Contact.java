package com.chemistry.android;

public class Contact {
    private String name;
    private String phoneNumber;
    private String sentChemistry;
    private long sentChemistryTime;
    private String receivedChemistry;
    private long receivedChemistryTime;
    private long createdTime;
    private long updatedTime;
    private long birthday;
    private String gender;
    private String thumbnail;

    // Constructor → for calls to DataSnapshot.getValue(Contact.class)
    public Contact() {}

    // Constructor
    public Contact(
            String name,
            String phoneNumber,
            String sentChemistry,
            long sentChemistryTime,
            String receivedChemistry,
            long receivedChemistryTime,
            long createdTime,
            long updatedTime,
            long birthday,
            String gender,
            String thumbnail) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.sentChemistry = sentChemistry;
        this.sentChemistryTime = sentChemistryTime;
        this.receivedChemistry = receivedChemistry;
        this.receivedChemistryTime = receivedChemistryTime;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.birthday = birthday;
        this.gender = gender;
        this.thumbnail = thumbnail;
    }

    /* ---------- GETTERS ---------- */

    // Get name
    public String getName() {
        return name;
    }

    // Get phone number
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Get sent chemistry
    public String getSentChemistry() {
        return sentChemistry;
    }

    // Get sent chemistry time
    public long getSentChemistryTime() {
        return sentChemistryTime;
    }

    // Get received chemistry
    public String getReceivedChemistry() {
        return receivedChemistry;
    }

    // Get received chemistry time
    public long getReceivedChemistryTime() {
        return receivedChemistryTime;
    }

    // Get created time
    public long getCreatedTime() {
        return createdTime;
    }

    // Get updated time
    public long getUpdatedTime() {
        return updatedTime;
    }

    // Get birthday
    public long getBirthday() {
        return birthday;
    }

    // Get gender
    public String getGender() {
        return gender;
    }

    // Get profile picture
    public String getThumbnail() {
        return thumbnail;
    }

    /* ---------- SETTERS ---------- */

    // Set name
    public void setName(String name) {
        this.name = name;
    }

    // Set phone number
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Set sent chemistry
    public void setSentChemistry(String sentChemistry) {
        this.sentChemistry = sentChemistry;
    }

    // Set sent chemistry time
    public void setSentChemistryTime(long sentChemistryTime) {
        this.sentChemistryTime = sentChemistryTime;
    }

    // Set received chemistry
    public void setReceivedChemistry(String receivedChemistry) {
        this.receivedChemistry = receivedChemistry;
    }

    // Set received chemistry time
    public void setReceivedChemistryTime(long receivedChemistryTime) {
        this.receivedChemistryTime = receivedChemistryTime;
    }

    // Set sent created time
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    // Set sent updated time
    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    // Set birthday
    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    // Set gender
    public void setGender(String gender) {
        this.gender = gender;
    }

    // Set profile picture
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    // Set key → for calls to DataSnapshot.getValue(Contact.class)
    public void setKey(String key) {}
}