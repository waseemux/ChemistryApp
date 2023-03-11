package com.chemistry.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ActivityPasscode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout of passcode page
        setContentView(R.layout.activity_passcode);

        // Initialize interactive elements of passcode page
        EditText passcodeEditText = findViewById(R.id.passcode_EditText);
        Button verifyPasscodeButton = findViewById(R.id.verifyPasscode_Button);

        // Get verification ID
        String verificationId = getIntent().getStringExtra("verificationId");

        // Initialize parameters for sign in object
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Context context = getApplicationContext();
        Activity activity = this;

        // Initialize sign in object
        UtilSignIn utilSignIn = new UtilSignIn(auth, context, activity);

        // Run passcode verification when 'Verify passcode' button is clicked
        verifyPasscodeButton.setOnClickListener(view -> {

            // Get input passcode from text field
            String passcode = passcodeEditText.getText().toString();

            // Verify passcode to sign in
            utilSignIn.verifyPasscode(verificationId, passcode);
        });
    }
}