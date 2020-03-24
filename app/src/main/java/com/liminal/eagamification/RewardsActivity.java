package com.liminal.eagamification;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RewardsActivity extends AppCompatActivity {

    private List<RewardDetails> rewardDetailsList = new ArrayList<>();
    private RewardsAdapter rewardsAdapter;
    private DatabaseReference databaseReference;
    private long rewardPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        rewardsAdapter = new RewardsAdapter(rewardDetailsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(rewardsAdapter);

        SharedPreferences sharedPreferences = getSharedPreferences("User_Details", Context.MODE_PRIVATE);
        DatabaseReference userRewardsReference = FirebaseDatabase.getInstance().getReference()
                .child("userProfileTable")
                .child(sharedPreferences.getString("id",""))
                .child("rewardDetails");

        ValueEventListener rewardsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                rewardPoints = (long) dataSnapshot.child("rewardPoints").getValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };

        userRewardsReference.addValueEventListener(rewardsEventListener);



        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                RewardDetails rewardDetails = rewardDetailsList.get(position);
                final Dialog inputTextDialog = new Dialog(RewardsActivity.this);
                inputTextDialog.setContentView(R.layout.dialog_reward_claim);

                Button cancelButton = inputTextDialog.findViewById(R.id.cancelButton);
                Button buyButton = inputTextDialog.findViewById(R.id.buyButton);

                TextView title = inputTextDialog.findViewById(R.id.title);
                TextView description = inputTextDialog.findViewById(R.id.description);
                TextView cost = inputTextDialog.findViewById(R.id.cost);

                title.setText(rewardDetails.getTitle());
                description.setText(rewardDetails.getDescription());
                cost.setText(String.valueOf(rewardDetails.getCost()));
                cancelButton.setOnClickListener(v -> inputTextDialog.dismiss());
                buyButton.setOnClickListener(v -> {
                    if(rewardDetails.getCost() > rewardPoints)
                        Toast.makeText(getApplicationContext(), "Sorry you have insufficient coins!", Toast.LENGTH_SHORT).show();
                    else
                    {
                        userRewardsReference.child("rewardPoints").setValue(rewardPoints - rewardDetails.getCost());
                        databaseReference.child(rewardDetails.getRid()).child("quantity").setValue(rewardDetails.getQuantity()-1);
                    }
                    inputTextDialog.dismiss();
                });

                inputTextDialog.show();

//                Toast.makeText(getApplicationContext(), rewardDetails.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        databaseReference = FirebaseDatabase.getInstance().getReference().child("rewardsTable");
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                rewardDetailsList.clear();
                for (DataSnapshot reward : dataSnapshot.getChildren()) {
                    String rid = reward.getKey();
                    String title = Objects.requireNonNull(reward.child("title").getValue()).toString();
                    String description = Objects.requireNonNull(reward.child("description").getValue()).toString();
                    long cost = (long) reward.child("cost").getValue();
                    long quantity = (long) reward.child("quantity").getValue();
                    RewardDetails rewardDetails = new RewardDetails(rid, title, description, cost, quantity);
                    rewardDetailsList.add(rewardDetails);
                    assert rid != null;
                    Log.d("Rewards_Activity", rid);
                }
                rewardsAdapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };

        databaseReference.addValueEventListener(eventListener);

    }
}
