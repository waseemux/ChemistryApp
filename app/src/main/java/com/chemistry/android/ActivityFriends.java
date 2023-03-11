package com.chemistry.android;

import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityFriends extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        GridView friendsGridView = findViewById(R.id.friends_GridView);
        ImageButton friendsBackButton = findViewById(R.id.friendsBack_Button);

        if (UtilContacts.getFriendsList() != null && !UtilContacts.getFriendsList().isEmpty()) {
            AdapterFriend adapterFriend = new AdapterFriend(friendsGridView.getContext(), UtilContacts.getFriendsList());
            friendsGridView.setAdapter(adapterFriend);
        }

        friendsBackButton.setOnClickListener(v -> finish());
    }
}