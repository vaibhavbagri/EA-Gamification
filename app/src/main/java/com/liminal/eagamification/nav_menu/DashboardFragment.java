package com.liminal.eagamification.nav_menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.SingleValueDataSet;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.CircularGauge;
import com.anychart.charts.Pie;
import com.anychart.core.axes.Circular;
import com.anychart.core.gauge.pointers.Bar;
import com.anychart.enums.Align;
import com.anychart.enums.Anchor;
import com.anychart.enums.LegendLayout;
import com.anychart.graphics.vector.Fill;
import com.anychart.graphics.vector.SolidFill;
import com.anychart.graphics.vector.text.HAlign;
import com.anychart.graphics.vector.text.VAlign;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liminal.eagamification.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DashboardFragment extends Fragment{

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Get user details stored in shared preferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("User_Details", Context.MODE_PRIVATE);

        // Get Username, Profile picture, Coins and Tickets from Firebase
        DatabaseReference userProfileReference = FirebaseDatabase.getInstance().getReference().child("userProfileTable");

        // Add a listener to update UI when User Profile is updated
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                updateUserProfileLayout( root,
                        (long) dataSnapshot.child("rewardDetails").child("coins").getValue(),
                        (long) dataSnapshot.child("rewardDetails").child("tickets").getValue(),
                        (String) dataSnapshot.child("personalDetails").child("username").getValue(),
                        (String) dataSnapshot.child("personalDetails").child("photoURL").getValue(),
                        (long) dataSnapshot.child("statistics").child("general").child("campExperiences").getValue(),
                        (long) dataSnapshot.child("statistics").child("general").child("itemsCollected").getValue(),
                        (long) dataSnapshot.child("statistics").child("general").child("markersScanned").getValue(),
                        (long) dataSnapshot.child("statistics").child("general").child("rewardsClaimed").getValue());

                // Update dashboard achievement status
                if(dataSnapshot.child("statistics").child("achievements").child("dashboardVisitedStatus").getValue().equals("incomplete"))
                    userProfileReference.child(sharedPreferences.getString("id","")).child("statistics").child("achievements").child("dashboardVisitedStatus").setValue("completed");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };
        userProfileReference.child(sharedPreferences.getString("id","")).addListenerForSingleValueEvent(eventListener);

        generatePieChart(root);
//        generateCircularGaugeChart(root);

        return root;
    }



    // Function to load user details into UI
    private void updateUserProfileLayout(View view, long coins, long tickets, String userName, String photoURL, long campExperiences, long itemsCollected, long markersScanned, long rewardsClaimed)
    {
        // Update username
        TextView userNameTextView = view.findViewById(R.id.usernameTextView);
        userNameTextView.setText(userName + " ");

        // Update coins and tickets
        TextView coinsTextView = view.findViewById(R.id.coinsTextView);
        coinsTextView.setText(coins + " ");
        TextView ticketsTextView = view.findViewById(R.id.ticketsTextView);
        ticketsTextView.setText(tickets + " ");

        // Update common statistics
        TextView campExperiencesTextView = view.findViewById(R.id.campsExperiencedCountTextView);
        campExperiencesTextView.setText(campExperiences + " ");
        TextView itemsCollectedTextView = view.findViewById(R.id.itemsCollectedCountTextView);
        itemsCollectedTextView.setText(itemsCollected + " ");
        TextView markersScannedTextView = view.findViewById(R.id.markersScannedCountTextView);
        markersScannedTextView.setText(markersScanned + " ");
        TextView rewardsClaimedTextView = view.findViewById(R.id.rewardsClaimedCountTextView);
        rewardsClaimedTextView.setText(rewardsClaimed + " ");

        // Update profile picture
        ImageView profilePictureView = view.findViewById(R.id.profilePictureView);
        Glide.with(requireActivity().getApplicationContext()).load(Uri.parse(photoURL)).into(profilePictureView);

        Log.d("EAG_UPDATE_PROFILE", "Username : " + userName + " Coins : " + coins + " Tickets : " + tickets);
    }



    // Function to create a Pie Chart
    private void generatePieChart(View view)
    {
        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view);
        APIlib.getInstance().setActiveAnyChartView(anyChartView);


        Pie pie = AnyChart.pie();

        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(getActivity(), event.getData().get("x") + ":" + event.getData().get("value") + " hours", Toast.LENGTH_SHORT).show();
            }
        });

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Flappy Bird", 62));
        data.add(new ValueDataEntry("AR Portal", 4));
        data.add(new ValueDataEntry("Zombie Attack", 23));
        data.add(new ValueDataEntry("Haikumon", 15));

        pie.data(data);

        pie.title("Time spent on AR explore (Hours)");

        pie.labels().position("outside");
        pie.legend().title().enabled(false);

        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);

        anyChartView.setChart(pie);
    }



//    // Function to create a Circular Gauge Chart
//    private void generateCircularGaugeChart(View view)
//    {
//        AnyChartView anyChartView2 = view.findViewById(R.id.any_chart_view_2);
//        APIlib.getInstance().setActiveAnyChartView(anyChartView2);
//
//        CircularGauge circularGauge = AnyChart.circular();
//        circularGauge.data(new SingleValueDataSet(new String[] { "10", "69", "42", "99",}));
//        circularGauge.fill("#fff")
//                .stroke(null)
//                .padding(0d, 0d, 0d, 0d)
//                .margin(100d, 100d, 100d, 100d);
//        circularGauge.startAngle(0d);
//        circularGauge.sweepAngle(270d);
//
//        Circular xAxis = circularGauge.axis(0)
//                .radius(100d)
//                .width(1d)
//                .fill((Fill) null);
//        xAxis.scale()
//                .minimum(0d)
//                .maximum(100d);
//        xAxis.ticks("{ interval: 1 }")
//                .minorTicks("{ interval: 1 }");
//        xAxis.labels().enabled(false);
//        xAxis.ticks().enabled(false);
//        xAxis.minorTicks().enabled(false);
//
//        circularGauge.label(0d)
//                .text("Flappy Bird, <span style=\"\">10%</span>")
//                .useHtml(true)
//                .hAlign(HAlign.CENTER)
//                .vAlign(VAlign.MIDDLE);
//        circularGauge.label(0d)
//                .anchor(Anchor.RIGHT_CENTER)
//                .padding(0d, 10d, 0d, 0d)
//                .height(17d / 2d + "%")
//                .offsetY(100d + "%")
//                .offsetX(0d);
//        Bar bar0 = circularGauge.bar(0d);
//        bar0.dataIndex(0d);
//        bar0.radius(100d);
//        bar0.width(17d);
//        bar0.fill(new SolidFill("#64b5f6", 1d));
//        bar0.stroke(null);
//        bar0.zIndex(5d);
//        Bar bar100 = circularGauge.bar(100d);
//        bar100.dataIndex(5d);
//        bar100.radius(100d);
//        bar100.width(17d);
//        bar100.fill(new SolidFill("#F5F4F4", 1d));
//        bar100.stroke("1 #e5e4e4");
//        bar100.zIndex(4d);
//
//        circularGauge.label(1d)
//                .text("Zombie Attack, <span style=\"\">69%</span>")
//                .useHtml(true)
//                .hAlign(HAlign.CENTER)
//                .vAlign(VAlign.MIDDLE);
//        circularGauge.label(1d)
//                .anchor(Anchor.RIGHT_CENTER)
//                .padding(0d, 10d, 0d, 0d)
//                .height(17d / 2d + "%")
//                .offsetY(80d + "%")
//                .offsetX(0d);
//        Bar bar1 = circularGauge.bar(1d);
//        bar1.dataIndex(1d);
//        bar1.radius(80d);
//        bar1.width(17d);
//        bar1.fill(new SolidFill("#1976d2", 1d));
//        bar1.stroke(null);
//        bar1.zIndex(5d);
//        Bar bar101 = circularGauge.bar(101d);
//        bar101.dataIndex(5d);
//        bar101.radius(80d);
//        bar101.width(17d);
//        bar101.fill(new SolidFill("#F5F4F4", 1d));
//        bar101.stroke("1 #e5e4e4");
//        bar101.zIndex(4d);
//
//        circularGauge.label(2d)
//                .text("Half life Alyx, <span style=\"\">42%</span>")
//                .useHtml(true)
//                .hAlign(HAlign.CENTER)
//                .vAlign(VAlign.MIDDLE);
//        circularGauge.label(2d)
//                .anchor(Anchor.RIGHT_CENTER)
//                .padding(0d, 10d, 0d, 0d)
//                .height(17d / 2d + "%")
//                .offsetY(60d + "%")
//                .offsetX(0d);
//        Bar bar2 = circularGauge.bar(2d);
//        bar2.dataIndex(2d);
//        bar2.radius(60d);
//        bar2.width(17d);
//        bar2.fill(new SolidFill("#ef6c00", 1d));
//        bar2.stroke(null);
//        bar2.zIndex(5d);
//        Bar bar102 = circularGauge.bar(102d);
//        bar102.dataIndex(5d);
//        bar102.radius(60d);
//        bar102.width(17d);
//        bar102.fill(new SolidFill("#F5F4F4", 1d));
//        bar102.stroke("1 #e5e4e4");
//        bar102.zIndex(4d);
//
//        circularGauge.label(3d)
//                .text("AR portal, <span style=\"\">99%</span>")
//                .useHtml(true)
//                .hAlign(HAlign.CENTER)
//                .vAlign(VAlign.MIDDLE);
//        circularGauge.label(3d)
//                .anchor(Anchor.RIGHT_CENTER)
//                .padding(0d, 10d, 0d, 0d)
//                .height(17d / 2d + "%")
//                .offsetY(40d + "%")
//                .offsetX(0d);
//        Bar bar3 = circularGauge.bar(3d);
//        bar3.dataIndex(3d);
//        bar3.radius(40d);
//        bar3.width(17d);
//        bar3.fill(new SolidFill("#ffd54f", 1d));
//        bar3.stroke(null);
//        bar3.zIndex(5d);
//        Bar bar103 = circularGauge.bar(103d);
//        bar103.dataIndex(5d);
//        bar103.radius(40d);
//        bar103.width(17d);
//        bar103.fill(new SolidFill("#F5F4F4", 1d));
//        bar103.stroke("1 #e5e4e4");
//        bar103.zIndex(4d);
//
//        circularGauge.margin(20d, 0d, 0d, 20d);
//        circularGauge.title()
//                .text("Minigame progression' +\n" +
//                        "    '<br/><span style=\"color:#929292; font-size: 12px;\">(Games maybe updated)</span>")
//                .useHtml(true);
//        circularGauge.title().enabled(true);
//        circularGauge.title().hAlign(HAlign.CENTER);
//        circularGauge.title()
//                .padding(0d, 0d, 0d, 0d)
//                .margin(0d, 0d, 20d, 0d);
//
//        anyChartView2.setChart(circularGauge);
//    }
}