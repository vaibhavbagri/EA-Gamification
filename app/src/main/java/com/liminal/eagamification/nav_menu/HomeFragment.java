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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.LocationBasedActivity;
import com.liminal.eagamification.MainActivity;
import com.liminal.eagamification.easy_augment.ScanMarkerActivity;
import com.liminal.eagamification.rewards.RewardsActivity;
import com.liminal.eagamification.ar_camp.CampActivity;
import com.liminal.eagamification.R;

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

    //Enable GPS alert dialog box
    private AlertDialog alert;

    BottomNavigationView bottomNavigationView;

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        locationBasedActivityTableReference = FirebaseDatabase.getInstance().getReference().child("locationBasedActivityTable");
        //Used to calculate values in live missions

        // Retrieve location from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        locationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Navigation Bar
        bottomNavigationView = root.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.live_updates:
                    item.setCheckable(true);
                    item.setIcon(R.drawable.camp_icon);
                    bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.challenges_logo);
                    getChildFragmentManager().beginTransaction().replace(R.id.popupFrameLayout, new LiveUpdatesFragment()).commit();
                    return true;
                case R.id.challenges:
                    getChildFragmentManager().beginTransaction().replace(R.id.popupFrameLayout, new ChallengesFragment()).commit();
                    item.setIcon(R.drawable.live_updates_logo);
                    bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.live_updates_logo);
                    return true;
                case R.id.ar_camp:
                    dismissPopupFragment();
                    startActivity(new Intent(getActivity(), CampActivity.class));
                    return true;
                case R.id.rewards_button:
                    dismissPopupFragment();
                    startActivity(new Intent(getActivity(), RewardsActivity.class));
                    return true;
            }
            return false;
        });

        return root;
    }

    //If any popups are on display, dismiss them before opening rewards or camp activity
    private void dismissPopupFragment(){
        if(getChildFragmentManager().findFragmentById(R.id.popupFrameLayout) != null) {
            getChildFragmentManager().beginTransaction()
                    .remove(Objects.requireNonNull(getChildFragmentManager().findFragmentById(R.id.popupFrameLayout)))
                    .commit();
            bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.live_updates_logo);
            bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.challenges_logo);
        }
    }

    // Function to show alert box to ask user to enable GPS
    private void enableGPS(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());

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

        //If popup is not dismissed, recycler view of challenges and live updates is populated with duplicate entries
        dismissPopupFragment();

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
        LocationBasedActivity activity = (LocationBasedActivity) marker.getTag();
        final Dialog beginActivityDialogBox = new Dialog(requireContext());
        beginActivityDialogBox.setContentView(R.layout.dialog_box_begin_activity);

        Button cancelButton = beginActivityDialogBox.findViewById(R.id.dialogBoxCancelButton);
        Button beginButton = beginActivityDialogBox.findViewById(R.id.dialogBoxPlayButton);

        TextView titleTextView = beginActivityDialogBox.findViewById(R.id.dialogBoxActivityTitleTextView);
        TextView descriptionTextView = beginActivityDialogBox.findViewById(R.id.dialogBoxActivityDescriptionTextView);
        TextView highscorerTextView = beginActivityDialogBox.findViewById(R.id.dialogBoxActivityHighestScorerTextView);
        TextView highscoreTextView = beginActivityDialogBox.findViewById(R.id.dialogBoxActivityHighestScoreTextView);

        ProgressBar downloadProgressBar = beginActivityDialogBox.findViewById(R.id.progressBar);

        // Update current marker details in shared preferences
        SharedPreferences sharedPreferencesMarker = requireActivity().getSharedPreferences("Current_Marker_Details", Context.MODE_PRIVATE);
        sharedPreferencesMarker.edit().putString("asset_bundle_link", activity.assetBundleLink).apply();

        double user_latitude = mLastKnownLocation.getLatitude();
        double user_longitude = mLastKnownLocation.getLongitude();
        double game_latitude = activity.latitude;
        double game_longitude = activity.longitude;
        double latitude_diff = user_latitude - game_latitude;
        double longitude_diff = user_longitude - game_longitude;

        titleTextView.setText(activity.name);
        descriptionTextView.setText(activity.description);
        highscorerTextView.setText("Highest Scorer : " + activity.highscorer);
        highscoreTextView.setText("Highest Score : " + activity.highscore);

        Intent intent;

        if(activity.category.equals("scan")) {

            // Create intent to begin scanner
            intent = new Intent(getActivity(), ScanMarkerActivity.class);

            // Download marker from firebase
            Glide.with(getActivity())
                    .asBitmap()
                    .load(activity.markerLink)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Log.d("EAG_MARKER_DOWNLOAD", "Marker loaded from URL");

                            // Enable play button when marker is done downloading
                            beginButton.setEnabled(true);
                            beginButton.setTextColor(getResources().getColor(R.color.colorPrimary, null));

                            // Disable progress bar to indicate downlaod has finished
                            downloadProgressBar.setVisibility(View.GONE);

                            // Add bitmap to scanner intent
                            intent.putExtra("marker", resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }
        else {
            //Navigate to unity player activity using this intent
            intent = new Intent(getActivity(), MainActivity.class);

            // Enable play button
            beginButton.setEnabled(true);
            beginButton.setTextColor(getResources().getColor(R.color.colorPrimary, null));

            // Disable progress bar
            downloadProgressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Clicking play opens Unity Activity", Toast.LENGTH_LONG).show();
        }

        cancelButton.setOnClickListener(v -> beginActivityDialogBox.dismiss());
        beginButton.setOnClickListener(v -> {
            // Check if user is at the marker location
            if(latitude_diff < MARKER_RADIUS && latitude_diff > -MARKER_RADIUS && longitude_diff < MARKER_RADIUS && longitude_diff > -MARKER_RADIUS) {
                // Dismiss dialog box
                beginActivityDialogBox.dismiss();
                // Start scanner
                startActivity(intent);
            }
            else
            {
                Toast.makeText(getActivity(), "Proceed to the location of AR marker to begin.", Toast.LENGTH_LONG).show();
            }
        });

        beginActivityDialogBox.show();
        return false;
    }



    // Function to setup the map and add custom markers
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Setup and Customize the map
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.ar_explore_custom_map));
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot activity : dataSnapshot.getChildren()) {
                    // Get activity details from firebase
                    String name = Objects.requireNonNull(activity.child("name").getValue()).toString();
                    String description = Objects.requireNonNull(activity.child("description").getValue()).toString();
                    String category = Objects.requireNonNull(activity.child("category").getValue()).toString();
                    String assetBundleLink = "";
                    String markerLink = "";
                    if(category.equals("scan")) {
                        assetBundleLink = Objects.requireNonNull(activity.child("assetBundleLink").getValue()).toString();
                        markerLink = Objects.requireNonNull(activity.child("markerLink").getValue()).toString();
                    }

                    // Get activity locations from firebase
                    for (DataSnapshot locations : activity.child("locations").getChildren()) {
                        double latitude = (double) locations.child("latitude").getValue();
                        double longitude = (double) locations.child("longitude").getValue();
                        String highscorer = (String) locations.child("highscorer") .getValue();
                        long highscore = (long) locations.child("highscore").getValue();

                        Marker newMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name));
                        newMarker.setTitle(null);
                        newMarker.setTag(new LocationBasedActivity(category, name, description, latitude, longitude, assetBundleLink, markerLink, highscorer, highscore));

                        switch (category) {
                            case "event":
                                newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.event_marker), 100, 100, false)));
                                break;
                            case "game":
                                newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.game_marker), 100, 100, false)));
                                break;
                            case "scan":
                                newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.scan_marker), 100, 100, false)));
                                break;
                        }
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
            locationResult.addOnCompleteListener(requireActivity(), task -> {
                if (task.isSuccessful())
                {
                    // Set the map's camera position to the current location of the device.
                    Log.d(TAG,"User location obtained");
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



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}