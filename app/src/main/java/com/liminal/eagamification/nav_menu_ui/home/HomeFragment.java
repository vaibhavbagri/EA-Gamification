package com.liminal.eagamification.nav_menu_ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.liminal.eagamification.EasyAugmentHelper;
import com.liminal.eagamification.MenuActivity;
import com.liminal.eagamification.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        EasyAugmentHelper easyAugmentHelper = new EasyAugmentHelper("101", getActivity(), MenuActivity.class.getName());
        easyAugmentHelper.loadMarkerImages();

        Button button = root.findViewById(R.id.easy_augment_button);
        button.setOnClickListener(view -> easyAugmentHelper.activateScanner());

        return root;
    }
}
