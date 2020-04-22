package com.liminal.eagamification.nav_menu_ui;


import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.liminal.eagamification.MenuActivity;
import com.liminal.eagamification.R;

import java.io.File;
import java.net.URI;

public class ProfileFragment extends Fragment {

    private TextView userNameTextView;

    private EditText userNameEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText phoneNoEditText;
    private EditText DOBEditText;

    private ImageView profilePictureView;

    private SharedPreferences sharedPreferences;
    private StorageReference profilePictureRef;
    private DatabaseReference userProfileReference;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get shared references for user's ID and email
        sharedPreferences = getActivity().getSharedPreferences("User_Details", Context.MODE_PRIVATE);

        // Create a reference to all profile fields
        userNameTextView = root.findViewById(R.id.userNameTextView);

        userNameEditText = root.findViewById(R.id.userNameEditText);
        firstNameEditText = root.findViewById(R.id.firstNameEditText);
        lastNameEditText = root.findViewById(R.id.lastNameEditText);
        phoneNoEditText = root.findViewById(R.id.phoneEditText);
        DOBEditText = root.findViewById(R.id.dobEditText);

        profilePictureView = root.findViewById(R.id.profilePictureview);

        Button changePicButton = root.findViewById(R.id.addPicButton);
        Button updateProfileButton = root.findViewById(R.id.updateProfileButton);

        userProfileReference = FirebaseDatabase.getInstance().getReference().child("userProfileTable");
        profilePictureRef = FirebaseStorage.getInstance().getReference().child(sharedPreferences.getString("id","")).child("ProfilePicture");

        // Add a listener to update UI when User Profile is updated
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase and update EditProfile layout
                updateUserProfileLayout(
                        (String) dataSnapshot.child("personalDetails").child("userName").getValue(),
                        (String) dataSnapshot.child("personalDetails").child("firstName").getValue(),
                        (String) dataSnapshot.child("personalDetails").child("lastName").getValue(),
                        (String) dataSnapshot.child("personalDetails").child("DOB").getValue(),
                        (String) dataSnapshot.child("personalDetails").child("mobileNo").getValue(),
                        (String) dataSnapshot.child("personalDetails").child("photoURL").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        userProfileReference.child(sharedPreferences.getString("id","")).addValueEventListener(eventListener);

        // Upload profile picture
        changePicButton.setOnClickListener(v -> chooseNewImage());
        // Update profile information
        updateProfileButton.setOnClickListener(v -> updateUserProfile());

        return root;
    }



    // Function to update Edit Profile Layout with current field values
    private void updateUserProfileLayout(String userName, String firstName, String lastName, String dob, String mobileNo, String photo_url)
    {
        // Set Text fields for all Edit texts
        if(userName != null)
        {
            userNameTextView.setText(userName + " ");
            userNameEditText.setText(userName);
        }

        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);

        if(dob != null)
            DOBEditText.setText(dob);

        if(mobileNo != null)
            phoneNoEditText.setText(mobileNo);

        Glide.with(getActivity().getApplicationContext()).load(Uri.parse(photo_url)).into(profilePictureView);

        Log.d("EAG_EDIT_PROFILE", "Username : " + userName + " First Name : " + firstName + " Last Name : " + lastName +  " DOB : " + dob + " Mobile No : " + mobileNo);
    }



    // Function to choose new image
    private void chooseNewImage()
    {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(android.content.Intent.createChooser(intent, "Select target augmented image"), 1);
    }



    // Get result of choose image here
    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == android.app.Activity.RESULT_OK && requestCode == 1)
            {
                // Get the URI of target image
                Uri imageURI = data.getData();

                // Upload to firebase
                profilePictureRef.putFile(imageURI).addOnSuccessListener(taskSnapshot -> profilePictureRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Upload link for profile picture to User Profile
                    userProfileReference.child(sharedPreferences.getString("id", "")).child("personalDetails").child("photoURL").setValue(uri.toString());
                }));
            }
        }
        catch (Exception e) {
            Log.e("EAG_CHOOSE_IMAGE", "Target image selection error ", e);
        }
    }



    // Function to upload new user details on Firebase
    private void updateUserProfile()
    {
        String userName = String.valueOf(userNameEditText.getText()).equals("") ? "NA" : String.valueOf(userNameEditText.getText());
        String firstName = String.valueOf(firstNameEditText.getText());
        String lastName = String.valueOf(lastNameEditText.getText());
        String DOB = String.valueOf(DOBEditText.getText()).equals("") ? "NA" : String.valueOf(DOBEditText.getText());
        String mobileNo = String.valueOf(phoneNoEditText.getText()).equals("") ? "NA" : String.valueOf(phoneNoEditText.getText());



        // Check validity of username
        if(checkUsernameValidity(userName))
        {
            DatabaseReference userNameDatabaseReference = FirebaseDatabase.getInstance().getReference().child("userNameTable");

            userNameDatabaseReference.orderByChild("userName").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // username is unique
                    if (!dataSnapshot.exists()) {
                        // Update user profile
                        userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("userName").setValue(userName);
                        userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("firstName").setValue(firstName);
                        userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("lastName").setValue(lastName);
                        userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("DOB").setValue(DOB);
                        userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("mobileNo").setValue(mobileNo);
                        // Update usernames table
                        userNameDatabaseReference.child(sharedPreferences.getString("id", "")).child("userName").setValue(userName);
                        Toast.makeText(getActivity(), "Updated successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(getActivity(), "Username already exists", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }});
        }
    }



    // Function to check validity of username
    private boolean checkUsernameValidity(String text) {
        // Set character limit of 4 for username
        if (text.length() < 4)
        {
            Toast.makeText(getActivity(), "Username must be atleast 4 letters long", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Only allow alpha numeric characters and underscores
        if (!text.matches("^[a-zA-Z0-9_]*$"))
        {
            Toast.makeText(getActivity(), "Username cannot contain special characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
