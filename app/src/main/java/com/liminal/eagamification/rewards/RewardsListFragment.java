package com.liminal.eagamification.rewards;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.R;
import com.liminal.eagamification.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RewardsListFragment extends Fragment {

    private List<RewardDetails> rewardDetailsList = new ArrayList<>();
    private List<String> savedRewardIDList = new ArrayList<>();

    private ClaimRewardsAdapter claimRewardsAdapter;
    private RecyclerView recyclerView;

    private DatabaseReference userDetailsReference;
    private DatabaseReference rewardDetailsReference;

    private long rewardPoints;
    private String category;

    RewardsListFragment(String category)
    {
        this.category = category;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_rewards_list, container, false);

        recyclerView = root.findViewById(R.id.recycler_view);
        claimRewardsAdapter = new ClaimRewardsAdapter(rewardDetailsList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(claimRewardsAdapter);

        Log.d("RewardsList", "On create view");
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("User_Details", Context.MODE_PRIVATE);
        Log.d("RewardsList", "On start");

        // Firebase references to user profile and reward details table
        userDetailsReference = FirebaseDatabase.getInstance().getReference().child("userProfileTable").child(sharedPreferences.getString("id",""));
        rewardDetailsReference = FirebaseDatabase.getInstance().getReference().child("rewardsTable").child(category);

        // Read amount of coins user has from firebase
        ValueEventListener rewardsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                rewardPoints = (long) dataSnapshot.getValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        userDetailsReference.child("rewardDetails").child("coins").addValueEventListener(rewardsEventListener);

        // Add listener to recycler view items to allow user to buy offers
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                RewardDetails rewardDetails = rewardDetailsList.get(position);
                if(savedRewardIDList.contains(rewardDetails.rid))
                    Toast.makeText(getContext(), "Reward already purchased.", Toast.LENGTH_SHORT).show();
                else {
                    // setup dialog box to allow purchase of rewards
                    final Dialog inputTextDialog = new Dialog(requireContext());
                    inputTextDialog.setContentView(R.layout.dialog_box_claim_reward);

                    Button cancelButton = inputTextDialog.findViewById(R.id.cancelButton);
                    Button buyButton = inputTextDialog.findViewById(R.id.buyButton);

                    TextView title = inputTextDialog.findViewById(R.id.title);
                    TextView description = inputTextDialog.findViewById(R.id.description);
                    TextView cost = inputTextDialog.findViewById(R.id.cost);

                    title.setText(rewardDetails.title);
                    description.setText(rewardDetails.description);
                    cost.setText(String.valueOf(rewardDetails.cost));

                    cancelButton.setOnClickListener(v -> inputTextDialog.dismiss());

                    buyButton.setOnClickListener(v -> {
                        // Notify the user that he is indeed poor
                        if (rewardDetails.cost > rewardPoints)
                            Toast.makeText(getContext(), "Insufficient coins in wallet.", Toast.LENGTH_SHORT).show();
                        // Purchase the reward
                        else {
                            // Update achievement status on firebase
                            userDetailsReference.child("statistics").child("achievements").child("rewardBoughtStatus").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.getValue().equals("incomplete"))
                                        userDetailsReference.child("statistics").child("achievements").child("rewardBoughtStatus").setValue("completed");
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Read failed
                                    Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
                                }
                            });

                            // Process the transaction
                            userDetailsReference.child("rewardDetails").child("coins").setValue(rewardPoints - rewardDetails.cost);
                            rewardDetailsReference.child(rewardDetails.rid).child("quantity").setValue(rewardDetails.quantity - 1);
                            userDetailsReference.child("rewardDetails").child("savedRewards").child(rewardDetails.rid).setValue(rewardDetails);

                            Toast.makeText(getContext(), "Transaction successful.", Toast.LENGTH_SHORT).show();
                        }
                        inputTextDialog.dismiss();
                    });

                    inputTextDialog.show();
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        // Add purchased rewards to user profile
        ValueEventListener savedRewardsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                savedRewardIDList.clear();
                for (DataSnapshot reward : dataSnapshot.getChildren()) {
                    savedRewardIDList.add(reward.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        userDetailsReference.child("rewardDetails").child("savedRewards").addValueEventListener(savedRewardsEventListener);

        // Retrieve rewards from firebase
        ValueEventListener rewardDetailsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                rewardDetailsList.clear();
                for (DataSnapshot reward : dataSnapshot.getChildren()) {
                    String rid = reward.getKey();
                    String adminID = Objects.requireNonNull(reward.child("adminID").getValue()).toString();
                    String title = Objects.requireNonNull(reward.child("title").getValue()).toString();
                    String description = Objects.requireNonNull(reward.child("description").getValue()).toString();
                    long cost = (long) reward.child("cost").getValue();
                    long quantity = (long) reward.child("quantity").getValue();
                    RewardDetails rewardDetails = new RewardDetails(rid, adminID, title, description, cost, quantity);
                    rewardDetailsList.add(rewardDetails);
                    Log.d("EAG_REWARDS", rid);
                }
                claimRewardsAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        rewardDetailsReference.addValueEventListener(rewardDetailsListener);
    }
}
