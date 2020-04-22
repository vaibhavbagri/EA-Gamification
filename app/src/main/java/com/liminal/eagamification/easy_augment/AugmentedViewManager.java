package com.liminal.eagamification.easy_augment;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.R;

import androidx.annotation.NonNull;

class AugmentedViewManager {
    private Context context;

    AugmentedViewManager(View view, Context scannerContext)
    {
        context = scannerContext;

        Button button = view.findViewById(R.id.button);
        button.setOnClickListener(dialog -> createPopUpDialog());

        TextView haikuContentView = view.findViewById(R.id.haikuContentView);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("haikuContent");
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                haikuContentView.setText(String.valueOf(dataSnapshot.child("haikuText").getValue()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };

        databaseReference.addValueEventListener(eventListener);

    }

    private void createPopUpDialog()
    {
        // Dialog for taking text input
        final Dialog inputTextDialog = new Dialog(context);
        inputTextDialog.setContentView(R.layout.dialog_text_input);

        Button cancelButton = inputTextDialog.findViewById(R.id.dialogInputTextCancelButton);
        Button uploadButton = inputTextDialog.findViewById(R.id.dialogInputTextUploadButton);

        EditText inputText = inputTextDialog.findViewById(R.id.dialogInputTextEditText);

        cancelButton.setOnClickListener(view -> inputTextDialog.dismiss());
        uploadButton.setOnClickListener(v -> {
            uploadText(String.valueOf(inputText.getText()));
            inputTextDialog.dismiss();
        });

        inputTextDialog.show();
    }

    private void uploadText(String text){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("haikuContent");
        databaseReference.child("haikuText").setValue(text);
    }
}
