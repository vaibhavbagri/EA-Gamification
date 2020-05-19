package com.liminal.eagamification.nav_menu;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.R;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.MyViewHolder> {

    private List<Challenge> challengeList;
    private HomeFragment.ClickListener clickListener;
    private DatabaseReference locationBasedActivityTableReference;
    private Context context;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_challenge, parent, false);
        context = parent.getContext();
        locationBasedActivityTableReference = FirebaseDatabase.getInstance().getReference().child("locationBasedActivityTable");
        return new MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Challenge challenge = challengeList.get(position);
        holder.description.setText(challenge.description);
        holder.rewardPoints.setText(String.valueOf(challenge.rewardPoints));

        if(challenge.isClaimed) {
            holder.constraintLayout.setBackgroundColor(Color.LTGRAY);
            holder.claimButton.setEnabled(false);
            holder.constraintLayout.setAlpha(0.8f);
        }

        if(challenge.progress < challenge.target) {
            holder.progress.setText(challenge.progress + "/" + challenge.target);
            holder.progressBar.setProgress((int) ((challenge.progress * 100) / challenge.target));
        }
        else {
            holder.progress.setText(challenge.target + "/" + challenge.target);
            holder.progressBar.setProgress(100);
        }

        ValueEventListener iconLinkListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String iconLink =  dataSnapshot.child(challenge.activityName).child("iconLink").getValue().toString();
                Log.d("EAG_FIREBASE_DB", "Activity : " + challenge.activityName + "\nIcon link : " + iconLink );
                // set activity icon
                Glide.with(context).load(Uri.parse(iconLink)).into(holder.activityIcon);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        locationBasedActivityTableReference.addListenerForSingleValueEvent(iconLinkListener);

    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView description;
        TextView rewardPoints;
        TextView progress;
        ProgressBar progressBar;
        ImageView activityIcon;
        Button claimButton;
        ConstraintLayout constraintLayout;
        private WeakReference<HomeFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, HomeFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            description = itemView.findViewById(R.id.description);
            rewardPoints = itemView.findViewById(R.id.rewardPoints);
            progress = itemView.findViewById(R.id.progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
            activityIcon = itemView.findViewById(R.id.activityIcon);
            claimButton = itemView.findViewById(R.id.redeemButton);
            constraintLayout = itemView.findViewById(R.id.challengeConstraintLayout);

            claimButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    ChallengesAdapter(List<Challenge> challengeList, HomeFragment.ClickListener clickListener){
        this.challengeList = challengeList;
        this.clickListener = clickListener;
    }
}

