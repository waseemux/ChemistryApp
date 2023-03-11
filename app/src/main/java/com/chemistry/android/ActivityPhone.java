package com.chemistry.android;

import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class ActivityPhone extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout of phone page
        setContentView(R.layout.activity_phone);

        // Initialize interactive elements of phone page
        EditText phoneNumberEditText = findViewById(R.id.phoneNumber_EditText);
        Button getPasscodeButton = findViewById(R.id.getPasscode_Button);

        // Initialize parameters for sign in object
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Initialize sign in object
        UtilSignIn utilSignIn = new UtilSignIn(auth, this, this);

        // Submit phone number to get passcode when 'Get passcode' button is clicked
        getPasscodeButton.setOnClickListener(view -> {

            // Get input phone number from text field
            String phoneNumber = phoneNumberEditText.getText().toString();

            // Format phone number to E.164 format before submitting
            String e164PhoneNumber = "+" + PhoneNumberUtils.formatNumberToE164(phoneNumber, Locale.getDefault().getCountry());

            // Request verification code for (formatted) phone number
            utilSignIn.sendVerificationCode(e164PhoneNumber);
        });
    }
}