package com.itachi1706.cheesecakeutilities.Modules.SGPsi;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.itachi1706.cheesecakeutilities.R;

/**
 * Created by Kenneth on 18/2/2018.
 * for com.itachi1706.cheesecakeutilities.Modules.SGPsi in CheesecakeUtilities
 */

public class PsiGraphFragment extends Fragment {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chromium_trex, container, false);
        WebView webView = view.findViewById(R.id.webview);

        Bundle b = getArguments();
        String key;
        if (b != null) key = b.getString("key", "psi");
        else key = "psi";
        String filename = key + "graph.html";

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/sgpsi/" + filename);
        return view;
    }

}