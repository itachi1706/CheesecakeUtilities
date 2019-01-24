package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MslWebViewActivity extends AppCompatActivity {

    private static final String TAG = "MSL-WebView";
    private static final String URL = "https://app.mystudylife.com";
    WebView view;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new WebView(this);
        setContentView(view);

        WebSettings ws = view.getSettings();
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.64 Safari/537.36");
        view.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, "Console Msg: " + consoleMessage.message());

                if (consoleMessage.message().startsWith("Key: ") && !consoleMessage.message().contains("null")) {
                    String key = consoleMessage.message().substring(5);
                    Log.d(TAG, "Key found! (" + key + ")");
                    new AlertDialog.Builder(MslWebViewActivity.this).setTitle("Key found")
                            .setMessage("Key detected! Your Access Key is: " + key + "\n\n" +
                                    "Click OK to save the key and exit, otherwise click cancel to just exit\nIf this is not the correct key, click Continue to go on")
                            .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                sp.edit().putString(MSLActivity.MSL_SP_ACCESS_TOKEN, key).apply();
                                finish();
                            })).setNegativeButton(android.R.string.cancel, ((dialog, which) -> finish()))
                            .setNeutralButton("Continue", ((dialog, which) -> dialog.dismiss())).show();
                }
                return super.onConsoleMessage(consoleMessage);
            }
        });
        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i(TAG, "Finished Loading: " + url);

                if (url.contains(URL)) {
                    Log.i(TAG, "Found MSL URL");
                    injectScriptFile(view); // see below ...

                    // test if the script was loaded
                    view.loadUrl("javascript:setTimeout(getKey(), 10000)");
                }
            }

            @SuppressWarnings("ResultOfMethodCallIgnored")
            private void injectScriptFile(WebView view) {
                InputStream input;
                try {
                    input = getAssets().open("msl/inject.js.min");
                    byte[] buffer = new byte[input.available()];
                    input.read(buffer);
                    input.close();

                    // String-ify the script byte-array using BASE64 encoding !!!
                    String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
                    view.loadUrl("javascript:(function() {" +
                            "var parent = document.getElementsByTagName('head').item(0);" +
                            "var script = document.createElement('script');" +
                            "script.type = 'text/javascript';" +
                            // Tell the browser to BASE64-decode the string into your script !!!
                            "script.innerHTML = window.atob('" + encoded + "');" +
                            "parent.appendChild(script)" +
                            "})()");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        view.loadUrl(URL + "/dashboard");
    }
}
