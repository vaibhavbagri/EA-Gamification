package com.liminal.eagamification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

import com.google.android.gms.location.LocationServices;

import java.util.Objects;

public class ChartsSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts_select);

        Button pieChartButton = findViewById(R.id.gameStartButton1);
        Button circularGaugeButton = findViewById(R.id.gameStartButton2);
        Button mapsButton = findViewById(R.id.gameStartButton3);

        pieChartButton.setOnClickListener(v -> {
            AlphaAnimation buttonClick = new AlphaAnimation(1f, 0.5f);
            buttonClick.setDuration(100);
            v.startAnimation(buttonClick);
            new Handler().postDelayed(() -> {
                Intent unityIntent = new Intent(this, PieChartActivity.class);
                startActivity(unityIntent);
            },100);
        });

        circularGaugeButton.setOnClickListener(v -> {
            AlphaAnimation buttonClick = new AlphaAnimation(1f, 0.5f);
            buttonClick.setDuration(100);
            v.startAnimation(buttonClick);
            new Handler().postDelayed(() -> {
                Intent unityIntent = new Intent(this, CircularGaugeActivity.class);
                startActivity(unityIntent);
            },100);
        });

        mapsButton.setOnClickListener(v -> {
            AlphaAnimation buttonClick = new AlphaAnimation(1f, 0.5f);
            buttonClick.setDuration(100);
            v.startAnimation(buttonClick);
            new Handler().postDelayed(() -> {
                Intent unityIntent = new Intent(this, MapsActivity.class);
                startActivity(unityIntent);
            },100);
        });
    }
}
