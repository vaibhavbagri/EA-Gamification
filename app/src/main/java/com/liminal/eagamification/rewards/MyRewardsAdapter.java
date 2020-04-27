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
        holder.title.setText(rewardDetails.title);
        holder.description.setText(rewardDetails.description);
        holder.cost.setText(String.valueOf(rewardDetails.cost));
        holder.quantity.setText(String.valueOf(rewardDetails.quantity));
    }

    @Override
    public int getItemCount() {
        return rewardDetailsList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, description, cost, quantity;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            cost = itemView.findViewById(R.id.cost);
            quantity = itemView.findViewById(R.id.quantity);
        }
    }

    MyRewardsAdapter(List<RewardDetails> rewardDetailsList){
        this.rewardDetailsList = rewardDetailsList;
    }
}
