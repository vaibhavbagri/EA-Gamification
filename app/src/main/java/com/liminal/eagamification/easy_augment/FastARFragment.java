package com.liminal.eagamification.easy_augment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.ImageInsufficientQualityException;
import com.google.ar.sceneform.ux.ArFragment;

public class FastARFragment extends ArFragment {

    private static final String TAG = "EAG_AR_FRAGMENT";

    SharedPreferences sharedPreferencesMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Log.d(TAG,"Fast AR fragment initialized");

        sharedPreferencesMarker = getActivity().getSharedPreferences("Current_Marker_Details", Context.MODE_PRIVATE);

        // Turn off the plane discovery since we're only looking for images
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);

        // Turn off AR Core features for performance optimization
        getArSceneView().setLightEstimationEnabled(false);
        getArSceneView().getPlaneRenderer().setEnabled(false);
        getArSceneView().setLightEstimationEnabled(false);

        return view;
    }



    @Override
    protected Config getSessionConfiguration(Session session) {
        Log.d(TAG,"Configuring Session");
        Config config = super.getSessionConfiguration(session);
        config.setFocusMode(Config.FocusMode.AUTO);
        config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
        if (!setupAugmentedImageDatabase(config, session)) {
            Log.e(TAG, "Could not setup augmented image database");
        }
        return config;
    }



    // Function to setup Augmented Image Database
    private boolean setupAugmentedImageDatabase(Config config, Session session) {
        // Create Augmented image database and add the reference image to it
        AugmentedImageDatabase augmentedImageDatabase = new AugmentedImageDatabase(session);

        // Download marker from firebase
        Glide.with(getActivity())
                .asBitmap()
                .load(sharedPreferencesMarker.getString("marker_link", ""))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Log.d(TAG, "Marker loaded from URL");

                        ScanMarkerActivity.hi=true;
                        // Check for marker quality
                        try { augmentedImageDatabase.addImage("Marker_Img.jpg", resource); }
                        catch (ImageInsufficientQualityException e) { Log.d(TAG, "Image Marker quality poor"); }

                        // Add database to the configuration
                        config.setAugmentedImageDatabase(augmentedImageDatabase);

                        Log.d(TAG, "Augmented Image Database has been setup " + sharedPreferencesMarker.getString("marker_link", ""));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

        return true;
    }
}
