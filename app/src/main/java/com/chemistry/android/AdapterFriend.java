package com.chemistry.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class AdapterFriend extends ArrayAdapter<Contact> {

    //Constructor
    public AdapterFriend(Context context, List<Contact> contacts) {
        super(context, 0, contacts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = getItem(position);

        if (contact == null) {
            return convertView;
        }

        // Initialize layout for contact item
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_friend, parent, false);
        }

        // Initialize elements of contact item that will be populated by data
        TextView nameTextView = convertView.findViewById(R.id.friendName_TextView);
        MaskedImageView thumbnailImageView = convertView.findViewById(R.id.friendThumbnail_ImageView);

        /* ---------- POPULATE ---------- */

        // Populate name text view
        nameTextView.setText(contact.getName());

        // Populate profile picture image view
        thumbnailImageView.setClipToOutline(true);
        Glide.with(getContext()).load(contact.getThumbnail()).into(thumbnailImageView);

        /* ---------- CLICK LISTENER ---------- */

        // Set click listener on convertView to open Bottom Sheet Dialog
        convertView.setOnClickListener(v -> {
            // Create new Bottom Sheet Dialog
            BottomSheetDialog friendSheet = new BottomSheetDialog(v.getContext());

            // Set the layout of the Bottom Sheet Dialog
            View friendSheetView = LayoutInflater.from(v.getContext()).inflate(R.layout.sheet_friends, null);
            friendSheet.setContentView(friendSheetView);

            // Pass the clicked Contact object to the Bottom Sheet Dialog
            TextView identifiedDurationTextView = friendSheetView.findViewById(R.id.identifiedDuration_TextView);
            MaskedImageView identifiedThumbnailImageView = friendSheetView.findViewById(R.id.identifiedThumbnail_ImageView);
            TextView identifiedNameTextView = friendSheetView.findViewById(R.id.identifiedName_TextView);
            TextView fromTextView = friendSheetView.findViewById(R.id.from_TextView);
            TextView toTextView = friendSheetView.findViewById(R.id.to_TextView);
            TextView friendReceivedChemistryTextView = friendSheetView.findViewById(R.id.friendReceivedChemistry_TextView);
            TextView friendSentChemistryTextView = friendSheetView.findViewById(R.id.friendSentChemistry_TextView);

            long duration = Math.abs(contact.getReceivedChemistryTime() - contact.getSentChemistryTime());

            if (duration < 60) {
                String durationText = "⏱ 0m";
                identifiedDurationTextView.setText(String.format("Identified in %s 🔥🔥🔥.", durationText));
            }

            // Chemistry received less than 1 hour ago
            else if (duration < 600) {
                long minutes = duration / 60;
                String durationText = String.format("%dm 🔥🔥", minutes);
                identifiedDurationTextView.setText(String.format("Identified in %s", durationText));
            }

            // Chemistry received less than 1 hour ago
            else if (duration < 3600) {
                long minutes = duration / 60;
                String durationText = String.format("%dm 🔥", minutes);
                identifiedDurationTextView.setText(String.format("Identified in %s", durationText));
            }

            // Chemistry received less than 1 day ago
            else if (duration < 86400) {
                long hours = duration / 3600;
                String durationText = String.format("%dh", hours);
                identifiedDurationTextView.setText(String.format("Identified in %s", durationText));
            }

            // Chemistry received less than 1 week ago
            else if (duration < 604800) {
                long days = duration / 86400;
                String durationText = String.format("%dd", days);
                identifiedDurationTextView.setText(String.format("Identified in %s", durationText));
            }

            // Chemistry received more than 1 week ago
            else {
                long weeks = duration / 604800;
                String durationText = String.format("%dw", weeks);
                identifiedDurationTextView.setText(String.format("Identified in %s", durationText));
            }

            identifiedThumbnailImageView.setClipToOutline(true);
            Glide.with(getContext()).load(contact.getThumbnail()).into(identifiedThumbnailImageView);

            identifiedNameTextView.setText(contact.getName());
            fromTextView.setText(String.format("From %s to you", contact.getName()));
            toTextView.setText(String.format("To %s from you", contact.getName()));
            friendReceivedChemistryTextView.setText(String.format("%s %s",
                    Chemistries.getIconByKey(contact.getReceivedChemistry()),
                    Chemistries.getNameByKey(contact.getReceivedChemistry())));
            friendSentChemistryTextView.setText(String.format("%s %s",
                    Chemistries.getIconByKey(contact.getSentChemistry()),
                    Chemistries.getNameByKey(contact.getSentChemistry())));

            // Show the Bottom Sheet Dialog
            friendSheet.show();
        });

        return convertView;
    }
}