package com.itachi1706.cheesecakeutilities.games.chromiumtrex;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.R;

public class ChromiumTRexActivity extends BaseModuleActivity {

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

        webView = findViewById(R.id.webview);

        if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            WebView.setWebContentsDebuggingEnabled(true);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.loadUrl("file:///android_asset/chromiumtrex/index.html");
    }
}
