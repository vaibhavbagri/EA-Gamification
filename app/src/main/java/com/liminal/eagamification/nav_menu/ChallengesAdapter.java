package com.liminal.eagamification.nav_menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liminal.eagamification.R;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.MyViewHolder> {

    private List<Challenge> challengeList;
    private HomeFragment.ClickListener clickListener;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_challenge, parent, false);

        return new MyViewHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Challenge challenge = challengeList.get(position);
        holder.description.setText(challenge.description);
        holder.rewardPoints.setText(challenge.rewardPoints);
        if(challenge.progress < challenge.target) {
            holder.progress.setText(challenge.progress + "/" + challenge.target);
            holder.progressBar.setProgress((int) ((challenge.progress * 100) / challenge.target));
        }
        else{
            holder.progress.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.button.setVisibility(View.VISIBLE);
        }
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
        ImageView imageView;
        Button button;
        private WeakReference<HomeFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, HomeFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            description = itemView.findViewById(R.id.description);
            rewardPoints = itemView.findViewById(R.id.rewardPoints);
            progress = itemView.findViewById(R.id.progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
            imageView = itemView.findViewById(R.id.imageView);
            button = itemView.findViewById(R.id.redeemButton);

            button.setOnClickListener(this);
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

