package com.liminal.eagamification.rewards;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.material.tabs.TabLayout;
import com.liminal.eagamification.MainActivity;
import com.liminal.eagamification.R;

import java.util.Objects;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

public class RewardsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);
        Button homeButton = findViewById(R.id.homeButton);
        Button claim_rewards_button = findViewById(R.id.claim_rewards_button);
        Button my_rewards_button = findViewById(R.id.my_rewards_button);
        replaceFragment(new ClaimRewardsFragment());

        claim_rewards_button.setOnClickListener(view -> {
            //To indicate claim rewards button as the current active button
            claim_rewards_button.setAlpha(1);
            my_rewards_button.setAlpha(0.5f);

            replaceFragment(new ClaimRewardsFragment());
        });

        my_rewards_button.setOnClickListener(view -> {
            //To indicate my rewards button as the current active button
            my_rewards_button.setAlpha(1);
            claim_rewards_button.setAlpha(0.5f);

            replaceFragment(new MyRewardsFragment());
        });

        homeButton.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(RewardsActivity.this, MainActivity.class));
        });
    }

    //Inflate fragment specified
    private void replaceFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rewards_fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}