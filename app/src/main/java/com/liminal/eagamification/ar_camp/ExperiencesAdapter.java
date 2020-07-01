package com.liminal.eagamification.ar_camp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liminal.eagamification.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
        //Even position yellow, odd position blue
        if(position % 2 == 0){
            holder.yellow_title.setText(arExperiences.getTitle());
            holder.yellow_description.setText(arExperiences.getDescription());
            holder.yellow_card.setVisibility(View.VISIBLE);
            holder.blue_card.setVisibility(View.GONE);
        }
        else {
            holder.blue_title.setText(arExperiences.getTitle());
            holder.blue_description.setText(arExperiences.getDescription());
            holder.blue_card.setVisibility(View.VISIBLE);
            holder.yellow_card.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return arExperiencesList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView yellow_title, yellow_description;
        TextView blue_title, blue_description;
        CardView yellow_card, blue_card;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            yellow_title = itemView.findViewById(R.id.yellow_title);
            yellow_description = itemView.findViewById(R.id.yellow_description);
            blue_title = itemView.findViewById(R.id.blue_title);
            blue_description = itemView.findViewById(R.id.blue_description);
            yellow_card = itemView.findViewById(R.id.yellow_card);
            blue_card = itemView.findViewById(R.id.blue_card);
        }
    }

    ExperiencesAdapter(List<ARExperiences> arExperiencesList){
        this.arExperiencesList = arExperiencesList;
    }
}
