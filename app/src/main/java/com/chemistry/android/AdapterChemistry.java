package com.chemistry.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AdapterChemistry extends ArrayAdapter<Chemistry> {

    // Constructor
    public AdapterChemistry(Context context, List<Chemistry> chemistries) {
        super(context, 0, chemistries);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Chemistry chemistry = getItem(position);

        // Initialize layout for chemistry item
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chemistry, parent, false);
        }

        // Initialize elements of chemistry item that will be populated by data
        TextView iconTextView = (TextView) convertView.findViewById(R.id.chemistryIcon_TextView);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.chemistryName_TextView);
        TextView countTextView = (TextView) convertView.findViewById(R.id.chemistryCount_TextView);

        /* ---------- POPULATE ---------- */

        // Populate icon text view
        iconTextView.setText(chemistry.getIcon());

        // Populate name text view
        nameTextView.setText(chemistry.getName());

        // Populate count text view
        countTextView.setText(String.format(getContext().getString(R.string.chemistry_count), chemistry.getCount()));

        return convertView;
    }
}