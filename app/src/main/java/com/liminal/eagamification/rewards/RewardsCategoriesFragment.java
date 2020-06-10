package com.liminal.eagamification.rewards;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liminal.eagamification.R;

import java.util.ArrayList;
import java.util.List;

public class RewardsCategoriesFragment extends Fragment implements View.OnClickListener{

    ViewPager viewPager;
    RewardsCategoryAdapter rewardsCategoryAdapter;
    List<RewardCategory> rewardCategoryList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_rewards_categories, container, false);
        root.findViewById(R.id.foodButton).setOnClickListener(this);
        root.findViewById(R.id.fitnessButton).setOnClickListener(this);
        root.findViewById(R.id.entertainmentButton).setOnClickListener(this);
        root.findViewById(R.id.travelButton).setOnClickListener(this);

//        rewardCategoryList = new ArrayList<>();
//        rewardCategoryList.add(new RewardCategory(R.drawable.holidays_background, "Holidays", "Holidays are fun"));
//        rewardCategoryList.add(new RewardCategory(R.drawable.live_updates_logo, "Holidays", "Holidays are fun"));
//        rewardCategoryList.add(new RewardCategory(R.drawable.rewards_logo, "Holidays", "Holidays are fun"));
//        rewardCategoryList.add(new RewardCategory(R.drawable.challenges_logo, "Holidays", "Holidays are fun"));
//
//        rewardsCategoryAdapter = new RewardsCategoryAdapter(rewardCategoryList, inflater, getContext());
//        viewPager = root.findViewById(R.id.categories_view_pager);
//        viewPager.setAdapter(rewardsCategoryAdapter);
//        viewPager.setPadding(130, 0, 130, 0);

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
                .replace(R.id.fragment_container_rewards, new RewardsListFragment(category))
                .addToBackStack("RewardCategories")
                .commit();
    }
}
