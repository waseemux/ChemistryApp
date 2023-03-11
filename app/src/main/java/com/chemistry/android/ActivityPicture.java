package com.chemistry.android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class ActivityPicture extends AppCompatActivity {

    private static final int GALLERY_PERMISSION_CODE = 101;                                 // Request code for gallery permission
    private Uri selectedImageUri;                                                           // Uri of selected image
    private MaskedImageView userThumbnailImageView;                                         // User thumbnail image view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_picture);                                          // Set content view
        userThumbnailImageView = findViewById(R.id.userThumbnail_ImageView);                // Get user thumbnail image view
        EditText fullNameEditText = findViewById(R.id.fullName_EditText);                   // Get full name edit text
        Button saveButton = findViewById(R.id.save_Button);                                 // Get save button

        /* ---------- CLICK LISTENERS ---------- */

        // Create an instance of UtilPermissions for READ_EXTERNAL_STORAGE permission
        UtilPermissions utilPermissions = new UtilPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE, GALLERY_PERMISSION_CODE);
        // Declare activity result launcher for handling intent results
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        selectedImageUri = data.getData();
                        userThumbnailImageView.setImageURI(selectedImageUri);
                    }
                }
            }
        );

        // Set click listener for thumbnail image view
        userThumbnailImageView.setOnClickListener(thumbnailView -> {
            // Check if permission has been granted
            utilPermissions.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            // If permission has been granted, create intent to pick image and start activity for result
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(galleryIntent);
        });

        // Set click listener on save button
        saveButton.setOnClickListener(buttonView -> {
            // If the user selected an image
            if (selectedImageUri != null) {
                // Upload the image to Firebase Storage
                StorageReference storage = FirebaseStorage.getInstance().getReference();
                String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
                StorageReference thumbnail = storage.child("thumbnails/" + phoneNumber);

                thumbnail.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded image
                    thumbnail.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Add the profile picture URL to the users database
                        DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("users");
                        assert phoneNumber != null;
                        usersDB.child(phoneNumber).child("thumbnail").setValue(uri.toString());
                    });
                });
            }

            // Check if the user entered a name
            if (fullNameEditText.getText() != null) {
                // Add the full name to the users database
                DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("users");
                String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
                assert phoneNumber != null;
                usersDB.child(phoneNumber).child("name").setValue(fullNameEditText.getText().toString());
            }

            // Get the calling activity's class name, or null if it's not available
            String callerActivity = getIntent().getStringExtra("CALLER_ACTIVITY");
            if (callerActivity == null) {

                String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
                assert phoneNumber != null;
                DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("users");
                DatabaseReference birthday = usersDB.child(phoneNumber).child("birthday");
                birthday.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Details already exist, skip ActivityDetail
                            Intent intent = new Intent(ActivityPicture.this, ActivityDetail.class);
                            startActivity(intent);
                        } else {
                            // Details don't exist, go to ActivityDetail
                            Intent intent = new Intent(ActivityPicture.this, ActivityMain.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            else if (callerActivity.equals(ActivityMain.class.getName())) {
                // If the caller activity is ActivityMain, return to it
                Intent intent = new Intent(ActivityPicture.this, ActivityMain.class);
                startActivity(intent);
            }

            else {
                // If the caller activity is not ActivityMain, go to ActivityDetail
                Intent intent = new Intent(ActivityPicture.this, ActivityDetail.class);
                startActivity(intent);
            }
        });

        // Check if user is a returning user
        String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
        DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("users");
        assert phoneNumber != null;
        usersDB.child(phoneNumber).child("thumbnail").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User already exists, fetch thumbnail from Firebase Storage and show it in the ImageView using Glide
                    StorageReference thumbnail = FirebaseStorage.getInstance().getReference().child("thumbnails/" + phoneNumber);

                    thumbnail.getDownloadUrl().addOnSuccessListener(uri ->
                        Glide.with(ActivityPicture.this).load(uri).into(userThumbnailImageView))
                            .addOnFailureListener(exception -> {
                                // Handle any errors
                                Toast.makeText(ActivityPicture.this, "Unable to fetch image at this time.", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
                Toast.makeText(ActivityPicture.this, "Unable to fetch thumbnail.", Toast.LENGTH_SHORT).show();
            }
        });

        usersDB.child(phoneNumber).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User already exists, fetch thumbnail from Firebase Storage and show it in the ImageView using Glide
                    String name = snapshot.getValue(String.class);
                    fullNameEditText.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
                Toast.makeText(ActivityPicture.this, "Unable to fetch name.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}