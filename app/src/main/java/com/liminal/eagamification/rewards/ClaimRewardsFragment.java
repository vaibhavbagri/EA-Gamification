package com.liminal.eagamification.rewards;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liminal.eagamification.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ClaimRewardsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_claim_rewards, container, false);
        //Inflate reward categories fragment to indicate the categories of items available
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container_rewards, new RewardsCategoriesFragment()).commit();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Code to open rewards category from rewards list on back button press
        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        getView().setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK && getChildFragmentManager().getBackStackEntryCount() != 0){
                getChildFragmentManager().popBackStack();
                return true;
            }
            return false;
        });
    }
}
