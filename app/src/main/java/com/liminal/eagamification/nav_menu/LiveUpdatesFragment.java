package com.liminal.eagamification.nav_menu;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LiveUpdatesFragment extends Fragment {

    private List<LiveUpdate> liveUpdateList = new ArrayList<>();
    private LiveUpdatesAdapter liveUpdatesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_live_updates, container, false);
        RecyclerView liveUpdatesRecyclerView = root.findViewById(R.id.recycler_view);
        liveUpdatesAdapter = new LiveUpdatesAdapter(liveUpdateList, position -> {
            Toast.makeText(getContext(),liveUpdateList.get(position).description,Toast.LENGTH_SHORT).show();
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        liveUpdatesRecyclerView.setLayoutManager(mLayoutManager);
        liveUpdatesRecyclerView.setAdapter(liveUpdatesAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(new ColorDrawable(Color.WHITE));
        liveUpdatesRecyclerView.addItemDecoration(dividerItemDecoration);

        Button button = root.findViewById(R.id.quitLiveUpdatesPopupButton);
        button.setOnClickListener(v -> getParentFragmentManager()
                .beginTransaction()
                .remove(Objects.requireNonNull(getParentFragmentManager().findFragmentById(R.id.popupFrameLayout)))
                .commit());
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Listen for values on Firebase
        FirebaseDatabase.getInstance().getReference().child("liveUpdatesTable").addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                liveUpdateList.clear();

                // Get 20 latest live updates
                final long LIVE_UPDATE_COUNT = 20;
                long startID = dataSnapshot.getChildrenCount() - LIVE_UPDATE_COUNT;

                for(long i = startID > 1 ? startID : 1 ; i < startID + LIVE_UPDATE_COUNT ; i++)
                {
                    String description = dataSnapshot.child(String.valueOf(i)).child("description").getValue().toString();
                    String activityName = dataSnapshot.child(String.valueOf(i)).child("activityID").getValue().toString();
                    LiveUpdate liveUpdate = new LiveUpdate(description, activityName);
                    liveUpdateList.add(liveUpdate);
                }

                liveUpdatesAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        });
    }
}
