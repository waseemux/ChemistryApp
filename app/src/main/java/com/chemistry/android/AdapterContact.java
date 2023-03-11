package com.chemistry.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AdapterContact extends ArrayAdapter<Contact> {

    //Constructor
    public AdapterContact(Context context, List<Contact> contacts) {
        super(context, 0, contacts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = getItem(position);

        // Initialize layout for contact item
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
        }

        // Initialize elements of contact item that will be populated by data
        TextView nameTextView = convertView.findViewById(R.id.contactName_TextView);
        TextView waityTextView = convertView.findViewById(R.id.waity_TextView);
        TextView instructionTextView = convertView.findViewById(R.id.contactInstruction_TextView);

        /* ---------- POPULATE ---------- */

        // Populate name text view
        nameTextView.setText(contact.getName());

        // Populate phone number text view
        if (contact.getSentChemistry().equals("")) {
            waityTextView.setText(R.string.red_question_mark);
        } else {
            waityTextView.setText("");
        }

        // Populate instruction text view → chemistry not sent
        if (contact.getSentChemistry().equals("")) {
            instructionTextView.setText(String.format(getContext().getString(R.string.contact_instruction_text), contact.getName()));
        }

        // Populate instruction text view → chemistry sent
        else {
            long duration = (System.currentTimeMillis() - contact.getSentChemistryTime()) / 1000;

            // Chemistry sent less than 1 minute ago
            if (duration < 60) {
                String durationText = String.format("Waiting for %s to identify you — ⏱ 0m", contact.getName());
                instructionTextView.setText(durationText);
            }

            // Chemistry sent less than 1 hour ago
            else if (duration <3600) {
                long minutes = duration / 60;
                String durationText = String.format("Waiting for %s to identify you — ⏱ %dm", contact.getName(), minutes);
                instructionTextView.setText(durationText);
            }

            // Chemistry sent less than 1 day ago
            else if (duration < 86400) {
                long hours = duration / 3600;
                String durationText = String.format("Waiting for %s to identify you — ⏱ %dh", contact.getName(), hours);
                instructionTextView.setText(durationText);
            }

            // Chemistry sent less than 1 week ago
            else if (duration < 604800) {
                long days = duration / 86400;
                String durationText = String.format("Waiting for %s to identify you — ⏱ %dd", contact.getName(), days);
                instructionTextView.setText(durationText);
            }

            // Chemistry sent more than 1 week ago
            else {
                long weeks = duration / 604800;
                String durationText = String.format("Waiting for %s to identify you — ⏱ %dw", contact.getName(), weeks);
                instructionTextView.setText(durationText);
            }
        }

        return convertView;
    }
}