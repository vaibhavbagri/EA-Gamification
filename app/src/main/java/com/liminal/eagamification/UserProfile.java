package com.liminal.eagamification;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

import java.net.URI;

@IgnoreExtraProperties
class UserProfile {

    // User details
    String ID;

    public String email;
    public String firstName;
    public String lastName;
    public String photoURL;

    // Empty constructor needed for Firebase
    UserProfile()
    {

    }

    UserProfile(String email, String firstName, String lastName, Uri photoURL, String ID)
    {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoURL = String.valueOf(photoURL);
        this.ID = ID;
    }
}

