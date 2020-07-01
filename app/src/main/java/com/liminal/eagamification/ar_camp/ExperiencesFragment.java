package com.liminal.eagamification.ar_camp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.R;
import com.liminal.eagamification.RecyclerTouchListener;
import com.unity3d.player.UnityPlayerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class ExperiencesFragment extends Fragment {

    private List<ARExperiences> arExperiencesList = new ArrayList<>();
    private ExperiencesAdapter experiencesAdapter;
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferencesUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_experiences, container, false);
        recyclerView = root.findViewById(R.id.recycler_view);
        experiencesAdapter = new ExperiencesAdapter(arExperiencesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(experiencesAdapter);
        sharedPreferencesUser = requireActivity().getSharedPreferences("User_Details", MODE_PRIVATE);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                ARExperiences arExperiences = arExperiencesList.get(position);
                //Player prefs are used to share data to Unity locally
                String playerPrefs = requireActivity().getPackageName() + ".v2.playerprefs";
                SharedPreferences sharedPreferencesUnity = requireActivity().getSharedPreferences(playerPrefs, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferencesUnity.edit();
                editor.putString("user_id", sharedPreferencesUser.getString("id",""));
                editor.putString("assetBundleLink", arExperiences.getAssetBundleLink());
                editor.apply();
                Intent intent = new Intent(getActivity(), UnityPlayerActivity.class);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        //Read and display stored AR Experiences
        FirebaseDatabase.getInstance().getReference().child("arExperiencesTable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Read data from firebase
                arExperiencesList.clear();
                for (DataSnapshot arExperience : dataSnapshot.getChildren()) {
                    String title = Objects.requireNonNull(arExperience.child("title").getValue()).toString();
                    String description = Objects.requireNonNull(arExperience.child("description").getValue()).toString();
                    String rewards = Objects.requireNonNull(arExperience.child("rewards").getValue()).toString();
                    String assetBundleLink = Objects.requireNonNull(arExperience.child("assetBundleLink").getValue()).toString();
                    ARExperiences arExperiences = new ARExperiences(title, description, rewards, assetBundleLink);
                    arExperiencesList.add(arExperiences);
                }
                experiencesAdapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        });
    }
}
