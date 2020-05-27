package com.liminal.eagamification.nav_menu;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import androidx.recyclerview.widget.RecyclerView;

public class LiveUpdatesAdapter extends RecyclerView.Adapter<LiveUpdatesAdapter.MyViewHolder> {

    private List<LiveUpdate> liveUpdateList;
    private HomeFragment.ClickListener clickListener;
    private DatabaseReference locationBasedActivityTableReference;
    private Context context;



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_live_update, parent, false);

        context = parent.getContext();
        locationBasedActivityTableReference = FirebaseDatabase.getInstance().getReference().child("locationBasedActivityTable");

        return new MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        LiveUpdate liveUpdate = liveUpdateList.get(position);

        // set challenge description
        holder.challengeDescription.setText(liveUpdate.description);

        ValueEventListener iconLinkListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String iconLink =  dataSnapshot.child(liveUpdate.activityName).child("iconLink").getValue().toString();
                String assetBundleLink = dataSnapshot.child(liveUpdate.activityName).child("assetBundleLink").getValue().toString();

                Log.d("EAG_FIREBASE_DB", "Activity : " + liveUpdate.activityName + "\nIcon link : " + iconLink + "\nAsset Bundle link : " + assetBundleLink);

                // set activity icon
                Glide.with(context).load(Uri.parse(iconLink)).into(holder.activityIcon);

                // send asset bundle link to button
                holder.acceptButton.setOnClickListener(v -> Toast.makeText(context, "Asset Bundle Link : " + assetBundleLink, Toast.LENGTH_LONG).show());
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
        return liveUpdateList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView challengeDescription;
        Button acceptButton;
        ImageView activityIcon;

        private WeakReference<HomeFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, HomeFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            challengeDescription = itemView.findViewById(R.id.TextView);
            acceptButton = itemView.findViewById(R.id.ClaimButton);
            activityIcon = itemView.findViewById(R.id.Icon);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }
    LiveUpdatesAdapter(List<LiveUpdate> rewardDetailsList, HomeFragment.ClickListener clickListener){
        this.liveUpdateList = rewardDetailsList;
        this.clickListener = clickListener;
    }

}
