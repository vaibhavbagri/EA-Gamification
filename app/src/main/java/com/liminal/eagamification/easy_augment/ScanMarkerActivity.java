package com.liminal.eagamification.easy_augment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.liminal.eagamification.R;


public class ScanMarkerActivity extends AppCompatActivity {

    private FastARFragment fastARFragment;
    private Scene scene;

    static Bitmap marker;

    private SharedPreferences sharedPreferencesMarker;
    private SharedPreferences sharedPreferencesUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_marker);

        // Get user ID and asset bundle link
        sharedPreferencesMarker = getSharedPreferences("Current_Marker_Details", MODE_PRIVATE);
        sharedPreferencesUser = getSharedPreferences("User_Details", Context.MODE_PRIVATE);

        // Get marker via intent
        Intent intent = getIntent();
        marker = intent.getParcelableExtra("marker");

        // Load AR Fragment
        fastARFragment = (FastARFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // Get current AR scene
        if (fastARFragment != null) {
            scene = fastARFragment.getArSceneView().getScene();
            scene.addOnUpdateListener(this::onUpdateFrame);
        }

    }



    //This method is called at the start of each frame. @param frameTime = time since last frame.
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = fastARFragment.getArSceneView().getArFrame();
        Log.d("EAG_MARKER", String.valueOf(frame.getUpdatedTrackables(AugmentedImage.class).size()));
        if (frame == null) return;

        // Check if current frame contains the marker
        for (AugmentedImage augmentedImage : frame.getUpdatedTrackables(AugmentedImage.class)) {
            if (augmentedImage.getTrackingState() == TrackingState.PAUSED || augmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING)
            {
                // Write data to player prefs
                String playerPrefs = this.getPackageName() + ".v2.playerprefs";
                SharedPreferences sharedPreferencesUnity = this.getSharedPreferences(playerPrefs, MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPreferencesUnity.edit();
                editor.putString("user_id", sharedPreferencesUser.getString("id",""));
                editor.putString("asset_bundle_link", sharedPreferencesMarker.getString("assetBundleLink",""));
                editor.apply();

                Log.d("EAG_MARKER_SCAN", "Marker detected");
            }
        }
    }
}
