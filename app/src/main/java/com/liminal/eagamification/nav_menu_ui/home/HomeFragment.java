package com.liminal.eagamification.nav_menu_ui.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.liminal.eagamification.EasyAugmentHelper;
import com.liminal.eagamification.MenuActivity;
import com.liminal.eagamification.R;
import com.unity3d.player.UnityPlayerActivity;

import java.io.File;
import java.util.Objects;

public class HomeFragment extends Fragment {

    //Location services
    private FusedLocationProviderClient client;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        ImageView loaderView = root.findViewById(R.id.menuAdvertisementImageView);
        Glide.with(this).asGif().load(R.drawable.scenery).into(loaderView);

        EasyAugmentHelper easyAugmentHelper = new EasyAugmentHelper("101", Objects.requireNonNull(getActivity()), MenuActivity.class.getName());
        easyAugmentHelper.loadMarkerImages();

        Button haikuButton = root.findViewById(R.id.gameStartButton1);
        Button driveButton = root.findViewById(R.id.gameStartButton2);

        client = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));

        haikuButton.setOnClickListener(v -> {
            AlphaAnimation buttonClick = new AlphaAnimation(1f, 0.5f);
            buttonClick.setDuration(100);
            v.startAnimation(buttonClick);
            client.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                if(location!=null)
                    Log.d("EAG_LOCATION",location.toString());
            });
            Toast.makeText(getContext(), "Database is setting up, please wait.", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(easyAugmentHelper::activateScanner,100);
        });

        driveButton.setOnClickListener(v -> {
            AlphaAnimation buttonClick = new AlphaAnimation(1f, 0.5f);
            buttonClick.setDuration(100);
            v.startAnimation(buttonClick);
            new Handler().postDelayed(() -> {
                Intent unityIntent = new Intent(getActivity(), UnityPlayerActivity.class);
                startActivity(unityIntent);
            },100);
        });

        return root;
    }

//    void chooseNewImage() {
//        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
//        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
//        intent.setType("image/*");
//        startActivityForResult(android.content.Intent.createChooser(intent, "Select target augmented image"),1);
//    }
//
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        try {
//            if (resultCode == android.app.Activity.RESULT_OK) {
//                if (requestCode == 1)
//                {
//                    Uri imagePath = data.getData();
////                    File file = new File(imagePath.getPath());
//                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("stor.jpg");
//
//                    storageReference.putFile(imagePath)
//                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                @Override
//                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                    // Get a URL to the uploaded content
//
////                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception exception) {
//                                    // Handle unsuccessful uploads
//                                    // ...
//                                }
//                            });
//                }
//            }
//        } catch (Exception e) {
//            Log.e("EAG", "onActivityResult - target image selection error ", e);
//        }
//    }
}
