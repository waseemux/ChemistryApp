package com.chemistry.android;

public class Chemistry {
    private String key;
    private String name;
    private String icon;
    private int count;
    private int weight;
    private boolean isSpecial;

    // Constructor → for calls to DataSnapshot.getValue(Chemistry.class)
    public Chemistry() {
    }

    // Constructor
    public Chemistry(String key, String name, String icon, int count, int weight, boolean isSpecial) {
        this.key = key;
        this.name = name;
        this.icon = icon;
        this.count = count;
        this.weight = weight;
        this.isSpecial = isSpecial;
    }

    /* ---------- GETTERS ---------- */

    // Get chemistry key
    public String getKey() {
        return key;
    }

    // Get chemistry name
    public String getName() {
        return name;
    }

    // Get chemistry icon
    public String getIcon() {
        return icon;
    }

    // Get chemistry count
    public int getCount() {
        return count;
    }

    // Get chemistry weight
    public int getWeight() {
        return weight;
    }

    // Get chemistry type
    public boolean getIsSpecial() {
        return isSpecial;
    }

    /* ---------- SETTERS ---------- */

    // Set chemistry key
    public void setKey(String key) {
        this.key = key;
    }

    // Set chemistry name
    public void setName(String name) {
        this.name = name;
    }

    // Set chemistry icon
    public void setIcon(String icon) {
        this.icon = icon;
    }

    // Set chemistry count
    public void setCount(int count) {
        this.count = count;
    }

    // Set chemistry weight
    public void setWeight(int weight) {
        this.weight = weight;
    }

    // Set chemistry type
    public void setIsSpecial(boolean isSpecial) {
        this.isSpecial = isSpecial;
    }
}