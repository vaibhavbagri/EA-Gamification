package com.liminal.eagamification.nav_menu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.LocationBasedGame;
import com.liminal.eagamification.easy_augment.ScanMarkerActivity;
import com.liminal.eagamification.rewards.RewardsActivity;
import com.liminal.eagamification.ar_camp.CampActivity;
import com.liminal.eagamification.easy_augment.EasyAugmentHelper;
import com.liminal.eagamification.MainActivity;
import com.liminal.eagamification.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.LOCATION_SERVICE;

public class HomeFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener
{
    private static final String TAG = "EAG_Google_Maps";
    private GoogleMap mMap;

    // Default location (Mumbai)
    private final LatLng mDefaultLocation = new LatLng(19.0760, 72.8777);
    // Default zoom
    private static final int DEFAULT_ZOOM = 17;
    // Marker radius
    private final int MARKER_RADIUS = 1;


    // Access to the system location services.
    private LocationManager locationManager;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private Location mLastSavedLocation;
    private boolean isMapOpened = false;

    // Keys for storing activity state.
    private static final String KEY_LOCATION = "location";

    // Firebase connectivity
    private DatabaseReference locationBasedActivityTableReference;
    private DatabaseReference userDatabaseReference;

    // Initialize Easy Augment
    private EasyAugmentHelper easyAugmentHelper;

    //Enable GPS alert dialog box
    private AlertDialog alert;

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        Log.d(TAG,"Generating Map");

        SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences("User_Details", Context.MODE_PRIVATE);

        locationBasedActivityTableReference = FirebaseDatabase.getInstance().getReference().child("locationBasedActivityTable");
        //Used to calculate values in live missions
        userDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("userProfileTable")
                .child(sharedPreferences.getString("id",""))
                .child("statistics");

//        easyAugmentHelper = new EasyAugmentHelper("101", Objects.requireNonNull(getActivity()), MainActivity.class.getName());
//        easyAugmentHelper.loadMarkerImages();

        // Retrieve location from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        locationManager = (LocationManager) Objects.requireNonNull(getContext()).getSystemService(LOCATION_SERVICE);
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
//            enableGPS();

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Implement button to allow user to claim rewards
        FloatingActionButton claimRewardsButton = root.findViewById(R.id.claimRewardsButton);
        claimRewardsButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RewardsActivity.class);
            startActivity(intent);
        });

        // Implement button to show missions
        FloatingActionButton missionsButton = root.findViewById(R.id.missionsButton);
        missionsButton.setOnClickListener(view -> manageMissionsPopup(root, inflater));

        // Implement button to show live updates
        FloatingActionButton liveUpdatesButton = root.findViewById(R.id.socialButton);
        liveUpdatesButton.setOnClickListener(view -> manageLiveUpdatesPopup(root, inflater));

        // Implement button to show AR camp
        FloatingActionButton campButton = root.findViewById(R.id.campButton);
        campButton.setOnClickListener(v -> {
            Intent campIntent = new Intent(getActivity(), CampActivity.class);
            startActivity(campIntent);
        });

        return root;
    }

    // Function to manage missions popup window
    private void manageMissionsPopup(View view, LayoutInflater inflater)
    {
        // Inflate the popup window layout
        View popupMissionsView = inflater.inflate(R.layout.popup_missions, null);
        //Setup recycler view within popup window
        List<Challenge> challengeList = new ArrayList<>();
        RecyclerView challengesRecyclerView = popupMissionsView.findViewById(R.id.recycler_view);
        ChallengesAdapter challengesAdapter = new ChallengesAdapter(challengeList, position -> {
            Toast.makeText(getContext(),challengeList.get(position).description,Toast.LENGTH_SHORT).show();
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        challengesRecyclerView.setLayoutManager(mLayoutManager);
        challengesRecyclerView.setAdapter(challengesAdapter);
        // Setup popup window
        final PopupWindow popupWindow = new PopupWindow(popupMissionsView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        // Allow touch input outside the popup window
        popupWindow.setOutsideTouchable(true);

        if(popupWindow.isShowing())
            popupWindow.dismiss();
        else
        {
            // Show popup view at the center
            popupWindow.showAtLocation(view, Gravity.CENTER, 0,0);

            // Button to dismiss popup
            Button button = popupMissionsView.findViewById(R.id.quitMissionsPopupButton);
            button.setOnClickListener(v -> popupWindow.dismiss());
        }

        //Listen for values on Firebase
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                challengeList.clear();
                for (DataSnapshot challengeID : dataSnapshot.getChildren()) {
                    String description = challengeID.child("description").getValue().toString();
                    String rewardPoints = challengeID.child("rewardPoints").getValue().toString();
                    String activityName = challengeID.child("activityID").getValue().toString();
                    String stat = challengeID.child("stat").getValue().toString();
                    long target = (long) challengeID.child("target").getValue();
                    long challengePosition = (long) challengeID.child("challengePosition").getValue();
                    //Check the current stat value and stored stat value at the start of the week to calculate progress
                    userDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long stat_value = (long) dataSnapshot.child("activityBased").child(activityName).child(stat).getValue();
                            long stored_value = (long) dataSnapshot.child("challenges").child(String.valueOf(challengePosition)).getValue();
                            long progress = stat_value - stored_value ;
                            Challenge challenge = new Challenge(progress,description,rewardPoints,activityName,target,stat);
                            challengeList.add(challenge);
                            challengesAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };

        FirebaseDatabase.getInstance().getReference().child("challengesTable").addValueEventListener(eventListener);
    }



    // Function to manage missions popup window
    private void manageLiveUpdatesPopup(View view, LayoutInflater inflater)
    {
        // Inflate the popup window layout
        View popupMissionsView = inflater.inflate(R.layout.popup_live_updates, null);

        //Setup recycler view within popup window
        List<LiveUpdate> liveUpdateList = new ArrayList<>();
        RecyclerView liveUpdatesRecyclerView = popupMissionsView.findViewById(R.id.recycler_view);
        LiveUpdatesAdapter liveUpdatesAdapter = new LiveUpdatesAdapter(liveUpdateList, position -> {
            Toast.makeText(getContext(),liveUpdateList.get(position).description,Toast.LENGTH_SHORT).show();
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        liveUpdatesRecyclerView.setLayoutManager(mLayoutManager);
        liveUpdatesRecyclerView.setAdapter(liveUpdatesAdapter);

        // Setup popup window
        final PopupWindow popupWindow = new PopupWindow(popupMissionsView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        // Show popup view at the center
        popupWindow.showAtLocation(view, Gravity.CENTER, 0,0);

        // Button to dismiss popup
        Button button = popupMissionsView.findViewById(R.id.quitLiveUpdatesPopupButton);
        button.setOnClickListener(v -> popupWindow.dismiss());

        //Listen for values on Firebase
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                liveUpdateList.clear();
                for (DataSnapshot updateID : dataSnapshot.getChildren()) {
                    String description = updateID.child("description").getValue().toString();
                    String activityName = updateID.child("activityID").getValue().toString();
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
        };

        FirebaseDatabase.getInstance().getReference().child("liveUpdatesTable").addValueEventListener(eventListener);
    }



    // Function to show alert box to ask user to enable GPS
    private void enableGPS(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));

        alertDialogBuilder.setMessage("GPS is disabled in your device. Enable GPS to continue.")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        (dialog, id) -> {
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        });
        alert = alertDialogBuilder.create();
        alert.show();
    }



    @Override
    public void onResume()
    {
        super.onResume();
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Log.d(TAG,"GPS enabled by user");
            getDeviceLocation();
        }
        else
            enableGPS();
    }



    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"Location changed");
        mLastKnownLocation = location;
        if(!isMapOpened) {
            isMapOpened = true;
            // Move camera to new user location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
        }
    }



    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }



    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(getContext(),"GPS Enabled", Toast.LENGTH_SHORT).show();
        alert.dismiss();
        if(mLastSavedLocation == null)
            getDeviceLocation();
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom
                    (new LatLng(mLastSavedLocation.getLatitude(), mLastSavedLocation.getLongitude()), DEFAULT_ZOOM));

    }



    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(getContext(),"GPS Disabled", Toast.LENGTH_SHORT).show();
        mLastSavedLocation = mLastKnownLocation;
        if(alert==null || !alert.isShowing())
            enableGPS();
    }



    @Override
    public boolean onMarkerClick(Marker marker) {
        LocationBasedGame game = (LocationBasedGame) marker.getTag();
        final Dialog playGameDialogBox = new Dialog(Objects.requireNonNull(getContext()));
        playGameDialogBox.setContentView(R.layout.dialog_box_play_game);

        Button cancelButton = playGameDialogBox.findViewById(R.id.dialogBoxCancelButton);
        Button playButton = playGameDialogBox.findViewById(R.id.dialogBoxPlayButton);

        TextView titleTextView = playGameDialogBox.findViewById(R.id.dialogBoxGameTitleTextView);
        TextView descriptionTextView = playGameDialogBox.findViewById(R.id.dialogBoxGameDescriptionTextView);

        ProgressBar downloadProgressBar = playGameDialogBox.findViewById(R.id.progressBar);

        // Update current marker details in shared preferences
        SharedPreferences sharedPreferencesMarker = getActivity().getSharedPreferences("Current_Marker_Details", Context.MODE_PRIVATE);
        sharedPreferencesMarker.edit().putString("asset_bundle_link", game.assetBundleLink).apply();

        // Create intent to begin scanner
        Intent scannerIntent = new Intent(getActivity(), ScanMarkerActivity.class);

        double user_latitude = mLastKnownLocation.getLatitude();
        double user_longitude = mLastKnownLocation.getLongitude();
        double game_latitude = game.latitude;
        double game_longitude = game.longitude;
        double latitude_diff = user_latitude - game_latitude;
        double longitude_diff = user_longitude - game_longitude;

        // Download marker from firebase
        Glide.with(getActivity())
                .asBitmap()
                .load(game.markerLink)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Log.d("EAG_MARKER_DOWNLOAD", "Marker loaded from URL");

                        // Enable play button when marker is done downloading
                        playButton.setEnabled(true);
                        playButton.setTextColor(getResources().getColor(R.color.colorPrimary, null));

                        // Disable progress bar to indicate downlaod has finished
                        downloadProgressBar.setVisibility(View.GONE);

                        // Add bitmap to scanner intent
                        scannerIntent.putExtra("marker", resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

        titleTextView.setText(game.name);
        descriptionTextView.setText(game.description);

        cancelButton.setOnClickListener(v -> playGameDialogBox.dismiss());
        playButton.setOnClickListener(v -> {
            // Check if user is at the marker location
            if(latitude_diff < MARKER_RADIUS && latitude_diff > -MARKER_RADIUS && longitude_diff < MARKER_RADIUS && longitude_diff > -MARKER_RADIUS) {
                // Dismiss dialog box
                playGameDialogBox.dismiss();
                // Start scanner
                startActivity(scannerIntent);
            }
            else
            {
                Toast.makeText(getActivity(), "Proceed to the location of AR marker to begin.", Toast.LENGTH_LONG).show();
            }
        });

        playGameDialogBox.show();
        return false;
    }



    // Function to setup the map and add custom markers
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Setup and Customize the map
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Objects.requireNonNull(getContext()), R.raw.ar_explore_custom_map));
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot game: dataSnapshot.getChildren())
                {
                    // Get game details from firebase
                    String name = game.child("name").getValue().toString();
                    String description = game.child("description").getValue().toString();
                    String assetBundleLink = game.child("assetBundleLink").getValue().toString();
                    String markerLink = game.child("markerLink").getValue().toString();
                    String category = game.child("category").getValue().toString();
//                    double latitude = (double) game.child("latitude").getValue();
//                    double longitude = (double) game.child("longitude").getValue();
                    double latitude = 75.1;
                    double longitude = 18.1;

                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title(name));
                    newMarker.setTitle(null);
                    newMarker.setTag(new LocationBasedGame(name, description, latitude, longitude, assetBundleLink, markerLink));

                    switch (category)
                    {
                        case "event" :
                            newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.event_marker),100, 100,false)));
                            break;
                        case "game" :
                            newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.game_marker),100, 100,false)));
                            break;
                        case "scan" :
                            newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.scan_marker),100, 100,false)));
                            break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        locationBasedActivityTableReference.addValueEventListener(eventListener);
    }



    // Gets the current location of the device, and positions the map's camera.
    private void getDeviceLocation() {
        Log.d(TAG,"Device location requested");
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                if (task.isSuccessful())
                {
                    // Set the map's camera position to the current location of the device.
                    Log.d(TAG,"Location obtained");
                    mLastKnownLocation = task.getResult();

                    if (mLastKnownLocation != null)
                    {
                        Log.d(TAG, "User's location is : " + mLastKnownLocation.getLatitude() + " " + mLastKnownLocation.getLongitude() );
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    }
                    else {
                        Log.d(TAG, "Last known location is null");
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 10));
                        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                            getDeviceLocation();
                    }
                }
                else
                    {
                    Log.d(TAG, "Unable to retrieve location. Using default location instead.");
                    Log.e(TAG, "Exception: %s", task.getException());

                    // Set the map's camera position to the default location.
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
            });
        }
        catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // Updates the map's UI settings based on whether the user has granted location permission.
    private void updateLocationUI() {
        if (mMap != null) {
            try
            {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            catch (SecurityException e)
            {
                Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    //Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}