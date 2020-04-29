package com.liminal.eagamification.nav_menu;


import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.liminal.eagamification.R;

import java.time.Year;
import java.util.Calendar;
import java.util.Date;

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
        Button chooseDateButton = root.findViewById(R.id.selectDOBButton);
        CardView updateProfileButton = root.findViewById(R.id.updateProfileCardView);
        CardView inviteFriendsButton = root.findViewById(R.id.inviteFriendsCardView);

        userProfileReference = FirebaseDatabase.getInstance().getReference().child("userProfileTable");
        profilePictureRef = FirebaseStorage.getInstance().getReference().child(sharedPreferences.getString("id","")).child("ProfilePicture");

        // Add a listener to update UI when User Profile is updated
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase and update EditProfile layout
                updateUserProfileLayout(
                        (String) dataSnapshot.child("personalDetails").child("username").getValue(),
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
        // Invite friends to the application
        inviteFriendsButton.setOnClickListener(v -> shareApp());
        // Choose date from calender
        chooseDateButton.setOnClickListener(v-> getDate());


        return root;
    }


    // Function to get date from DatePicker
    private void getDate()
    {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int currYear = c.get(Calendar.YEAR);
        int currMonth = c.get(Calendar.MONTH);
        int currDay = c.get(Calendar.DAY_OF_MONTH);

        // Setup DatePicker Dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, year, monthOfYear, dayOfMonth) -> DOBEditText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year), currYear, currMonth, currDay);
        datePickerDialog.show();
    }


    // Function to allow user to share the application link
    private void shareApp()
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Join me in AR Explore ! \n\n https://play.google.com/store/apps/details?id=io.gartic.Gartic");
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }



    // Function to update Edit Profile Layout with current field values
    private void updateUserProfileLayout(String username, String firstName, String lastName, String dob, String mobileNo, String photo_url)
    {
        // Set Text fields for all Edit texts
        if(!username.equals("Anon"))
        {
            userNameTextView.setText(username + " ");
            userNameEditText.setText(username);
        }
        if(!dob.equals("NA"))
            DOBEditText.setText(dob);
        if(!mobileNo.equals("NA"))
            phoneNoEditText.setText(mobileNo);

        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);

        Glide.with(getActivity().getApplicationContext()).load(Uri.parse(photo_url)).into(profilePictureView);

        Log.d("EAG_EDIT_PROFILE", "Username : " + username + " First Name : " + firstName + " Last Name : " + lastName +  " DOB : " + dob + " Mobile No : " + mobileNo);
    }



    // Function to choose new image
    private void chooseNewImage()
    {
        android.content.Intent intent = new android.content.Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(android.content.Intent.createChooser(intent, "Choose a Picture"), 1);
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
        String username = String.valueOf(userNameEditText.getText()).equals("") ? "Anon" : String.valueOf(userNameEditText.getText());
        String firstName = String.valueOf(firstNameEditText.getText()).equals("")? sharedPreferences.getString("firstName","John") : String.valueOf(firstNameEditText.getText());
        String lastName = String.valueOf(lastNameEditText.getText()).equals("")? sharedPreferences.getString("firstName","Doe") : String.valueOf(lastNameEditText.getText());
        String DOB = String.valueOf(DOBEditText.getText()).equals("") ? "NA" : String.valueOf(DOBEditText.getText());
        String mobileNumber = String.valueOf(phoneNoEditText.getText()).equals("") ? "NA" : String.valueOf(phoneNoEditText.getText());

        if(isUsernameValid(username) && isNameValid(firstName) && isNameValid(lastName) && isMobileNumberValid(mobileNumber))
        {
            // Check if chosen username is available
            DatabaseReference userNameDatabaseReference = FirebaseDatabase.getInstance().getReference().child("userNameTable");
            userNameDatabaseReference.orderByChild("username").equalTo(username)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // username is unique
                            if (!dataSnapshot.exists())
                            {
                                if(username != "Anon")
                                    // Update usernames table
                                    userNameDatabaseReference.child(sharedPreferences.getString("id", "")).child("username").setValue(username);

                                // Update user profile
                                userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("username").setValue(username);
                                userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("firstName").setValue(firstName);
                                userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("lastName").setValue(lastName);
                                userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("DOB").setValue(DOB);
                                userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("mobileNo").setValue(mobileNumber);
                            }
                            else
                                Toast.makeText(getActivity(), "Username already exists", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
        }
    }



    // Function to check validity of mobile number
    private boolean isMobileNumberValid(String text)
    {
        // Only allow numbers with + and -
        if (!text.matches("^[-a-zA-Z0-9+]*$") && !text.equals("NA"))
        {
            Toast.makeText(getActivity(), "Mobile number cannot contain alphabets or special characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Function to check validity of first name and last name
    private boolean isNameValid(String text)
    {
        // Set character limit of 2 for name
        if (text.length() < 3)
        {
            Toast.makeText(getActivity(), "Please enter a valid name", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Only allow alphabets
        if (!text.matches("^[a-zA-Z]*$"))
        {
            Toast.makeText(getActivity(), "Name cannot contain numbers or special characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Function to check validity of username
    private boolean isUsernameValid(String text) {

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
