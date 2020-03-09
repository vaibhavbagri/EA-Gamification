package com.liminal.eagamification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

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
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sharedPreferences = getSharedPreferences("User_Details", Context.MODE_PRIVATE);

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

        databaseReference = FirebaseDatabase.getInstance().getReference().child("userProfileTable");

        // Add a listener to update UI when User Profile is updated
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
//                Log.d("EAG_", String.valueOf(dataSnapshot.getValue()));
                updateUI((Long) dataSnapshot.child("rewardDetails").child("rewardPoints").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        Log.d("EAG_", sharedPreferences.getString("id",""));
        databaseReference.child(sharedPreferences.getString("id","")).addValueEventListener(eventListener);
    }

    void updateUI(long rewardPoints)
    {
        TextView rewardPointsView = findViewById(R.id.rewardPointsView);
        rewardPointsView.setText(String.valueOf(rewardPoints));
        Log.d("EAG_FIREBASE_DB", "Reward Points : " + rewardPoints);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}
