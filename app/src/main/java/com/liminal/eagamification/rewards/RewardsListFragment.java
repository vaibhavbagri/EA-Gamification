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
    private DatabaseReference databaseReference;
    private DatabaseReference userRewardsReference;
    private DatabaseReference savedRewardsReference;
    private long rewardPoints;
    private String category;

    RewardsListFragment(String category) {
        this.category = category;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_rewards_list, container, false);
        recyclerView = root.findViewById(R.id.recycler_view);
        claimRewardsAdapter = new ClaimRewardsAdapter(rewardDetailsList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(claimRewardsAdapter);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences("User_Details", Context.MODE_PRIVATE);
        userRewardsReference = FirebaseDatabase.getInstance().getReference()
                .child("userProfileTable")
                .child(sharedPreferences.getString("id",""))
                .child("rewardDetails");

        savedRewardsReference = FirebaseDatabase.getInstance().getReference()
                .child("userProfileTable")
                .child(sharedPreferences.getString("id",""))
                .child("rewardDetails")
                .child("savedRewards");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("rewardsTable").child(category);

        ValueEventListener rewardsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                rewardPoints = (long) dataSnapshot.child("coins").getValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };

        userRewardsReference.addValueEventListener(rewardsEventListener);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                RewardDetails rewardDetails = rewardDetailsList.get(position);
                if(savedRewardIDList.contains(rewardDetails.rid))
                    Toast.makeText(getContext(), "You have already purchased this reward.", Toast.LENGTH_SHORT).show();
                else {
                    final Dialog inputTextDialog = new Dialog(Objects.requireNonNull(getContext()));
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
                        if (rewardDetails.cost > rewardPoints)
                            Toast.makeText(getContext(), "Sorry you have insufficient coins!", Toast.LENGTH_SHORT).show();
                        else {
                            userRewardsReference.child("coins").setValue(rewardPoints - rewardDetails.cost);
                            databaseReference.child(rewardDetails.rid).child("quantity").setValue(rewardDetails.quantity - 1);
                            userRewardsReference.child("savedRewards").child(rewardDetails.rid).setValue(rewardDetails);
                            Toast.makeText(getContext(), "Congrats! Reward purchased.", Toast.LENGTH_SHORT).show();
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

        savedRewardsReference.addValueEventListener(savedRewardsEventListener);

        ValueEventListener eventListener = new ValueEventListener() {
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
                    assert rid != null;
                    Log.d("Rewards_Activity", rid);
                }
                claimRewardsAdapter.notifyDataSetChanged();
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
