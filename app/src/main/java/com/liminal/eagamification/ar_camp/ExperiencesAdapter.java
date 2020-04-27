package com.liminal.eagamification.ar_camp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liminal.eagamification.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ExperiencesAdapter extends RecyclerView.Adapter<ExperiencesAdapter.MyViewHolder> {

    private List<ARExperiences> arExperiencesList;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_ar_experience, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ARExperiences arExperiences = arExperiencesList.get(position);
        holder.title.setText(arExperiences.title);
        holder.description.setText(arExperiences.description);
        holder.rewards.setText(arExperiences.rewards);
    }

    @Override
    public int getItemCount() {
        return arExperiencesList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, description, rewards;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            rewards = itemView.findViewById(R.id.rewards);
        }
    }

    ExperiencesAdapter(List<ARExperiences> arExperiencesList){
        this.arExperiencesList = arExperiencesList;
    }
}
