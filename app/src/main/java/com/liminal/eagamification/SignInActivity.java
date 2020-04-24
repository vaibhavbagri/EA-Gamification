package com.liminal.eagamification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    // Time for which splash screen should be shown
    private int splashScreenTime = 1500;

    // Account that stores user details
    private GoogleSignInAccount account;

    // Database reference
    private DatabaseReference userProfileReference;

    // Firebase auth reference
    private FirebaseAuth mAuth;

    // Shared preferences to store user details
    SharedPreferences sharedPreferences;

    // Constants
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        // get reference to user details
        sharedPreferences = getSharedPreferences("User_Details", Context.MODE_PRIVATE);

        // Get reference to User Profile table
        userProfileReference = FirebaseDatabase.getInstance().getReference().child("userProfileTable");

        // Show a loader on the splash screen
        ImageView loaderView = findViewById(R.id.loadingGifView);
        Glide.with(this).asGif().load(R.drawable.loading_cube).into(loaderView);

        // Request location permission
        new Handler().postDelayed(this::requestLocationPermission, splashScreenTime);
    }



    // Function is called when location permission is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION &&  grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            // Authenticate user when location permission is granted
            authenticateUser();
        }
        else
        {
            // Exit application if permission is denied
            Toast.makeText(this, "AR Explore needs access to user location to function. Please allow access to continue.", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(this::finish, splashScreenTime);
        }
    }



    // Function to request user location
    private void requestLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            // Request permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        else
            // Authenticate user when location permission is granted
            authenticateUser();
    }



    // Function to authenticate user using Google sign-in
    private void authenticateUser()
    {
        // Check if user is already signed in
        account = GoogleSignIn.getLastSignedInAccount(this);

        // User is already signed in
        if(account != null)
        {
            // Add account ID to shared preferences
            sharedPreferences.edit().putString("id", account.getId()).apply();
            Log.d("EAG_USER_LOGIN","User logged in with account : " + account.getEmail());
            // Start the application
            startActivity(new Intent(this, MainActivity.class));
            // Finish sign-in activity
            finish();
        }
        // User hasn't already signed in
        else
        {
            // Configure Google sign-in
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();

            // Start activity for Sign-in
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 0);
        }
    }



    // Obtain Sign-in result here
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 0) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Get account with which user has logged in
                account = task.getResult(ApiException.class);
                // Firebase authentication
                firebaseAuthWithGoogle(account);

                // Add account ID to shared preferences
                sharedPreferences.edit().putString("id", account.getId()).apply();

                // Check if account ID exists in the database
                ValueEventListener idListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // existing user log-in
                        if(dataSnapshot.hasChild(Objects.requireNonNull(account.getId())))
                        {
                            Log.d("EAG_GOOGLE_AUTH", "Signed in with account : " + account.getEmail());
                        }
                        // new user log-in
                        else
                        {
                            Log.d("EAG_GOOGLE_AUTH", "New user with signed in with account : " + account.getEmail());
                            addUserProfile();
                        }
                        // Start the application
                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        // Finish sign-in activity
                        finish();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
                    }
                };
                userProfileReference.addListenerForSingleValueEvent(idListener);
            }

            // Handle Sign-in failures
            catch (ApiException e) {
                if(e.getStatusCode() == 12501)
                    Toast.makeText(SignInActivity.this, "Please sign-in to continue", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(SignInActivity.this, "Sign-in failed, please try again", Toast.LENGTH_SHORT).show();
                // Reauthenticate if Sign-in fails
                authenticateUser();
                Log.d("EAG_GOOGLE_AUTH", "SignInResult : failed code = " + e.getStatusCode());
            }
        }
    }



    // Function to add new user to firebase
    private void addUserProfile()
    {
        Log.d("EAG_USER_PROFILE", "Adding new User to Database \nEmail : " + account.getEmail() + " Name : " + account.getGivenName() + " " + account.getFamilyName() + " Photo URL : " + account.getPhotoUrl());

        // Add profile to User Profile table
        userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("email").setValue(account.getEmail());
        userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("firstName").setValue(account.getGivenName());
        userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("lastName").setValue(account.getFamilyName());
        userProfileReference.child(sharedPreferences.getString("id","")).child("personalDetails").child("photoURL").setValue(String.valueOf(account.getPhotoUrl()));

        // Setup user currency
        userProfileReference.child(sharedPreferences.getString("id","")).child("rewardDetails").child("coins").setValue(0);
        userProfileReference.child(sharedPreferences.getString("id","")).child("rewardDetails").child("tickets").setValue(0);

        // Setup user statistics
        userProfileReference.child(sharedPreferences.getString("id","")).child("statistics").child("general").child("campExperiences").setValue(0);
        userProfileReference.child(sharedPreferences.getString("id","")).child("statistics").child("general").child("markersScanned").setValue(0);
        userProfileReference.child(sharedPreferences.getString("id","")).child("statistics").child("general").child("itemsCollected").setValue(0);
        userProfileReference.child(sharedPreferences.getString("id","")).child("statistics").child("general").child("rewardsClaimed").setValue(0);
    }



    // Firebase Authentication (No idea how this works)
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("EAG_MAIN_ACTIVITY", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("EAG_MAIN_ACTIVITY", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                    }
                    else {
                        // If sign in fails, display a message to the user.
                        Log.w("EAG_MAIN_ACTIVITY", "signInWithCredential:failure", task.getException());
                    }
                });
    }
}
