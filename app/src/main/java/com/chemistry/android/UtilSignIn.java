package com.chemistry.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UtilSignIn {
    private final FirebaseAuth mAuth;
    private final Context mContext;
    private final Activity mActivity;
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    // Constructor
    public UtilSignIn(FirebaseAuth auth, Context context, Activity activity) {
        mAuth = auth;
        mContext = context;
        mActivity = activity;

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // Verification completed → sign in
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signIn(credential);
            }

            // Verification failed → go to phone page
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Intent intent = new Intent(mContext, ActivityPhone.class);
                mActivity.startActivity(intent);
                Toast.makeText(mContext, "Verification failed, please try again!", Toast.LENGTH_SHORT).show();
            }

            // Passcode sent → go to passcode page
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Intent intent = new Intent(mContext, ActivityPasscode.class);
                intent.putExtra("verificationId", verificationId);
                mActivity.startActivity(intent);
            }
        };
    }

    // Method to send passcode to phone number
    public void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                                // Phone number to verify
                60,                                         // Timeout duration
                java.util.concurrent.TimeUnit.SECONDS,      // Unit of timeout (seconds)
                mActivity,                                  // Activity (for callback binding)
                mCallbacks                                  // Callbacks (for verification)
        );
    }

    // Method to verify passcode
    public void verifyPasscode(String verificationId, String passcode) {
        signIn(PhoneAuthProvider.getCredential(verificationId, passcode));
    }

    // Method to sign in
    private void signIn(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {

            // Sign in successful → go to home page
            if (task.isSuccessful()) {

                String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
                DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("users");
                assert phoneNumber != null;
                DatabaseReference name = usersDB.child(phoneNumber).child("name");
                name.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Details already exist, skip ActivityDetail
                            Intent intent = new Intent(mContext, ActivityMain.class);
                            mActivity.startActivity(intent);
                        } else {
                            // Details don't exist, go to ActivityDetail
                            Intent intent = new Intent(mContext, ActivityPicture.class);
                            mActivity.startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            // Sign in failed → go to phone page
            else {
                Intent intent = new Intent(mContext, ActivityPhone.class);
                mActivity.startActivity(intent);
                Toast.makeText(mContext, "Failed to sign in, please try again!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to check if user is signed in
    public boolean isSignedIn() {
        return mAuth.getCurrentUser() != null;
    }

    // Method to check sign in status and route to appropriate page
    public void checkSignInAndRoute() {

        // Sign in is true → go to home page
        if (mAuth.getCurrentUser() != null) {

            String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
            DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("users");
            assert phoneNumber != null;
            DatabaseReference name = usersDB.child(phoneNumber).child("name");
            name.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Details already exist, skip ActivityDetail
                        Intent intent = new Intent(mContext, ActivityMain.class);
                        mActivity.startActivity(intent);
                    } else {
                        // Details don't exist, go to ActivityDetail
                        Intent intent = new Intent(mContext, ActivityPicture.class);
                        mActivity.startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // Sign in is false, go to phone page
        else {
            Intent intent = new Intent(mContext, ActivityPhone.class);
            mActivity.startActivity(intent);
        }
    }
}