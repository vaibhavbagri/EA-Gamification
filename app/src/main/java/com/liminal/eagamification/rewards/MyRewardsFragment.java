package com.liminal.eagamification.rewards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyRewardsFragment extends Fragment {

    private List<RewardDetails> rewardDetailsList = new ArrayList<>();
    private MyRewardsAdapter myRewardsAdapter;
    private RecyclerView recyclerView;
    private DatabaseReference userRewardsReference;
    private static final int QRCODE_CAPTURE = 9001;
    private RewardDetails claimedReward;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_rewards, container, false);
        recyclerView = root.findViewById(R.id.recycler_view);
        myRewardsAdapter = new MyRewardsAdapter(rewardDetailsList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(myRewardsAdapter);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("User_Details", Context.MODE_PRIVATE);
        userRewardsReference = FirebaseDatabase.getInstance().getReference()
                .child("userProfileTable")
                .child(sharedPreferences.getString("id",""))
                .child("rewardDetails")
                .child("savedRewards");

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                RewardDetails rewardDetails = rewardDetailsList.get(position);
                claimedReward = rewardDetails;
                Intent intent = new Intent(getActivity(), QRScannerActivity.class);
                intent.putExtra("adminID",rewardDetails.adminID);
                startActivityForResult(intent,QRCODE_CAPTURE);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

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
                myRewardsAdapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };

        userRewardsReference.addValueEventListener(eventListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("REWARDS_FRAGMENT",data.toString()+requestCode+resultCode);
        if(requestCode == QRCODE_CAPTURE){
            if(resultCode == CommonStatusCodes.SUCCESS){
                userRewardsReference.child(claimedReward.rid).removeValue();
                Toast.makeText(getContext(),"CORRECT QR CODE",Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getContext(),"INCORRECT QR CODE",Toast.LENGTH_SHORT).show();
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
