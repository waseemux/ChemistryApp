package com.chemistry.android;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class AdapterAnonymous extends RecyclerView.Adapter<AdapterAnonymous.ViewHolder> {

    private final List<Contact> mContacts;
    private final Context mContext;


    public AdapterAnonymous(Context context, List<Contact> contacts) {
        mContext = context;
        mContacts = contacts;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView receivedChemistryIconTextView;
        public TextView receivedChemistryNameTextView;
        public TextView receivedChemistryDurationTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            receivedChemistryIconTextView = itemView.findViewById(R.id.receivedChemistryIcon_TextView);
            receivedChemistryNameTextView = itemView.findViewById(R.id.receivedChemistryName_TextView);
            receivedChemistryDurationTextView = itemView.findViewById(R.id.receivedChemistryDuration_TextView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // get the contact from the tag of the clicked ViewHolder instance
            Contact contact = (Contact) view.getTag();

            // open a bottom sheet dialog and pass the contact to it
            BottomSheetDialog anonymousSheet = new BottomSheetDialog(mContext);
            View anonymousSheetView = LayoutInflater.from(mContext).inflate(R.layout.sheet_anonymous, null);

            TextView anonymousIconTextView = anonymousSheetView.findViewById(R.id.anonymousChemistryIcon_TextView);
            anonymousIconTextView.setText(Chemistries.getIconByKey(contact.getReceivedChemistry()));

            TextView anonymousNameTextView = anonymousSheetView.findViewById(R.id.anonymousChemistryName_TextView);
            anonymousNameTextView.setText(Chemistries.getNameByKey(contact.getReceivedChemistry()));

            TextView anonymousDurationTextView = anonymousSheetView.findViewById(R.id.anonymousDuration_TextView);

            long duration = (System.currentTimeMillis() - contact.getReceivedChemistryTime()) / 1000;

            if (duration < 60) {
                String durationText = "⏱ 0m";
                anonymousDurationTextView.setText(String.format("It's been %s since you received this chemistry.", durationText));
            }

            // Chemistry received less than 1 hour ago
            else if (duration < 3600) {
                long minutes = duration / 60;
                String durationText = String.format("%dm", minutes);
                anonymousDurationTextView.setText(String.format("It's been %s since you received this chemistry.", durationText));
            }

            // Chemistry received less than 1 day ago
            else if (duration < 86400) {
                long hours = duration / 3600;
                String durationText = String.format("%dh", hours);
                anonymousDurationTextView.setText(String.format("It's been %s since you received this chemistry.", durationText));
            }

            // Chemistry received less than 1 week ago
            else if (duration < 604800) {
                long days = duration / 86400;
                String durationText = String.format("%dd", days);
                anonymousDurationTextView.setText(String.format("It's been %s since you received this chemistry.", durationText));
            }

            // Chemistry received more than 1 week ago
            else {
                long weeks = duration / 604800;
                String durationText = String.format("%dw", weeks);
                anonymousDurationTextView.setText(String.format("It's been %s since you received this chemistry.", durationText));
            }

            Button revealButton = anonymousSheetView.findViewById(R.id.reveal_Button);
            revealButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                    popupMenu.getMenuInflater().inflate(R.menu.reveal_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.action_solve:
                                String name = contact.getName();
                                String shuffledName = UtilSolve.shuffle(name);
                                revealButton.setText(shuffledName);
                                break;
                            case R.id.action_cheat:
                                revealButton.setText(contact.getName());
                                break;
                        }
                        revealButton.setTextColor(Color.BLACK);
                        revealButton.setBackgroundColor(Color.TRANSPARENT);
                        return true;
                    });

                    popupMenu.show();
                }
            });

            anonymousSheet.setContentView(anonymousSheetView);
            anonymousSheet.show();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_anonymous, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact contact = mContacts.get(position);

        String receivedChemistry = contact.getReceivedChemistry();
        String icon = Chemistries.getIconByKey(receivedChemistry);
        holder.receivedChemistryIconTextView.setText(icon);
        String name = Chemistries.getNameByKey(receivedChemistry);
        holder.receivedChemistryNameTextView.setText(name);

        long duration = (System.currentTimeMillis() - contact.getReceivedChemistryTime()) / 1000;

        // Chemistry received less than 1 minute ago
        if (duration < 60) {
            String durationText = "⏱ 0m";
            holder.receivedChemistryDurationTextView.setText(durationText);
        }

        // Chemistry received less than 1 hour ago
        else if (duration < 3600) {
            long minutes = duration / 60;
            String durationText = String.format("⏱ %dm", minutes);
            holder.receivedChemistryDurationTextView.setText(durationText);
        }

        // Chemistry received less than 1 day ago
        else if (duration < 86400) {
            long hours = duration / 3600;
            String durationText = String.format("⏱ %dh", hours);
            holder.receivedChemistryDurationTextView.setText(durationText);
        }

        // Chemistry received less than 1 week ago
        else if (duration < 604800) {
            long days = duration / 86400;
            String durationText = String.format("⏱ %dd", days);
            holder.receivedChemistryDurationTextView.setText(durationText);
        }

        // Chemistry received more than 1 week ago
        else {
            long weeks = duration / 604800;
            String durationText = String.format("⏱ %dw", weeks);
            holder.receivedChemistryDurationTextView.setText(durationText);
        }

        holder.itemView.setTag(contact);
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }
}