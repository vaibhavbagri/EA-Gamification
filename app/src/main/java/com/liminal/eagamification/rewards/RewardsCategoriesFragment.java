package com.liminal.eagamification.rewards;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.liminal.eagamification.R;

public class RewardsCategoriesFragment extends Fragment implements View.OnClickListener{
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    public RewardsCategoriesFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment RewardsCategoriesFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static RewardsCategoriesFragment newInstance(String param1, String param2) {
//        RewardsCategoriesFragment fragment = new RewardsCategoriesFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_rewards_categories, container, false);
        root.findViewById(R.id.foodButton).setOnClickListener(this);
        root.findViewById(R.id.fitnessButton).setOnClickListener(this);
        root.findViewById(R.id.entertainmentButton).setOnClickListener(this);
        root.findViewById(R.id.travelButton).setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.foodButton:
                replaceFragment("Food");
                break;

            case R.id.fitnessButton:
                replaceFragment("Fitness");
                break;

            case R.id.entertainmentButton:
                replaceFragment("Entertainment");
                break;

            case R.id.travelButton:
                replaceFragment("Travel");
                break;
        }
    }

    private void replaceFragment(String category) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_rewards,new RewardsListFragment(category))
                .addToBackStack("RewardCategories")
                .commit();
    }
}
