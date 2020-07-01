package com.liminal.eagamification.rewards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liminal.eagamification.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyRewardsAdapter extends RecyclerView.Adapter<MyRewardsAdapter.MyViewHolder> {

    private List<RewardDetails> rewardDetailsList;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_my_reward, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        RewardDetails rewardDetails = rewardDetailsList.get(position);
        holder.description.setText(rewardDetails.getDescription());
    }

    @Override
    public int getItemCount() {
        return rewardDetailsList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView description;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description);
        }
    }

    MyRewardsAdapter(List<RewardDetails> rewardDetailsList){
        this.rewardDetailsList = rewardDetailsList;
    }
}
