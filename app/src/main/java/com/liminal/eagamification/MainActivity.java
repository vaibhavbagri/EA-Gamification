package com.liminal.eagamification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get user details stored in shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("User_Details", Context.MODE_PRIVATE);


//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupWithNavController(navigationView, navController);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ImageButton expandMenuButton = findViewById(R.id.expandMenuButton);

        // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_profile,
                R.id.nav_achievements,
                R.id.nav_dashboard,
                R.id.nav_about_us,
                R.id.nav_sign_out)
                .setDrawerLayout(drawer)
                .build();

        DatabaseReference userProfileReference = FirebaseDatabase.getInstance().getReference().child("userProfileTable");

        // Add a listener to update UI when User Profile is updated
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                updateUserProfileLayout(
                        (long) dataSnapshot.child("rewardDetails").child("coins").getValue(),
                        (long) dataSnapshot.child("rewardDetails").child("tickets").getValue(),
                        (String) dataSnapshot.child("personalDetails").child("firstName").getValue(),
                        (String) dataSnapshot.child("personalDetails").child("photoURL").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        userProfileReference.child(sharedPreferences.getString("id","")).addValueEventListener(eventListener);

        // Inflate the navigation drawer
        expandMenuButton.setOnClickListener(v -> mAppBarConfiguration.getDrawerLayout().openDrawer(Gravity.LEFT));

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }



    // Function to load user details into UI
    private void updateUserProfileLayout(long coins, long tickets, String userName, String photoURL)
    {
        // Update username
        TextView userNameView = findViewById(R.id.userNameView);
        userNameView.setText("Hi " + userName + "!");

        // Update coins and tickets
        TextView coinsView = findViewById(R.id.coinPointsView);
        coinsView.setText(coins + " ");
        TextView ticketsView = findViewById(R.id.ticketPointsView);
        ticketsView.setText(tickets + " ");

        // Update profile picture
        ImageView profilePictureView = findViewById(R.id.profilePictureview);
        Glide.with(this).load(Uri.parse(photoURL)).into(profilePictureView);

        Log.d("EAG_UPDATE_PROFILE", "Username : " + userName + " Coins : " + coins + " Tickets : " + tickets);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}