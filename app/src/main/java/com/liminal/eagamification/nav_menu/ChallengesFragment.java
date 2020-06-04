package com.liminal.eagamification.nav_menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ChallengesFragment extends Fragment {

    private DatabaseReference userDatabaseReference;

    // Adapters for recycler views
    private ChallengesAdapter weeklyChallengesAdapter;
    private ChallengesAdapter dailyChallengesAdapter;

    // Array lists for recycler views
    private List<Challenge> weeklyChallengeList = new ArrayList<>();
    private List<Challenge> dailyChallengeList = new ArrayList<>();

    // Text view for recycler views
    private TextView weeklyChallengesTimer;
    private TextView dailyChallengesTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_challenges, container, false);
        // Weekly Challenges
        weeklyChallengeList.clear();
        RecyclerView weeklyChallengesRecyclerView = root.findViewById(R.id.weeklyChallengesRecyclerView);
        weeklyChallengesAdapter = new ChallengesAdapter(weeklyChallengeList, position -> {
            if(claimReward(weeklyChallengeList.get(position)))
                dismissFragment();
        });
        RecyclerView.LayoutManager weeklyLayoutManager = new LinearLayoutManager(getContext());
        weeklyChallengesRecyclerView.setLayoutManager(weeklyLayoutManager);
        weeklyChallengesRecyclerView.setAdapter(weeklyChallengesAdapter);
        weeklyChallengesTimer = root.findViewById(R.id.weeklyChallengesTimer);

        // Daily Challenges
        dailyChallengeList.clear();
        RecyclerView dailyChallengesRecyclerView = root.findViewById(R.id.dailyChallengesRecyclerView);
        dailyChallengesAdapter = new ChallengesAdapter(dailyChallengeList, position -> {
            if(claimReward(dailyChallengeList.get(position)))
                dismissFragment();
        });
        RecyclerView.LayoutManager dailyLayoutManager = new LinearLayoutManager(getContext());
        dailyChallengesRecyclerView.setLayoutManager(dailyLayoutManager);
        dailyChallengesRecyclerView.setAdapter(dailyChallengesAdapter);
        dailyChallengesTimer = root.findViewById(R.id.dailyChallengesTimer);

        // Button to dismiss popup
        Button button = root.findViewById(R.id.quitMissionsPopupButton);
        button.setOnClickListener(v -> dismissFragment());

        return root;
    }

    private void dismissFragment() {
        getParentFragmentManager().beginTransaction().remove(getParentFragmentManager().findFragmentById(R.id.popupFrameLayout)).commit();
    }

    // Function to allow user to claim challenge rewards
    private boolean claimReward(Challenge challenge) {
        if(challenge.progress < challenge.target) {
            Toast.makeText(getContext(), "You have not yet completed this challenge", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            userDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!(boolean) dataSnapshot.child("statistics").child("challenges").child(challenge.challengeType).child(String.valueOf(challenge.position)).child("isClaimed").getValue()) {
                        long currentRewardPoints = (long) dataSnapshot.child("rewardDetails").child(challenge.rewardType).getValue();
                        currentRewardPoints += challenge.rewardPoints;
                        userDatabaseReference.child("rewardDetails").child(challenge.rewardType).setValue(currentRewardPoints);
                        userDatabaseReference.child("statistics").child("challenges").child(challenge.challengeType).child(String.valueOf(challenge.position)).child("isClaimed").setValue(true);
                        Toast.makeText(getContext(), "Congratulations, reward points claimed!", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getContext(), "You have already claimed this reward", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Read failed
                    Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
                }
            });
            return true;
        }
    }

    // Function to setup daily and weekly challenges
    private void challenge_setup(long current_ts, boolean isFirstLogin, String challengeType, ChallengesAdapter challengesAdapter, List<Challenge> challengeList) {
        FirebaseDatabase.getInstance().getReference().child("challengesTable").child(challengeType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot challengeID : dataSnapshot.getChildren()) {
                    if((long)challengeID.child("timestampStart").getValue() < current_ts && (long)challengeID.child("timestampEnd").getValue() > current_ts) {
                        String description = Objects.requireNonNull(challengeID.child("description").getValue()).toString();
                        Log.d("EAG_CHALLENGE", "Challenge found : " + description);

                        String rewardType = Objects.requireNonNull(challengeID.child("rewardType").getValue()).toString();
                        long rewardPoints = (long) challengeID.child("rewardPoints").getValue();
                        String activityName = Objects.requireNonNull(challengeID.child("activityID").getValue()).toString();
                        String stat = Objects.requireNonNull(challengeID.child("stat").getValue()).toString();
                        long target = (long) challengeID.child("target").getValue();
                        long challengePosition = (long) challengeID.child("challengePosition").getValue();

                        //Check the current stat value and stored stat value at the start to calculate progress
                        userDatabaseReference.child("statistics").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                long stat_value = 0;
                                if(dataSnapshot.hasChild("activityBased")) {
                                    if (dataSnapshot.child("activityBased").hasChild(activityName))
                                        stat_value = (long) dataSnapshot.child("activityBased").child(activityName).child(stat).getValue();
                                }
                                else
                                    userDatabaseReference.child("statistics").child("activityBased").child(activityName).child(stat).setValue(0);

                                long progress = 0;
                                boolean isClaimed = false;

                                if (isFirstLogin) {
                                    userDatabaseReference.child("statistics").child("challenges").child(challengeType).child(String.valueOf(challengePosition)).child("value").setValue(stat_value);
                                    userDatabaseReference.child("statistics").child("challenges").child(challengeType).child(String.valueOf(challengePosition)).child("isClaimed").setValue(isClaimed);
                                } else {
                                    long stored_value = (long) dataSnapshot.child("challenges").child(challengeType).child(String.valueOf(challengePosition)).child("value").getValue();
                                    isClaimed = (boolean) dataSnapshot.child("challenges").child(challengeType).child(String.valueOf(challengePosition)).child("isClaimed").getValue();
                                    progress = stat_value - stored_value;
                                }

                                Challenge challenge = new Challenge(challengeType, progress, description, rewardType, rewardPoints, activityName, target, stat, isClaimed, challengePosition);
                                challengeList.add(challenge);
                                Log.d("EAG_CHALLENGE", challengeList.toString());
                                challengesAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        if(challengePosition == 1){
                            long ts_secs = ((long)challengeID.child("timestampEnd").getValue() - current_ts)/1000;
                            long days = ts_secs / (3600 * 24);
                            long hours = 1 + (ts_secs / 3600) % 24;
                            long mins = 0;
                            if(hours == 1)
                                mins = (ts_secs / 60) % 60;
                            if(challengeType.equals("weekly")){
                                if(days == 0) {
                                    if(hours == 1)
                                        weeklyChallengesTimer.setText(mins + " mins to go ");
                                    else
                                        weeklyChallengesTimer.setText(hours + " hours to go ");
                                }
                                else
                                    weeklyChallengesTimer.setText(days + " days, " + hours + " hours to go ");
                            }else{
                                if(hours == 1)
                                    dailyChallengesTimer.setText(mins + " mins to go ");
                                else
                                    dailyChallengesTimer.setText(hours + " hours to go ");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("User_Details", Context.MODE_PRIVATE);

        userDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("userProfileTable")
                .child(sharedPreferences.getString("id",""));

        //Check if first login of the day and week
        userDatabaseReference.child("loginDetails").child("currentTimestamp").setValue(ServerValue.TIMESTAMP);
        userDatabaseReference.child("loginDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long current_ts = (long) dataSnapshot.child("currentTimestamp").getValue();
                long previous_ts = (long) dataSnapshot.child("previousTimestamp").getValue();
                Calendar current_calender = Calendar.getInstance();
                Calendar previous_calendar = Calendar.getInstance();
                current_calender.setTimeInMillis(current_ts);
                previous_calendar.setTimeInMillis(previous_ts);

                if (current_calender.get(Calendar.YEAR) > previous_calendar.get(Calendar.YEAR)) {
                    challenge_setup(current_ts, true, "daily", dailyChallengesAdapter, dailyChallengeList);
                    challenge_setup(current_ts, true, "weekly", weeklyChallengesAdapter, weeklyChallengeList);
                }
                else {
                    if (current_calender.get(Calendar.DAY_OF_YEAR) > previous_calendar.get(Calendar.DAY_OF_YEAR)) {
                        Log.d("EAG_TIME", "First login of the day");
                        challenge_setup(current_ts, true, "daily", dailyChallengesAdapter, dailyChallengeList);
                    } else {
                        Log.d("EAG_TIME", "Repeated login (daily)");
                        challenge_setup(current_ts, false, "daily", dailyChallengesAdapter, dailyChallengeList);
                    }

                    if (current_calender.get(Calendar.WEEK_OF_YEAR) > previous_calendar.get(Calendar.WEEK_OF_YEAR)) {
                        Log.d("EAG_TIME", "First login of the week");
                        challenge_setup(current_ts, true, "weekly", weeklyChallengesAdapter, weeklyChallengeList);
                    } else {
                        Log.d("EAG_TIME", "Repeated login (weekly)");
                        challenge_setup(current_ts, false, "weekly", weeklyChallengesAdapter, weeklyChallengeList);
                    }
                }
                userDatabaseReference.child("loginDetails").child("previousTimestamp").setValue(current_ts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        });
    }
}
