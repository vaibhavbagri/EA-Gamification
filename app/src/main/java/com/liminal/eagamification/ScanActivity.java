package com.liminal.eagamification;

// Import statements
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.bumptech.glide.Glide;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.ArrayList;
import java.util.Objects;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ScanActivity extends AppCompatActivity {

    private AugmentedImageFragment arFragment;
    private ImageView scannerView;

    private Scene scene;

    // Required for video augmentation
    private AugmentVideo augmentVideo;

    // Variable that stores Marker detected in previous frame
    private AugmentedImage currentMarker = null;

    // Arraylist to store image details
    private ArrayList<ImageDetails> imageDetailsArrayList;

    // Layout for loading bar
    private RelativeLayout loadingBar;
    private TextView loadingText;

    //Augmentation flag: 0 - 3D model , 1 - video , 2 - view
    private String augmentFlag;

    //Required for view augmentation
    private AugmentView augmentView;

    //Required for playing audio
    private SimpleExoPlayer player;
    private boolean isAudioPlaying = false;

    //Required for augmenting 3D model
    private boolean isAugmenting = false;
    private AugmentedImageNode node;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // Initializes the loading bar
        setupLoadingBar();

        // Load the imageDetails table
        imageDetailsArrayList = DBManager.getDownloadedFromImageDetails();

        // Load Augmented Image Fragment
        arFragment = (AugmentedImageFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        scannerView = findViewById(R.id.scanner_view);

        //Initialize Augment Video player
        augmentVideo = new AugmentVideo(this);

        //Initialize audio player
        player = new SimpleExoPlayer.Builder(this).build();

        augmentView = new AugmentView(arFragment,this);


        if (arFragment != null) {
            scene = arFragment.getArSceneView().getScene();
            scene.addOnUpdateListener(this::onUpdateFrame);
        }

    }

    //This method is called at the start of each frame. @param frameTime = time since last frame.
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame == null) return;

        // Detect marker present in the frame
        AugmentedImage newMarker = detectMarker(frame);

        // If a New marker is detected update the current marker
        if(newMarker !=  null)
        {
            if(currentMarker == null)
                currentMarker = newMarker;

            else if(newMarker.getIndex() != currentMarker.getIndex()) {

                currentMarker = newMarker;

                //Stop video being played on previous marker
                if(augmentVideo.isTracking)
                {
                    augmentVideo.isTracking = false;
                    augmentVideo.release(scene);
                }
                //Remove view augmented on previous marker
                if(augmentView.isTracking)
                {
                    augmentView.isTracking = false;
                    augmentView.release(scene);
                }

                //Stop playing audio when new marker detected
                if (isAudioPlaying) {
                    isAudioPlaying = false;
                    player.release();
                }

                //Remove 3D model when new marker detected
                if (isAugmenting) {
                    isAugmenting = false;
                    delete_model();
                }

                //If new marker has been tracked before, re initialize its necessary variables
                if(newMarker.getTrackingState() == TrackingState.TRACKING){
                    augmentFlag = imageDetailsArrayList.get(currentMarker.getIndex()).redirectTo;
                }
            }
        }

        if(currentMarker == null) return;

        // Work with the marker that is currently in frame
        switch (currentMarker.getTrackingState()) {
            case PAUSED: //image has been detected.
                Log.d("SCAN_ACTIVITY_TRACKING_STATE", "tracking state : PAUSED");

                if(loadingBar.getVisibility() == View.GONE)
                    setLoadingBarVisible("Image Found. Processing Environment");
                switch (imageDetailsArrayList.get(currentMarker.getIndex()).redirectTo)
                {
                    case "0": // Open Activity
                        Intent intent = getIntent();
                        String redirectActivityName = intent.getStringExtra("REDIRECT_ACTIVITY_NAME");
                        if (redirectActivityName != null) {
                            ComponentName cn = new ComponentName(this, redirectActivityName);
                            Intent newActivity = new Intent().setComponent(cn);
                            newActivity.putExtra("IMAGE_NAME", imageDetailsArrayList.get(currentMarker.getIndex()).imageName);
                            startActivity(newActivity);
                            Log.d("SCAN_ACTIVITY_REDIRECT_TO", "Redirecting to Activity : " + redirectActivityName);
                        } else
                            Log.d("SCAN_ACTIVITY_REDIRECT_TO", "Redirect Activity name is not specified");
                        break;

                    case "1": // Open website
                        String website = imageDetailsArrayList.get(currentMarker.getIndex()).redirect;
                        Intent newWebActivity = new Intent(this, RedirectWeb.class);
                        newWebActivity.putExtra("WEBSITE", website);
                        startActivity(newWebActivity);
                        Log.d("SCAN_ACTIVITY_REDIRECT_TO", "Redirecting to Website : " + website);
                        break;

                    case "2": // Open Video
                        String videoURL = imageDetailsArrayList.get(currentMarker.getIndex()).redirect;
                        Intent newVideoActivity = new Intent(this, RedirectVideo.class);
                        newVideoActivity.putExtra("VIDEO_URL", videoURL);
                        startActivity(newVideoActivity);
                        Log.d("SCAN_ACTIVITY_REDIRECT_TO", "Redirecting to Video : " + videoURL);
                        break;

                    case "3": // Augment Model
                        Log.d("SCAN_ACTIVITY_REDIRECT_TO", "Augmenting model : " + imageDetailsArrayList.get(currentMarker.getIndex()).redirect);
                        augmentFlag = "3";
                        break;

                    case "4": // Augment Video
                        Log.d("SCAN_ACTIVITY_REDIRECT_TO", "Augmenting video : " + imageDetailsArrayList.get(currentMarker.getIndex()).redirect);
//                        augmentVideo.videoAugment(imageDetailsArrayList.get(currentMarker.getIndex()).redirect,this);
                        augmentFlag = "4";
                        break;

                    case "5": //Augment View
                        Log.d("SCAN_ACTIVITY_REDIRECT_TO", "Augmenting view : " + imageDetailsArrayList.get(currentMarker.getIndex()).redirect);
                        augmentFlag = "5";
                        break;

                    case "6": //Play Audio
                        if(!isAudioPlaying)
                        {
                            String url = imageDetailsArrayList.get(currentMarker.getIndex()).redirect;
                            Log.d("SCAN_ACTIVITY_REDIRECT_TO", "Playing audio : " + url);
                            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "EasyAugment");
                            Uri uri = Uri.parse(url);
                            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
                            player.prepare(mediaSource, false, false);
                            player.setPlayWhenReady(true);
                            isAudioPlaying = true;
                        }
                        break;
                }
                break;

            case TRACKING: // Image is being tracked
                Log.d("SCAN_ACTIVITY_TRACKING_STATE", "tracking state : " + currentMarker.getTrackingMethod());

                // Set visibility of scanner depending on tracking status
                if(currentMarker.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING)
                {
                    if(loadingBar.getVisibility() == View.VISIBLE)
                    {
                        loadingBar.setVisibility(View.GONE);
                        scannerView.setVisibility(View.GONE);
                    }
                }
                else if(currentMarker.getTrackingMethod() == AugmentedImage.TrackingMethod.LAST_KNOWN_POSE)
                {
                    if(loadingBar.getVisibility() == View.GONE)
                    {
                        scannerView.setVisibility(View.VISIBLE);
                        setLoadingBarVisible("Lost Marker. Searching...");
                    }
                }

                switch (augmentFlag)
                {
                    case "3": //Augment Model
                        if(!isAugmenting)
                        {
                            node = new AugmentedImageNode(this, imageDetailsArrayList.get(currentMarker.getIndex()).redirect,arFragment);
                            node.setImage(currentMarker);
                            scene.addChild(node);
                            Log.d("SCAN_ACTIVITY_MODEL", "Model augmented");
                            isAugmenting = true;
                        }
                        break;

                    case "4": //Augment Video
                        if(!augmentVideo.isTracking)
                        {
                            augmentVideo.videoAugment(imageDetailsArrayList.get(currentMarker.getIndex()).redirect,this);
                            augmentVideo.playVideo(currentMarker.createAnchor(currentMarker.getCenterPose()), currentMarker.getExtentX(), currentMarker.getExtentZ(), scene);
                            Log.d("SCAN_ACTIVITY_VIDEO", "Video is playing");
                            augmentVideo.isTracking = true;
                        }
                        break;

                    case "5": //Augment View
                        if(!augmentView.isTracking)
                        {
                            //Get view ID of layout to be rendered
                            int viewID = getResources().getIdentifier(imageDetailsArrayList.get(currentMarker.getIndex()).redirect,"layout",this.getPackageName());
                            augmentView.createViewRenderable(currentMarker.createAnchor(currentMarker.getCenterPose()),viewID);
                            Log.d("SCAN_ACTIVITY_VIEW", "View Augmented");
                            augmentView.isTracking = true;
                        }
                        break;
                }
                break;

            case STOPPED: // Image Marker is not present in camera frame
                Log.d("SCAN_ACTIVITY_TRACKING_STATE", "tracking state : STOPPED");
//                augmentedImageMap.remove(currentMarker);
                break;
        }
    }

    private void delete_model() {
        scene.removeChild(node);
        Objects.requireNonNull(node.getAnchor()).detach();
        node.setParent(null);
        node = null;
    }

    // Function to detect markers
    private AugmentedImage detectMarker(Frame frame) {
        for (AugmentedImage augmentedImage : frame.getUpdatedTrackables(AugmentedImage.class)) {
            if (augmentedImage.getTrackingState() == TrackingState.PAUSED || augmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING)
            {
                Log.d("SCAN_ACTIVITY_DETECT", String.valueOf(augmentedImage.getIndex()));
                return augmentedImage;
            }
        }
        return null;
    }

    // Function to setup loading bar
    private void setupLoadingBar()
    {
        ImageView loadingImgView = findViewById(R.id.loadingImgView);
        loadingText = findViewById(R.id.loadingTextView);
        loadingBar = findViewById(R.id.loadingBar);

        Glide.with(this)
                .load(R.drawable.loading_spinner)
                .centerCrop()
                .transition(withCrossFade())
                .into(loadingImgView);
    }

    // Function to set Text in the loading bar and make it visible
    private void setLoadingBarVisible(String text)
    {
        loadingBar.setVisibility(View.VISIBLE);
        loadingText.setText(text);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (augmentedImageMap.isEmpty())
//            scannerView.setVisibility(View.VISIBLE);
//    }

    @Override
    protected void onPause() {
        super.onPause();
        onStop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(player!=null)
            player.release();
        player = null;
        Log.d("SCAN_ACTIVITY_STOP", "JUST STOP IT PLEASE");
        // Destroy the scanner activity once an image is found
        finish();
    }
}