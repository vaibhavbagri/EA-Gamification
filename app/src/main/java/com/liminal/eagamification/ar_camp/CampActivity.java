package com.liminal.eagamification.ar_camp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.liminal.eagamification.MainActivity;
import com.liminal.eagamification.R;

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
            //To indicate experiences button as the current active button
            experiences_button.setAlpha(1);
            collection_button.setAlpha(0.5f);

            replaceFragment(new ExperiencesFragment());
        });

        collection_button.setOnClickListener(view -> {
            //To indicate collection button as the current active button
            collection_button.setAlpha(1);
            experiences_button.setAlpha(0.5f);

            replaceFragment(new CollectionFragment());
        });

        homeButton.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(CampActivity.this, MainActivity.class));
        });
    }

    //Navigate between Experiences and Collection fragment
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
