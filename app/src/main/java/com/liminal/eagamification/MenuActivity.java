package com.liminal.eagamification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
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

public class MenuActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.rewardPointsButton);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RewardsActivity.class);
            startActivity(intent);
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

//        UserProfile angelPriya = new UserProfile("angelpriya@gmail.com","Angel","Priya","9988776655",5000,"f");
//        UserProfile bhaktijPatil = new UserProfile("bhaktij1999@gmail.com","Bhaktij","Patil","9158752433",69420,"k");
//
////        TextView textView = findViewById(R.id.nameTextView);
//
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("User Profile Table");
//        databaseReference.child(angelPriya.getUID()).setValue(angelPriya);
//        databaseReference.child(bhaktijPatil.getUID()).setValue(bhaktijPatil);
//
//        // Add a listener to update UI when User Profile is updated
//        ValueEventListener eventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // Read data from firebase
//                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
//                updateUI(userProfile);
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Read failed
//                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
//            }
//        };
//        databaseReference.child(bhaktijPatil.getUID()).addValueEventListener(eventListener);
//        databaseReference.child(angelPriya.getUID()).addListenerForSingleValueEvent(eventListener);
//
//        SharedPreferences sharedPreferences1 = getSharedPreferences("User_Profile_Details", Context.MODE_PRIVATE);
//        Log.d("user_name",sharedPreferences1.getString("user_name", null));
//    }
//
//    void updateUI(UserProfile userProfile)
//    {
//        Log.d("EAG_FIREBASE_DB", "Username : " + userProfile.firstName);
//    }
//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
