package com.liminal.eagamification;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

class AugmentedViewManager {
    private View view;
    private Context context;

    AugmentedViewManager(View view, Context scannerContext)
    {
        context = scannerContext;
        this.view = view;
        Button button = view.findViewById(R.id.button);
        button.setOnClickListener(view1 -> popUpEditText());
    }

    private void popUpEditText() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Comments");

        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // do something here on OK

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void onViewInflated()
    {
        Button button = view.findViewById(R.id.button);
        Intent intent = new Intent(context, RedirectWeb.class);
        intent.putExtra("WEBSITE", "https://github.com/google-ar/sceneform-android-sdk/issues/989");
        button.setOnClickListener(view -> context.startActivity(intent));
        TextView textView = view.findViewById(R.id.textView);

    }
}
