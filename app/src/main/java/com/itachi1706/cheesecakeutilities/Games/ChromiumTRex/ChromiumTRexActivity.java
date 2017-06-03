package com.itachi1706.cheesecakeutilities.Games.ChromiumTRex;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;

import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.R;

public class ChromiumTRexActivity extends BaseActivity {

    WebView webView;

    @Override
    public String getHelpDescription() {
        return "This is the T-Rex game from the Chromium Browser that appears when you are offline!" +
                "\n\nCopyright © 2014 The Chromium Authors. All rights reserved.";
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chromium_trex);

        webView = (WebView) findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/chromiumtrex/index.html");
    }
}
