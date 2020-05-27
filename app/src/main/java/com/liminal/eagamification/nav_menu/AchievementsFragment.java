package com.liminal.eagamification.nav_menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.R;

public class AchievementsFragment extends Fragment{

    private DatabaseReference userDetailsDatabaseReference;

    // Achievement rewards
    final long PROFILE_COMPLETE_REWARD = 10;
    final long SHARE_APP_REWARD = 25;
    final long BUY_REWARD_REWARD = 30;
    final long VISIT_DASHBOARD_REWARD = 10;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("User_Details", Context.MODE_PRIVATE);
        userDetailsDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("userProfileTable")
                .child(sharedPreferences.getString("id",""));


        Button completeProfileButton = view.findViewById(R.id.completeProfileClaimButton);
        Button shareAppButton = view.findViewById(R.id.shareAppClaimButton);
        Button buyRewardButton = view.findViewById(R.id.buyRewardClaimButton);
        Button visitDashboardButton = view.findViewById(R.id.visitDashboardClaimButton);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check achievements status on firebase and update UI accordingly

                // Complete AR Explore profile
                if(dataSnapshot.child("profileCompleteStatus").getValue().equals("completed"))
                {
                    Log.d("EAG_Achievement_Status", "Complete Profile completed");
                    completeProfileButton.setEnabled(true);
                    completeProfileButton.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary, null));
                }
                else if(dataSnapshot.child("profileCompleteStatus").getValue().equals("claimed"))
                {
                    Log.d("EAG_Achievement_Status", "Complete Profile claimed");
                    completeProfileButton.setText("Claimed");
                    completeProfileButton.setTextColor(getActivity().getResources().getColor(R.color.colorAccent, null));
                }

                // Share app with friends
                if(dataSnapshot.child("appSharedStatus").getValue().equals("completed"))
                {
                    Log.d("EAG_Achievement_Status", "Invite Friends completed");
                    shareAppButton.setEnabled(true);
                    shareAppButton.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary, null));
                }
                else if(dataSnapshot.child("appSharedStatus").getValue().equals("claimed"))
                {
                    Log.d("EAG_Achievement_Status", "Invite Friends claimed");
                    shareAppButton.setText("Claimed");
                    shareAppButton.setTextColor(getActivity().getResources().getColor(R.color.colorAccent, null));
                }

                // Visit Dashboard to view statistics
                if(dataSnapshot.child("dashboardVisitedStatus").getValue().equals("completed"))
                {
                    Log.d("EAG_Achievement_Status", "Dashboard Visit completed");
                    visitDashboardButton.setEnabled(true);
                    visitDashboardButton.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary, null));
                }
                else if(dataSnapshot.child("appSharedStatus").getValue().equals("claimed"))
                {
                    Log.d("EAG_Achievement_Status", "Dashboard Visit claimed");
                    shareAppButton.setText("Claimed");
                    shareAppButton.setTextColor(getActivity().getResources().getColor(R.color.colorAccent, null));
                }

                // Buy a reward using coins
                if(dataSnapshot.child("rewardBoughtStatus").getValue().equals("completed"))
                {
                    Log.d("EAG_Achievement_Status", "Buy reward completed ");
                    buyRewardButton.setEnabled(true);
                    buyRewardButton.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary, null));
                }
                else if(dataSnapshot.child("rewardBoughtStatus").getValue().equals("claimed"))
                {
                    Log.d("EAG_Achievement_Status", "Buy reward claimed");
                    buyRewardButton.setText("Claimed");
                    buyRewardButton.setTextColor(getActivity().getResources().getColor(R.color.colorAccent, null));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        userDetailsDatabaseReference.child("statistics").child("achievements").addListenerForSingleValueEvent(eventListener);

        shareAppButton.setOnClickListener(v-> userDetailsDatabaseReference.child("rewardDetails").child("tickets").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userDetailsDatabaseReference.child("rewardDetails").child("tickets").setValue((long) dataSnapshot.getValue() + SHARE_APP_REWARD);
                userDetailsDatabaseReference.child("statistics").child("achievements").child("appSharedStatus").setValue("claimed");
                shareAppButton.setText("Claimed");
                shareAppButton.setTextColor(getActivity().getResources().getColor(R.color.colorAccent, null));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        }));

        return view;
    }
}
