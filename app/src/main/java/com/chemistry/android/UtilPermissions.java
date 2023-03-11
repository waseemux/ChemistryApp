package com.chemistry.android;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

public class UtilPermissions {
    private final Activity activity;
    final Map<String, Integer> permissions;

    // Constructor
    public UtilPermissions(Activity activity, String permission, int requestCode) {
        this.activity = activity;
        this.permissions = new HashMap<>();

        // Add the permission and its request code
        addPermission(permission, requestCode);
    }

    // Method to check if permission has been granted
    public void checkPermission(String permission) {

        // Get request code associated with the permission
        Integer requestCode = permissions.get(permission);

        // If permission not found in the map, throw exception
        if (requestCode == null) {
            throw new IllegalArgumentException("Permission not found: " + permission);
        }

        // If permission not granted → request it
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(permission, requestCode);
        }
    }

    // Method to request a permission
    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    // Method to add a new permission to the map
    private void addPermission(String permission, int requestCode) {
        permissions.put(permission, requestCode);
    }
}
