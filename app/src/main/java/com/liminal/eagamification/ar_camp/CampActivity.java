package com.liminal.eagamification.ar_camp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.liminal.eagamification.MainActivity;
import com.liminal.eagamification.R;
import com.liminal.eagamification.rewards.ClaimRewardsFragment;
import com.liminal.eagamification.rewards.MyRewardsFragment;
import com.liminal.eagamification.rewards.RewardsActivity;

import java.util.Objects;

public class CampActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camp);
        Button homeButton = findViewById(R.id.homeButton);
        Button experiences_button = findViewById(R.id.experiences_button);
        Button collection_button = findViewById(R.id.collection_button);
        replaceFragment(new ExperiencesFragment());

        experiences_button.setOnClickListener(view -> {
            //To indicate claim rewards button as the current active button
            experiences_button.setAlpha(1);
            collection_button.setAlpha(0.5f);

            replaceFragment(new ExperiencesFragment());
        });

        collection_button.setOnClickListener(view -> {
            //To indicate my rewards button as the current active button
            collection_button.setAlpha(1);
            experiences_button.setAlpha(0.5f);

            replaceFragment(new CollectionFragment());
        });

        homeButton.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(CampActivity.this, MainActivity.class));
        });
    }

    private void replaceFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.camp_fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
