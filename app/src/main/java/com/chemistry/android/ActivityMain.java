package com.chemistry.android;

import static android.content.ContentValues.TAG;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ActivityMain extends AppCompatActivity {
    private UtilPermissions readContactsPermission;
    private UtilPermissions pushNotificationsPermission;
    private RecyclerView anonymousRecyclerView;
    private ListView contactsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* -------------------- SIGN IN -------------------- */

        // Sign in and activity routing
        FirebaseAuth auth = FirebaseAuth.getInstance();
        UtilSignIn utilSignIn = new UtilSignIn(FirebaseAuth.getInstance(), this, this);
        if (!utilSignIn.isSignedIn()) {
            utilSignIn.checkSignInAndRoute();
            return;
        }

        // Get user's phone number
        String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
        assert phoneNumber != null;

        // ----- END OF SIGN IN ----- //

        /* -------------------- LAYOUT -------------------- */

        setContentView(R.layout.activity_contacts);                             // Page layout
        anonymousRecyclerView = findViewById(R.id.anonymous_RecyclerView);      // Anonymous list
        contactsListView = findViewById(R.id.contacts_ListView);                // Contacts list
        ImageButton optionsButton = findViewById(R.id.signOut_Button);          // Sign out button
        ImageButton friendsButton = findViewById(R.id.friends_Button);          // Friends button
        SearchView searchView = findViewById(R.id.contactSearch_SearchView);    // Search contacts

        // ----- END OF LAYOUT ----- //

        /* -------------------- CONTACTS -------------------- */

        // Initialize UtilContacts class to process contacts
        UtilContacts utilContacts = new UtilContacts(this, anonymousRecyclerView, contactsListView);

        // Initialize UtilPermissions class to request permissions
        readContactsPermission = new UtilPermissions(this, Manifest.permission.READ_CONTACTS, 1);

        // Permission already granted → download contacts
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            utilContacts.downloadContacts();
        }

        // Permission not granted yet → request it and download contacts
        else {
            readContactsPermission.checkPermission(Manifest.permission.READ_CONTACTS);
            utilContacts.downloadContacts();
        }

        // ----- END OF CONTACTS ----- //

        /* -------------------- TOKEN -------------------- */

        // Push notification permission
        pushNotificationsPermission = new UtilPermissions(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY, 2);

        // Permission already granted → get token
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) == PackageManager.PERMISSION_GRANTED) {
            getFMSToken();
        }

        // Permission not granted yet → request it and get token
        else {
            pushNotificationsPermission.checkPermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
            getFMSToken();
        }

        // ----- END OF TOKEN ----- //

        /* -------------------- SEARCH -------------------- */

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Contact> filteredContacts = utilContacts.filterContacts(newText);
                AdapterContact filteredAdapter = new AdapterContact(contactsListView.getContext(), filteredContacts);
                contactsListView.setAdapter(filteredAdapter);
                return true;
            }
        });

        // ----- END OF SEARCH ----- //

        /* -------------------- CLICK ANONYMOUS -------------------- */

        /* -------------------- CLICK CONTACT -------------------- */

        // Set click listener for contacts list view
        contactsListView.setOnItemClickListener((parent, view, position, id) -> {

            // Get selected contact as recipient
            Contact recipient = (Contact) parent.getItemAtPosition(position);

            // Get recipient's phone number
            String recipientPhoneNumber = recipient.getPhoneNumber();

            if (recipient.getSentChemistry().equals("")) {
                // Create bottom sheet dialog for chemistry picker
                BottomSheetDialog chemistryPicker = new BottomSheetDialog(this);

                // Inflate chemistry picker layout
                View chemistryPickerView = getLayoutInflater().inflate(R.layout.sheet_chemistries, contactsListView, false);

                // Set content view for chemistry picker
                chemistryPicker.setContentView(chemistryPickerView);

                /* -------------------- CHEMISTRIES -------------------- */

                // Initialize list view for chemistries
                GridView chemistriesGridView = chemistryPicker.findViewById(R.id.chemistryPicker_GridView);

                // Get the list of chemistries
                List<Chemistry> chemistriesList = Chemistries.getChemistries();

                // Order chemistries by weight
                chemistriesList.sort((chemistry1, chemistry2) -> Integer.compare(chemistry2.getWeight(), chemistry1.getWeight()));

                // Set chemistries list adapter
                if (chemistriesGridView != null) {
                    AdapterChemistry adapterChemistry = new AdapterChemistry(chemistryPicker.getContext(), chemistriesList);
                    chemistriesGridView.setAdapter(adapterChemistry);
                }

                // Open chemistry picker
                chemistryPicker.show();

                // Set click listener for chemistries list view
                if (chemistriesGridView != null) {
                    chemistriesGridView.setOnItemClickListener((parent1, view1, position1, id1) -> {

                        // Get selected chemistry
                        Chemistry selectedChemistry = (Chemistry) parent1.getItemAtPosition(position1);

                        // Get selected chemistry's key
                        String selectedChemistryKey = selectedChemistry.getKey();

                        // Check if clicked chemistry is already selected
                        boolean isSelected = view1.getTag() != null ? (Boolean) view1.getTag() : false;
                        if (isSelected) {

                            // Deselect the clicked chemistry
                            view1.setBackground(null);
                            view1.setTag(null);
                        }

                        // Clicked chemistry is not already selected
                        else {

                            // Deselect previously selected chemistry (if any)
                            for (int i = 0; i < parent1.getChildCount(); i++) {
                                View childView = parent1.getChildAt(i);
                                if (childView.getTag() != null && (Boolean) childView.getTag()) {
                                    childView.setBackground(null);
                                    childView.setTag(null);
                                    break;
                                }
                            }

                            // Select the clicked chemistry
                            view1.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_10));
                            view1.setTag(true);
                        }



                        /* -------------------- SEND BUTTON -------------------- */

                        // Initialize send button
                        Button sendButton = chemistryPicker.findViewById(R.id.sendChemistry_Button);

                        // Set click listener for send button
                        assert sendButton != null;
                        sendButton.setOnClickListener(v -> {

                            // Set sent chemistry for contact on device
                            recipient.setSentChemistry(selectedChemistryKey);
                            recipient.setSentChemistryTime(System.currentTimeMillis());
                            recipient.setUpdatedTime(System.currentTimeMillis());

                            // Get sent chemistry, sent chemistry time, and updated time
                            String sentChemistry = recipient.getSentChemistry();
                            long sentChemistryTime = recipient.getSentChemistryTime();
                            long updatedTime = recipient.getUpdatedTime();

                            // Get database reference for contacts, user's contacts, and selected contact
                            DatabaseReference contactsDB = FirebaseDatabase.getInstance().getReference().child("contacts");
                            DatabaseReference userContactsDB = contactsDB.child(phoneNumber);
                            DatabaseReference recipientContact = userContactsDB.child(recipientPhoneNumber);

                            // Set sent chemistry, sent chemistry time, and updated time for contact in database
                            recipientContact.child("sentChemistry").setValue(sentChemistry);
                            recipientContact.child("sentChemistryTime").setValue(sentChemistryTime);
                            recipientContact.child("updatedTime").setValue(updatedTime);

                            // Update received chemistry in user's contact in recipient's contacts database
                            DatabaseReference recipientContactsDB = contactsDB.child(recipientPhoneNumber);

                            // Find recipient's contacts database
                            recipientContactsDB.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot recipientContactsDBSnapshot) {

                                    // Recipient is a user
                                    if (recipientContactsDBSnapshot.exists()) {
                                        DatabaseReference userAsRecipientContact = recipientContactsDB.child(phoneNumber);

                                        // Find user in recipient's contacts database
                                        userAsRecipientContact.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userAsRecipientContactSnapshot) {

                                                // User is a contact of recipient → update contact
                                                if (userAsRecipientContactSnapshot.exists()) {
                                                    userAsRecipientContact.child("receivedChemistry").setValue(sentChemistry);
                                                    userAsRecipientContact.child("receivedChemistryTime").setValue(sentChemistryTime);
                                                    userAsRecipientContact.child("updatedTime").setValue(updatedTime);
                                                }

                                                // User is not a contact of recipient → create contact
                                                else {
                                                    Map<String, Object> newContact = new HashMap<>();
                                                    newContact.put("name", "Stranger Sender");
                                                    newContact.put("phoneNumber", phoneNumber);
                                                    newContact.put("sentChemistry", "");
                                                    newContact.put("sentChemistryTime", 0);
                                                    newContact.put("receivedChemistry", sentChemistry);
                                                    newContact.put("receivedChemistryTime", sentChemistryTime);
                                                    newContact.put("createdTime", updatedTime);
                                                    newContact.put("updatedTime", updatedTime);
                                                    newContact.put("birthday", 0);
                                                    newContact.put("gender", "");
                                                    newContact.put("thumbnail", "");
                                                    userAsRecipientContact.setValue(newContact);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                                    }

                                    // Recipient is not a user
                                    else {
                                        DatabaseReference prospectsDB = FirebaseDatabase.getInstance().getReference().child("prospects");
                                        DatabaseReference recipientProspectsDB = prospectsDB.child(recipientPhoneNumber);

                                        // Find prospects database
                                        recipientProspectsDB.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot recipientProspectsDBSnapshot) {

                                                // Recipient is a prospect
                                                if (recipientContactsDBSnapshot.exists()) {
                                                    DatabaseReference userAsRecipientProspect = recipientProspectsDB.child(phoneNumber);

                                                    // Find user in recipient's prospects database
                                                    userAsRecipientProspect.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot userAsRecipientProspectSnapshot) {

                                                            // User is a prospect of recipient → update prospect
                                                            if (userAsRecipientProspectSnapshot.exists()) {
                                                                userAsRecipientProspect.child("receivedChemistry").setValue(sentChemistry);
                                                                userAsRecipientProspect.child("receivedChemistryTime").setValue(sentChemistryTime);
                                                                userAsRecipientProspect.child("updatedTime").setValue(updatedTime);
                                                            }

                                                            // User is not a prospect of recipient → create prospect
                                                            else {
                                                                Map<String, Object> newProspect = new HashMap<>();
                                                                newProspect.put("phoneNumber", phoneNumber);
                                                                newProspect.put("receivedChemistry", sentChemistry);
                                                                newProspect.put("receivedChemistryTime", sentChemistryTime);
                                                                newProspect.put("addedTime", updatedTime);
                                                                newProspect.put("updatedTime", updatedTime);
                                                                userAsRecipientProspect.setValue(newProspect);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {}
                                                    });
                                                }

                                                // Recipient is not a prospect
                                                else {
                                                    Map<String, Object> newProspect = new HashMap<>();
                                                    newProspect.put("phoneNumber", phoneNumber);
                                                    newProspect.put("receivedChemistry", sentChemistry);
                                                    newProspect.put("receivedChemistryTime", sentChemistryTime);
                                                    newProspect.put("addedTime", updatedTime);
                                                    newProspect.put("updatedTime", updatedTime);
                                                    recipientProspectsDB.child(phoneNumber).setValue(newProspect);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });

                                        /* -------------------- SMS -------------------- */

                                        // Call the Firebase Functions HTTP callable function
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("phoneNumber", recipientPhoneNumber);
                                        data.put("message", sentChemistry + ". Find out who sent you this on Chemistry. Download: play.google.com");

                                        FirebaseFunctions.getInstance().useEmulator("127.0.0.1", 5001);
                                        FirebaseFunctions.getInstance().getHttpsCallable("sendSms")
                                                .call(data)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "SMS sent successfully");
                                                    } else {
                                                        Log.e(TAG, "Error sending SMS", task.getException());
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });

                            // Close chemistry picker
                            chemistryPicker.dismiss();
                        });
                    });
                }
            }

            else {
                // Show custom dialog
                String gender = recipient.getGender();
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
                builder.setTitle("Chemistry sent");
                builder.setMessage("You have already sent a chemistry to " + recipient.getName() + ". Please wait for them to identify you.");

                // Add button
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                // Show dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        /* -------------------- CLICK ANONYMOUS -------------------- */

        /* -------------------- SIGN OUT -------------------- */

        // Set OnClickListener on the button
        optionsButton.setOnClickListener(v -> {
            // Create a BottomSheetDialog
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ActivityMain.this);

            // Set the menu resource for the dialog
            bottomSheetDialog.setContentView(R.layout.sheet_options);

            // Find the menu items in your bottom sheet layout
            LinearLayout editOption = bottomSheetDialog.findViewById(R.id.edit_option);
            LinearLayout logoutOption = bottomSheetDialog.findViewById(R.id.logout_option);

            // Set the click listeners for the menu items
            assert editOption != null;
            editOption.setOnClickListener(v1 -> {
                Intent intent = new Intent(ActivityMain.this, ActivityPicture.class);
                intent.putExtra("CALLER_ACTIVITY", ActivityMain.class.getName());
                startActivity(intent);
                bottomSheetDialog.dismiss(); // dismiss the bottom sheet dialog
            });

            // Set click listener for sign out button
            assert logoutOption != null;
            logoutOption.setOnClickListener(v2 -> {

                // Sign out user
                auth.signOut();

                // Go to phone page
                Intent intent = new Intent(this, ActivityPhone.class);
                startActivity(intent);
            });

            // Show the bottom sheet dialog
            bottomSheetDialog.show();
        });

        /* -------------------- FRIENDS -------------------- */

        // Set click listener for friends button
        friendsButton.setOnClickListener(v -> {

            // Go to friends page
            Intent intent = new Intent(this, ActivityFriends.class);
            startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (readContactsPermission.permissions.containsValue(requestCode)) {

            // Permission granted → process contacts
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Initialize list views for anonymous, friends, and contacts lists
                anonymousRecyclerView = findViewById(R.id.anonymous_RecyclerView);
                contactsListView = findViewById(R.id.contacts_ListView);

                // Initialize process contacts object
                UtilContacts utilContacts = new UtilContacts(this, anonymousRecyclerView, contactsListView);
                utilContacts.processContacts();
            }
        }

        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (pushNotificationsPermission.permissions.containsValue(requestCode)) {

            // Permission granted → get FCM token
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFMSToken();
            }
        }

        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getFMSToken() {
        DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("users");
        String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
        assert phoneNumber != null;
        DatabaseReference userToken = usersDB.child(phoneNumber).child("token");

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(ActivityMain.this, "Failed to get token", Toast.LENGTH_SHORT).show();
                return;
            }
            String token = task.getResult();
            if (token != null) {
                userToken.setValue(token);
            }
            if (token == null) {
                Handler handler = new Handler();
                handler.postDelayed(() -> FirebaseMessaging.getInstance().getToken().addOnCompleteListener(retryTask -> {
                    String retryToken = retryTask.getResult();
                    if (retryToken != null) {
                        userToken.setValue(retryToken);
                    }
                }), 500); // Try again after 0.5s
            }
        });
    }
}