package com.liminal.eagamification;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

import java.net.URI;

@IgnoreExtraProperties
public class UserProfile {

    // User details
    String ID;

    public String userName;
    public String email;
    public String firstName;
    public String lastName;
    public String photoURL;
    public String phoneNo;
    public String DOB;

    // Empty constructor needed for Firebase
    UserProfile()
    {

    }

    public UserProfile(String email, String firstName, String lastName, Uri photoURL, String ID)
    {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoURL = String.valueOf(photoURL);
        this.ID = ID;
        this.phoneNo = "NA";
        this.DOB = "NA";
        this.userName = "NA";
    }

    public UserProfile(String email, String firstName, String lastName, String photoURL, String ID, String phoneNo, String DOB, String userName)
    {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoURL = String.valueOf(photoURL);
        this.ID = ID;
        this.phoneNo = phoneNo;
        this.DOB = DOB;
        this.userName = userName;
    }
}