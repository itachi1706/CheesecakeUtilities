package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Kenneth on 19/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
class MslWebViewWhyActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MSL-WebViewWhy"
        const val URL = "file:///android_asset/msl/why.html"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = WebView(this)
        setContentView(view)

        val ws = view.settings
        ws.builtInZoomControls = true
        ws.displayZoomControls = true
        view.loadUrl(URL)
    }
}