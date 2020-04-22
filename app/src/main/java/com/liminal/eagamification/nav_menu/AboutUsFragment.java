package com.liminal.eagamification.nav_menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.liminal.eagamification.R;

public class AboutUsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);

        // Get name of the website from ScanActivity
        String website = "https://liminal.in/about#Team";

        // Open required Website
        WebView webView = view.findViewById(R.id.aboutUsWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(website);

        return view;
    }
}
