package com.liminal.eagamification.rewards;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liminal.eagamification.R;

public class RewardsCategoriesFragment extends Fragment implements View.OnClickListener{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_rewards_categories, container, false);
        root.findViewById(R.id.foodButton).setOnClickListener(this);
        root.findViewById(R.id.fitnessButton).setOnClickListener(this);
        root.findViewById(R.id.entertainmentButton).setOnClickListener(this);
        root.findViewById(R.id.travelButton).setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.foodButton:
                replaceFragment("Food");
                break;

            case R.id.fitnessButton:
                replaceFragment("Fitness");
                break;

            case R.id.entertainmentButton:
                replaceFragment("Entertainment");
                break;

            case R.id.travelButton:
                replaceFragment("Travel");
                break;
        }
    }

    private void replaceFragment(String category) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_rewards,new RewardsListFragment(category))
                .addToBackStack("RewardCategories")
                .commit();
    }
}
