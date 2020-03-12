package com.liminal.eagamification.nav_menu_ui.home;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.liminal.eagamification.EasyAugmentHelper;
import com.liminal.eagamification.MenuActivity;
import com.liminal.eagamification.R;

import java.io.File;

public class HomeFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        EasyAugmentHelper easyAugmentHelper = new EasyAugmentHelper("101", getActivity(), MenuActivity.class.getName());
        easyAugmentHelper.loadMarkerImages();

        CardView arButton = root.findViewById(R.id.AR);
        CardView uploadButton = root.findViewById(R.id.Upload);


//        Button arButton = root.findViewById(R.id.easyAugmentButton);
//        Button uploadButton = root.findViewById(R.id.uploadButton);

        uploadButton.setOnClickListener(view -> chooseNewImage());
        arButton.setOnClickListener(view -> easyAugmentHelper.activateScanner());

        return root;
    }

    void chooseNewImage() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(android.content.Intent.createChooser(intent, "Select target augmented image"),1);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == android.app.Activity.RESULT_OK) {
                if (requestCode == 1)
                {
                    Uri imagePath = data.getData();
//                    File file = new File(imagePath.getPath());
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("stor.jpg");

                    storageReference.putFile(imagePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content

//                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...
                                }
                            });
                }
            }
        } catch (Exception e) {
            Log.e("EAG", "onActivityResult - target image selection error ", e);
        }
    }
}
