package com.liminal.eagamification.nav_menu_ui.feedback;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.ARGame;
import com.liminal.eagamification.EasyAugmentHelper;
import com.liminal.eagamification.MenuActivity;
import com.liminal.eagamification.R;

import java.util.Objects;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "Google Maps Fragment";
    private GoogleMap mMap;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    //Firebase connectivity
    private DatabaseReference databaseReference;

    //To start Scan Activity
    private EasyAugmentHelper easyAugmentHelper;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_google_maps, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("gamesTable");

        //Setup Easy Augment Helper
        easyAugmentHelper = new EasyAugmentHelper("101", Objects.requireNonNull(getActivity()), MenuActivity.class.getName());
        easyAugmentHelper.loadMarkerImages();

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        return root;
    }

    @Override
    public void onLocationChanged(Location location) {
        getDeviceLocation();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        ARGame gameObject = (ARGame) marker.getTag();
        final Dialog gameDialog = new Dialog(Objects.requireNonNull(getContext()));
        gameDialog.setContentView(R.layout.dialog_play_game);

        Button cancelButton = gameDialog.findViewById(R.id.cancelButton);
        Button playButton = gameDialog.findViewById(R.id.playButton);

        TextView name = gameDialog.findViewById(R.id.name);
        TextView description = gameDialog.findViewById(R.id.description);
        TextView rewardPoints = gameDialog.findViewById(R.id.rewardPoints);

        double user_latitude = mLastKnownLocation.getLatitude();
        double user_longitude = mLastKnownLocation.getLongitude();
        assert gameObject != null;
        double game_latitude = gameObject.latitude;
        double game_longitude = gameObject.longitude;
        double latitude_diff = user_latitude - game_latitude;
        double longitude_diff = user_longitude - game_longitude;
        if(latitude_diff<1 && latitude_diff>-1)
            if(longitude_diff<1 && longitude_diff>-1)
                playButton.setEnabled(true);

        name.setText(gameObject.name);
        description.setText(gameObject.description);
        rewardPoints.setText(String.valueOf(gameObject.rewardPoints));
        cancelButton.setOnClickListener(v -> gameDialog.dismiss());
        playButton.setOnClickListener(v -> {
            AlphaAnimation buttonClick = new AlphaAnimation(1f, 0.5f);
            buttonClick.setDuration(100);
            v.startAnimation(buttonClick);
            Toast.makeText(getContext(), "Database is setting up, please wait.", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(easyAugmentHelper::activateScanner,100);
            gameDialog.dismiss();
        });

        gameDialog.show();
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        Objects.requireNonNull(getContext()), R.raw.style));

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot game: dataSnapshot.getChildren())
                {
                    String name = game.getKey();
                    String description = Objects.requireNonNull(game.child("description").getValue()).toString();
                    double latitude = (double) game.child("latitude").getValue();
                    double longitude = (double) game.child("longitude").getValue();
                    long rewardPoints = (long) game.child("rewardPoints").getValue();
                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title(name));
                    newMarker.setTag(new ARGame(name,description,latitude,longitude,rewardPoints));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        databaseReference.addValueEventListener(eventListener);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        if (mLastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()).getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
        }
    }
}