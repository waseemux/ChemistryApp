package com.chemistry.android;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class UtilContacts {
    private final Context context;
    private final RecyclerView anonymousRecyclerView;
    private final ListView contactsListView;

    private final List<Contact> anonymousList;
    private static final List<Contact> friendsList = new ArrayList<>();
    private final List<Contact> contactsList;

    private final List<Contact> processedPhonebook;

    private final DatabaseReference userContacts;
    private final DatabaseReference userProspects;

    //Constructor
    public UtilContacts(Context context, RecyclerView anonymous, ListView contacts) {
        this.context = context;
        this.anonymousRecyclerView = anonymous;
        this.contactsListView = contacts;

        anonymousList = new ArrayList<>();
        contactsList = new ArrayList<>();

        processedPhonebook = new ArrayList<>();

        DatabaseReference contactsDB = FirebaseDatabase.getInstance().getReference().child("contacts");
        DatabaseReference prospectsDB = FirebaseDatabase.getInstance().getReference().child("prospects");

        FirebaseUser user = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser());
        String userPhoneNumber = Objects.requireNonNull(user.getPhoneNumber());

        userContacts = contactsDB.child(userPhoneNumber);
        userProspects = prospectsDB.child(userPhoneNumber);
    }

    /* ---------- PROCESSORS ---------- */

    // Method to process phonebook contacts
    private void processPhonebook() {

        // Clear phonebook list
        processedPhonebook.clear();

        // Create list for raw phonebook data
        final Set<String> phonebook = new HashSet<>();

        ContentResolver contentResolver = context.getContentResolver();
        try (Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)) {
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    // Get name
                    int nameColumn = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    if (nameColumn == -1) {
                        continue;
                    }
                    String name = cursor.getString(nameColumn);

                    // Get ID
                    int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                    if (idColumn == -1) {
                        continue;
                    }
                    String id = cursor.getString(idColumn);

                    // Get phone number
                    int hasPhoneNumberColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                    if (hasPhoneNumberColumn == -1) {
                        continue;
                    }
                    if (cursor.getInt(hasPhoneNumberColumn) > 0) {
                        try (Cursor pCur = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id},
                                null)) {
                            while (pCur.moveToNext()) {
                                int phoneNumberColumn = pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                if (phoneNumberColumn == -1) {
                                    continue;
                                }
                                String phoneNo = pCur.getString(phoneNumberColumn);

                                // Format phone number to E.164 format
                                try {
                                    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
                                    Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phoneNo, Locale.getDefault().getCountry());
                                    phoneNo = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
                                } catch (NumberParseException e) {
                                    e.printStackTrace();
                                }

                                // Phone number not repeated and >= 7 digits → build Contact
                                if (!phonebook.contains(phoneNo) && phoneNo.length() >= 7) {
                                    phonebook.add(phoneNo);

                                    // Populate fields to build Contact
                                    Contact contact = new Contact(
                                            name,
                                            phoneNo,
                                            "",
                                            0,
                                            "",
                                            0,
                                            System.currentTimeMillis(),
                                            System.currentTimeMillis(),
                                            0,
                                            "",
                                            ""
                                    );

                                    // If name is not 'SPAM' → add Contact to phonebookContacts
                                    if (!name.equals("SPAM")) {
                                        processedPhonebook.add(contact);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Method to upload processed phonebook to Firebase
    private void uploadProcessedPhonebook() {

        // Find user's contacts node in contacts database
        userContacts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userContactsSnapshot) {

                // Node doesn't exist → create
                if (!userContactsSnapshot.exists()) {
                    userContacts.setValue(new HashMap<String, Object>());
                }

                // Upload contacts from processed phonebook
                for (Contact contact : processedPhonebook) {
                    String contactPhoneNumber = contact.getPhoneNumber();
                    userContacts.child(contactPhoneNumber).setValue(contact);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Method to merge user's contacts with prospects
    private void mergeProspectsWithContacts() {

        // Find user's prospects node in prospects database
        userProspects.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userProspectsSnapshot) {

                // Node exists → find prospects
                if (userProspectsSnapshot.exists()) {
                    for (DataSnapshot prospect : userProspectsSnapshot.getChildren()) {
                        final String prospectPhoneNumber = prospect.child("phoneNumber").getValue(String.class);
                        final String prospectReceivedChemistry = prospect.child("receivedChemistry").getValue(String.class);
                        final Long prospectReceivedChemistryTime = prospect.child("receivedChemistryTime").getValue(Long.class);

                        // Find prospect in user's contacts
                        assert prospectPhoneNumber != null;
                        userContacts.child(prospectPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot contactSnapshot) {
                                final DatabaseReference contact = userContacts.child(prospectPhoneNumber);

                                // Prospect is a contact → update contact
                                if (contactSnapshot.exists()) {
                                    contact.child("receivedChemistry").setValue(prospectReceivedChemistry);
                                    contact.child("receivedChemistryTime").setValue(prospectReceivedChemistryTime);
                                    contact.child("updatedTime").setValue(prospectReceivedChemistryTime);
                                }

                                // Prospect is not a contact → add prospect to contacts
                                else {
                                    Map<String, Object> newContact = new HashMap<>();
                                    newContact.put("name", "Stranger Anonymous");
                                    newContact.put("phoneNumber", prospectPhoneNumber);
                                    newContact.put("sentChemistry", "");
                                    newContact.put("sentChemistryTime", 0);
                                    newContact.put("receivedChemistry", prospectReceivedChemistry);
                                    newContact.put("receivedChemistryTime", prospectReceivedChemistryTime);
                                    newContact.put("createdTime", prospectReceivedChemistryTime);
                                    newContact.put("updatedTime", prospectReceivedChemistryTime);
                                    newContact.put("birthday", 0);
                                    newContact.put("gender", "");
                                    newContact.put("thumbnail", "");
                                    contact.setValue(newContact);
                                }

                                // Delete prospect from prospects database
                                userProspects.child(prospectPhoneNumber).removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }

                    // Delete user's prospects node from prospects database
                    userProspects.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Method to update contacts of returning user
    private void updateContacts() {

        // Process phonebook contacts
        processedPhonebook.clear();
        processPhonebook();

        // Compare processed phonebook with user's contacts on Firebase
        for (Contact phonebookContact : processedPhonebook) {
            final DatabaseReference contact = userContacts.child(phonebookContact.getPhoneNumber());

            // Find phonebook contact in user's contacts on Firebase
            contact.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot contactSnapshot) {

                    // Phonebook contact is not in database → add
                    if (!contactSnapshot.exists()) {
                        Map<String, Object> newContact = new HashMap<>();
                        newContact.put("name", phonebookContact.getName());
                        newContact.put("phoneNumber", phonebookContact.getPhoneNumber());
                        newContact.put("sentChemistry", "");
                        newContact.put("sentChemistryTime", 0);
                        newContact.put("receivedChemistry", "");
                        newContact.put("receivedChemistryTime", 0);
                        newContact.put("createdTime", System.currentTimeMillis());
                        newContact.put("updatedTime", System.currentTimeMillis());
                        newContact.put("birthday", 0);
                        newContact.put("gender", "");
                        newContact.put("thumbnail", "");
                        contact.setValue(newContact);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // Method to download contacts from Firebase
    public void downloadContacts() {

        // Find user's contacts node in contacts database
        userContacts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userContactsSnapshot) {

                // Clear the final lists
                anonymousList.clear();
                friendsList.clear();
                contactsList.clear();

                // Create temporary lists to store contacts with and without sent chemistry
                final List<Contact> tempContactsWithSentChemistry = new ArrayList<>();
                final List<Contact> tempContactsWithoutSentChemistry = new ArrayList<>();

                /* ---------- LIST SORTING ---------- */

                // Go through each contact in user's contacts node and sort into lists
                for (DataSnapshot contactSnapshot : userContactsSnapshot.getChildren()) {
                    Contact contact = contactSnapshot.getValue(Contact.class);
                    assert contact != null;

                    // Has both sent and received chemistry → add to friends list
                    if (contact.getSentChemistryTime() != 0 && contact.getReceivedChemistryTime() != 0) {
                        friendsList.add(contact);
                    }

                    // Has sent chemistry but not received chemistry → add to with sent chemistry temp list
                    else if (contact.getSentChemistryTime() != 0 && contact.getReceivedChemistryTime() == 0) {
                        tempContactsWithSentChemistry.add(contact);
                    }

                    // Has received chemistry but not sent chemistry → add to anonymous list
                    else if (contact.getSentChemistryTime() == 0 && contact.getReceivedChemistryTime() != 0) {
                        anonymousList.add(contact);
                        tempContactsWithoutSentChemistry.add(contact);
                    }

                    // Has neither sent nor received chemistry → add to without sent chemistry temp list
                    else {
                        tempContactsWithoutSentChemistry.add(contact);
                    }
                }

                /* ---------- LIST ORDERS ---------- */

                // Order anonymous list by most recent received chemistry first
                anonymousList.sort((c1, c2) -> Long.compare(c2.getReceivedChemistryTime(), c1.getReceivedChemistryTime()));

                // Order friends list by most recent chemistry first, be it sent or received
                friendsList.sort((c1, c2) -> Long.compare(
                        Math.max(c2.getSentChemistryTime(), c2.getReceivedChemistryTime()),
                        Math.max(c1.getSentChemistryTime(), c1.getReceivedChemistryTime())));

                // Order 'with sent chemistry' temp list by most recent sent chemistry first
                tempContactsWithSentChemistry.sort((c1, c2) -> Long.compare(c2.getSentChemistryTime(), c1.getSentChemistryTime()));

                // Order 'without sent chemistry' temp list by alphabet
                tempContactsWithoutSentChemistry.sort(Comparator.comparing(Contact::getName));

                // Add 'with sent chemistry' and 'without sent chemistry' temp lists to contacts list
                contactsList.addAll(tempContactsWithSentChemistry);
                contactsList.addAll(tempContactsWithoutSentChemistry);

                /* ---------- FRIENDS PICTURES ---------- */

                //Initialize users database
                DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference("users");

                // Go through each contact in friends list and download their profile picture
                for (Contact friend : friendsList) {
                    final String friendPhoneNumber = friend.getPhoneNumber();
                    final DatabaseReference friendUserDB = usersDB.child(friendPhoneNumber);

                    friendUserDB.child("thumbnail").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot friendThumbnailSnapshot) {
                            String friendThumbnailUrl = friendThumbnailSnapshot.getValue(String.class);
                            userContacts.child(friendPhoneNumber).child("thumbnail").setValue(friendThumbnailUrl);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }

                /* ---------- LIST ADAPTERS ---------- */

                // Set anonymous list adapter
                AdapterAnonymous adapterAnonymous = new AdapterAnonymous(anonymousRecyclerView.getContext(), anonymousList);
                LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                anonymousRecyclerView.setLayoutManager(layoutManager);
                int horizontalSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
                anonymousRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(horizontalSpacing));
                anonymousRecyclerView.setAdapter(adapterAnonymous);

                // Set contacts list adapter
                AdapterContact adapterContact = new AdapterContact(contactsListView.getContext(), contactsList);
                contactsListView.setAdapter(adapterContact);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Method to add horizontal spacing between, left and right of items in a RecyclerView
    private static class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public HorizontalSpaceItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            // Spacing between items
            if (position != 0) {
                outRect.left = spacing;
            }

            // Spacing before first item
            if (position == 0) {
                outRect.left = spacing + spacing;
            }

            // Spacing after last item
            if (position == Objects.requireNonNull(parent.getAdapter()).getItemCount() - 1) {
                outRect.right = spacing + spacing;
            }
        }
    }

    /* ---------- PIPELINES ---------- */

    // Method to process contacts for a new user
    public void processNewUserContacts() {

        // Process phonebook contacts
        processPhonebook();

        // Upload processed phonebook to Firebase
        uploadProcessedPhonebook();

        // Merge prospects with contacts
        mergeProspectsWithContacts();
    }

    // Method to process contacts for a returning user
    public void processReturningUserContacts() {

        // Process phonebook contacts
        processPhonebook();

        // Update contacts with phonebook contacts
        updateContacts();
    }

    // Method to process contacts for new and returning users
    public void processContacts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        // Get user creation and last sign in times
        long userCreationTime = Objects.requireNonNull(user.getMetadata()).getCreationTimestamp();
        long userLastSignInTime = user.getMetadata().getLastSignInTimestamp();

        // User created <1s before last sign in → new user
        if (userLastSignInTime - userCreationTime < 1000) {
            processNewUserContacts();
        }

        // User created >1s before last sign in → returning user
        else {
            processReturningUserContacts();
        }
    }

    // Method to search contacts list
    public List<Contact> filterContacts(String query) {
        List<Contact> filteredList = new ArrayList<>();
        for (Contact contact : contactsList) {
            if (contact.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(contact);
            }
        }
        return filteredList;
    }

    /* ---------- GETTERS ---------- */

    // Get anonymous list
    public List<Contact> getAnonymousList() {
        return anonymousList;
    }

    // Get friends list
    public static List<Contact> getFriendsList() {
        return friendsList;
    }

    // Get contacts list
    public List<Contact> getContactsList() {
        return contactsList;
    }
}