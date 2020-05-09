package com.liminal.eagamification.nav_menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.liminal.eagamification.R;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LiveUpdatesAdapter extends RecyclerView.Adapter<LiveUpdatesAdapter.MyViewHolder> {

    private List<LiveUpdate> liveUpdateList;
    private HomeFragment.ClickListener clickListener;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_live_update, parent, false);

        return new MyViewHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        LiveUpdate liveUpdate = liveUpdateList.get(position);
        holder.challengeDescription.setText(liveUpdate.description);

    }

    @Override
    public int getItemCount() {
        return liveUpdateList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView challengeDescription;
        Button acceptButton;
        ImageView icon;

        private WeakReference<HomeFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, HomeFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            challengeDescription = itemView.findViewById(R.id.update);
            acceptButton = itemView.findViewById(R.id.liveUpdateButton);

            acceptButton.setOnClickListener(this);
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
