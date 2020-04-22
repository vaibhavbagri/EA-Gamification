package com.liminal.eagamification.nav_menu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import com.liminal.eagamification.RewardsActivity;
import com.liminal.eagamification.easy_augment.EasyAugmentHelper;
import com.liminal.eagamification.MainActivity;
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

    // Access to the system location services.
    private LocationManager locationManager;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private boolean isMapOpened = false;

    // Keys for storing activity state.
    private static final String KEY_LOCATION = "location";

    // Firebase connectivity
    private DatabaseReference locationBasedGamesTableReference;

    // Initialize Easy Augment
    private EasyAugmentHelper easyAugmentHelper;

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        Log.d(TAG,"Generating Map");

        locationBasedGamesTableReference = FirebaseDatabase.getInstance().getReference().child("locationBasedGamesTable");

        easyAugmentHelper = new EasyAugmentHelper("101", Objects.requireNonNull(getActivity()), MainActivity.class.getName());
        easyAugmentHelper.loadMarkerImages();

        // Retrieve location from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        locationManager = (LocationManager) Objects.requireNonNull(getContext()).getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            enableGPS();

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

        return root;
    }



    // Function to show alert box to ask user to enable GPS
    private void enableGPS(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        (dialog, id) -> {
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        });
        AlertDialog alert = alertDialogBuilder.create();
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
    }



    @Override
    public void onLocationChanged(Location location) {
        Log.d("EAG_MAPS","Location changed");
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLastKnownLocation = location;
            if(!isMapOpened) {
                isMapOpened = true;
                // Move camera to new user location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
            }
        }
        else
            enableGPS();
    }



    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }



    @Override
    public void onProviderEnabled(String s) { }



    @Override
    public void onProviderDisabled(String s) { }



    @Override
    public boolean onMarkerClick(Marker marker) {
        LocationBasedGame gameObject = (LocationBasedGame) marker.getTag();
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
        if(latitude_diff < 1 && latitude_diff > -1 && longitude_diff < 1 && longitude_diff > -1)
            playButton.setEnabled(true);

        name.setText(gameObject.name);
        description.setText(gameObject.description);
        rewardPoints.setText(String.valueOf(gameObject.assetBundleLink));
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



    // Function to setup the map and add custom markers
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Setup and Customize the map
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Objects.requireNonNull(getContext()), R.raw.style));
        mMap.getUiSettings().setMapToolbarEnabled(false);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot game: dataSnapshot.getChildren())
                {
                    // Get game details from firebase
                    String name = game.child("name").getValue().toString();
                    String description = game.child("description").getValue().toString();
                    String assetBundleLink = game.child("assetBundleLink").getValue().toString();
                    double latitude = (double) game.child("latitude").getValue();
                    double longitude = (double) game.child("longitude").getValue();

                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title(name));
                    newMarker.setTitle(null);
                    newMarker.setTag(new LocationBasedGame(name,description,latitude,longitude,assetBundleLink));
                    newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.event_marker),100, 100,false)));
//                    newMarker.setFlat(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        locationBasedGamesTableReference.addValueEventListener(eventListener);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

    }



    // Gets the current location of the device, and positions the map's camera.
    private void getDeviceLocation() {
        Log.d(TAG,"Device location requested");
        try {
            // Location permission granted
            if (mLocationPermissionGranted)
            {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                    if (task.isSuccessful())
                    {
                        // Set the map's camera position to the current location of the device.
                        Log.d(TAG,"Location obtained");
                        mLastKnownLocation = task.getResult();

                        if (mLastKnownLocation != null)
                        {
                            Log.d(TAG, "User's location is : " + String.valueOf(mLastKnownLocation.getLatitude()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
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
        }
        catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }



    // Prompts the user for permission to use the device location.
    private void getLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()).getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mLocationPermissionGranted = true;
        else
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }



    // Handles the result of the request for location permissions.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }



    // Updates the map's UI settings based on whether the user has granted location permission.
    private void updateLocationUI() {
        if (mMap != null) {
            try
            {
                if (mLocationPermissionGranted)
                {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
                else
                {
                    mMap.setMyLocationEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mLastKnownLocation = null;
                    getLocationPermission();
                }
            }
            catch (SecurityException e)
            {
                Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
            }
        }
    }



}

//public class HomeFragment extends Fragment {
//
//    //Location services
//    private FusedLocationProviderClient client;
//
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_temp, container, false);
//
//        ImageView loaderView = root.findViewById(R.id.menuAdvertisementImageView);
//        Glide.with(this).asGif().load(R.drawable.scenery).into(loaderView);
//
//        EasyAugmentHelper easyAugmentHelper = new EasyAugmentHelper("101", Objects.requireNonNull(getActivity()), MainActivity.class.getName());
//        easyAugmentHelper.loadMarkerImages();
//
//        Button haikuButton = root.findViewById(R.id.gameStartButton1);
//        Button driveButton = root.findViewById(R.id.gameStartButton2);
//        Button chartButton = root.findViewById(R.id.gameStartButton3);
//        Button gmapsButton = root.findViewById(R.id.gameStartButton4);
//
//        client = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));
//
////        gmapsButton.setOnClickListener(v -> {
////            AlphaAnimation buttonClick = new AlphaAnimation(1f, 0.5f);
////            buttonClick.setDuration(100);
////            v.startAnimation(buttonClick);
////            client.getLastLocation().addOnSuccessListener(getActivity(), location -> {
////                if(location!=null)
////                    Log.d("EAG_LOCATION",location.toString());
////            });
////            new Handler().postDelayed(() -> {
////                Intent unityIntent = new Intent(getActivity(), GoogleMapsActivity.class);
////                startActivity(unityIntent);
////            },100);
////        });
//
//        chartButton.setOnClickListener(v -> {
//            AlphaAnimation buttonClick = new AlphaAnimation(1f, 0.5f);
//            buttonClick.setDuration(100);
//            v.startAnimation(buttonClick);
//            client.getLastLocation().addOnSuccessListener(getActivity(), location -> {
//                if(location!=null)
//                    Log.d("EAG_LOCATION",location.toString());
//            });
//            new Handler().postDelayed(() -> {
//                Intent unityIntent = new Intent(getActivity(), ChartsSelectActivity.class);
//                startActivity(unityIntent);
//            },100);
//        });
//
//        haikuButton.setOnClickListener(v -> {
//            AlphaAnimation buttonClick = new AlphaAnimation(1f, 0.5f);
//            buttonClick.setDuration(100);
//            v.startAnimation(buttonClick);
//            client.getLastLocation().addOnSuccessListener(getActivity(), location -> {
//                if(location!=null)
//                    Log.d("EAG_LOCATION",location.toString());
//            });
//            Toast.makeText(getContext(), "Database is setting up, please wait.", Toast.LENGTH_LONG).show();
//            new Handler().postDelayed(easyAugmentHelper::activateScanner,100);
//        });
//
////        driveButton.setOnClickListener(v -> {
////            AlphaAnimation buttonClick = new AlphaAnimation(1f, 0.5f);
////            buttonClick.setDuration(100);
////            v.startAnimation(buttonClick);
////            new Handler().postDelayed(() -> {
////                Intent unityIntent = new Intent(getActivity(), UnityPlayerActivity.class);
////                startActivity(unityIntent);
////            },100);
////        });
//
//        return root;
//    }
//
////    void chooseNewImage() {
////        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
////        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
////        intent.setType("image/*");
////        startActivityForResult(android.content.Intent.createChooser(intent, "Select target augmented image"),1);
////    }
////
////
////    @Override
////    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////        try {
////            if (resultCode == android.app.Activity.RESULT_OK) {
////                if (requestCode == 1)
////                {
////                    Uri imagePath = data.getData();
//////                    File file = new File(imagePath.getPath());
////                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("stor.jpg");
////
////                    storageReference.putFile(imagePath)
////                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
////                                @Override
////                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
////                                    // Get a URL to the uploaded content
////
//////                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
////                                }
////                            })
////                            .addOnFailureListener(new OnFailureListener() {
////                                @Override
////                                public void onFailure(@NonNull Exception exception) {
////                                    // Handle unsuccessful uploads
////                                    // ...
////                                }
////                            });
////                }
////            }
////        } catch (Exception e) {
////            Log.e("EAG", "onActivityResult - target image selection error ", e);
////        }
////    }
//}