package com.chemistry.android;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ActivityDetail extends AppCompatActivity {

    private EditText birthdayEditText;
    private RadioGroup genderRadioGroup;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout of detail page
        setContentView(R.layout.activity_detail);

        // Initialize interactive elements of detail page
        birthdayEditText = findViewById(R.id.birthday_EditText);
        genderRadioGroup = findViewById(R.id.gender_RadioGroup);
        Button saveDetailsButton = findViewById(R.id.saveDetails_Button);

        calendar = Calendar.getInstance();

        // Save details on firebase when 'Save details' button is clicked
        saveDetailsButton.setOnClickListener(view -> {

            // Get input birthday in milliseconds from calendar
            long birthdayInMillis = calendar.getTimeInMillis();

            // Get selected gender from radio group
            int checkedRadioButtonId = genderRadioGroup.getCheckedRadioButtonId();
            RadioButton checkedRadioButton = findViewById(checkedRadioButtonId);
            String gender = checkedRadioButton.getText().toString();

            // Save details to firebase
            DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference("users");
            String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
            assert phoneNumber != null;
            usersDB.child(phoneNumber).child("birthday").setValue(birthdayInMillis);
            usersDB.child(phoneNumber).child("gender").setValue(gender);

            // Handover to main activity
            Intent intent = new Intent(ActivityDetail.this, ActivityMain.class);
            startActivity(intent);
        });

        // Set on click listener for birthday EditText to open date picker
        birthdayEditText.setOnClickListener(view -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Set max date allowed to be 15 years ago from today
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, -15);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year1, month1, day1) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(Calendar.YEAR, year1);
                selectedDate.set(Calendar.MONTH, month1);
                selectedDate.set(Calendar.DAY_OF_MONTH, day1);

                // Check if selected date is older than 15 years from today
                if (selectedDate.after(maxDate)) {
                    // Clear the text in the EditText
                    birthdayEditText.setText("");
                    // Show an error message
                    Toast.makeText(this, "You must be at least 15 years old to use this app", Toast.LENGTH_SHORT).show();
                } else {
                    // Set birthdate in MM/DD/YYYY format in EditText
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", month1 + 1, day1, year1);
                    birthdayEditText.setText(date);
                }
            }, year, month, day);

            // Set max date allowed to be 15 years ago from today
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

            // Show date picker
            datePickerDialog.show();
        });
    }
}