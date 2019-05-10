package com.itachi1706.cheesecakeutilities.Modules.APKMirrorDownloader

import android.Manifest
import android.animation.*
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.itachi1706.cheesecakeutilities.Modules.APKMirrorDownloader.`interface`.AsyncResponse
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.itachi1706.cheesecakeutilities.BuildConfig
import com.itachi1706.cheesecakeutilities.R
import im.delight.android.webview.AdvancedWebView

class APKMirrorActivity : AppCompatActivity(), AdvancedWebView.Listener, AsyncResponse {

    companion object {
        private const val APKMIRROR_URL = "https://www.apkmirror.com/"
        private const val APKMIRROR_UPLOAD_URL = "https://www.apkmirror.com/apk-upload/"

        private val COLOR_STATES = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))
    }

    private var webView: AdvancedWebView? = null
    private var progressBar: ProgressBar? = null
    private var navigation: BottomNavigationView? = null
    private var fabSearch: FloatingActionButton? = null

    private var refreshLayout: SwipeRefreshLayout? = null
    private var settingsLayoutFragment: RelativeLayout? = null
    private var webContainer: RelativeLayout? = null
    private var progressBarContainer: FrameLayout? = null
    private var firstLoadingView: LinearLayout? = null

    private var shortAnimDuration: Int? = null
    private var previsionThemeColor: Int = Color.parseColor("#FF8B14")
    private var sharedPreferences: SharedPreferences? = null

    private var settingsShortcut = false
    private var triggerAction = true

    private var nfcAdapter: NfcAdapter? = null

    /**
     * Listens for user clicking on the tab again. We first check if the page is scrolled. If so we move to top, otherwise we refresh the page
     */
    private val tabReselectListener = BottomNavigationView.OnNavigationItemReselectedListener { menuItem -> scrollOrReload(if (menuItem.itemId == R.id.navigation_home) APKMIRROR_URL else if (menuItem.itemId == R.id.navigation_upload) APKMIRROR_UPLOAD_URL else null) }

    private val tabSelectListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        if (triggerAction) {
            when (menuItem.itemId) {
                R.id.navigation_home -> selectNavigationItem(APKMIRROR_URL) //Home pressed
                R.id.navigation_upload -> selectNavigationItem(APKMIRROR_UPLOAD_URL) //Upload pressed
                R.id.navigation_settings //Settings pressed
                -> {
                    if (firstLoadingView!!.visibility == View.VISIBLE)
                        firstLoadingView!!.visibility = View.GONE
                    crossFade(webContainer!!, settingsLayoutFragment!!)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        changeUIColor(ContextCompat.getColor(this@APKMirrorActivity, R.color.apkmirrorPrimary))
                }
                R.id.navigation_exit -> finish()
            }
        }
        triggerAction = true
        true
    }

    private val isWritePermissionGranted: Boolean
        get() = Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private val chromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, progress: Int) {
            //update the progressbar value
            val animation = ObjectAnimator.ofInt(progressBar!!, "progress", progress)
            animation.duration = 100 // 0.5 second
            animation.interpolator = DecelerateInterpolator()
            animation.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setTheme(R.style.AppTheme)
            setContentView(R.layout.activity_main)

            //Views
            refreshLayout = findViewById(R.id.refresh_layout)
            progressBar = findViewById(R.id.main_progress_bar)
            navigation = findViewById(R.id.navigation)
            settingsLayoutFragment = findViewById(R.id.settings_layout_fragment)
            webContainer = findViewById(R.id.web_container)
            firstLoadingView = findViewById(R.id.first_loading_view)
            webView = findViewById(R.id.main_webview)
            fabSearch = findViewById(R.id.fab_search)
            progressBarContainer = findViewById(R.id.main_progress_bar_container)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

            initSearchFab()
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            shortAnimDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

            initNavigation()

            val saveUrl = sharedPreferences!!.getBoolean("apkmirror_save_url", false)
            val url: String
            val link = intent
            val data = link.data

            if (data != null)
                url = data.toString() //App was opened from browser
            else {
                //data is null which means it was either launched from shortcuts or normally
                val bundle = link.extras
                if (bundle == null) {
                    //Normal start from launcher
                    url = if (saveUrl)
                        sharedPreferences!!.getString("apkmirror_last_url", APKMIRROR_URL)!!
                    else
                        APKMIRROR_URL
                } else {
                    //Ok it was shortcuts, check if it was settings
                    val bundleUrl = bundle.getString("url")
                    if (bundleUrl != null) {
                        if (bundleUrl == "apkmirror://settings") {
                            //It was settings
                            url = APKMIRROR_URL
                            navigation!!.selectedItemId = R.id.navigation_settings
                            crossFade(webContainer!!, settingsLayoutFragment!!)
                            settingsShortcut = true
                        } else
                            url = bundleUrl
                    } else {
                        url = if (saveUrl)
                            sharedPreferences!!.getString("apkmirror_last_url", APKMIRROR_URL)!!
                        else
                            APKMIRROR_URL
                    }
                }
            }
            initWebView(url)
            //I know not the best solution xD
            if (!settingsShortcut) {
                firstLoadingView!!.visibility = View.VISIBLE
                Handler().postDelayed({
                    if (firstLoadingView!!.visibility == View.VISIBLE)
                        crossFade(firstLoadingView!!, webContainer!!)
                }, 2000)
            }
        } catch (e: RuntimeException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
            MaterialDialog.Builder(this).title(R.string.error).content(R.string.runtime_error_dialog_content)
                    .positiveText(android.R.string.ok).neutralText(R.string.copy_log).onPositive { _, _ -> finish() }.onNeutral { _, _ ->
                        // Gets a handle to the clipboard service.
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        // Creates a new text clip to put on the clipboard
                        val clip = ClipData.newPlainText("log", e.toString())
                        clipboard.primaryClip = clip
                    }.show()
        }
    }

    private fun initNavigation() {
        //Making the bottom navigation do something
        navigation!!.setOnNavigationItemSelectedListener(tabSelectListener)
        navigation!!.setOnNavigationItemReselectedListener(tabReselectListener)
    }

    private fun initSearchFab() {
        fabSearch!!.show()
        fabSearch!!.setOnClickListener { search() }
    }


    private fun initWebView(url: String) {
        webView!!.setListener(this, this)
        webView!!.addPermittedHostname("apkmirror.com")
        webView!!.webChromeClient = chromeClient
        webView!!.setUploadableFileTypes("application/vnd.android.package-archive")
        webView!!.loadUrl(url)
        refreshLayout!!.setOnRefreshListener { webView!!.reload() }
    }

    override fun onResume() {
        super.onResume()
        webView!!.onResume()
    }

    override fun onPause() {
        webView!!.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        webView!!.onDestroy()
        super.onDestroy()
    }

    override fun onStop() {
        if (sharedPreferences!!.getBoolean("apkmirror_save_url", false) && webView!!.url != "apkmirror://settings")
            sharedPreferences!!.edit().putString("apkmirror_last_url", webView!!.url).apply()
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        webView!!.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView!!.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView!!.restoreState(savedInstanceState)
    }

    override fun onBackPressed() {
        if (settingsLayoutFragment!!.visibility != View.VISIBLE) {
            if (!webView!!.onBackPressed()) return
        } else {
            crossFade(settingsLayoutFragment!!, webContainer!!)
            if (webView != null && webView!!.url == APKMIRROR_UPLOAD_URL) {
                triggerAction = false
                navigation!!.selectedItemId = R.id.navigation_upload
            } else {
                triggerAction = false
                navigation!!.selectedItemId = R.id.navigation_home
            }
            return
        }
        super.onBackPressed()
    }


    private fun runAsync(url: String) {
        //getting apps
        val pageAsync = PageAsync()
        pageAsync.response = this@APKMirrorActivity
        pageAsync.execute(url)
    }

    private fun search() {
        MaterialDialog.Builder(this).title(R.string.search).inputRange(1, 100).input(R.string.search, R.string.nothing) { _, _ -> }.onPositive { dialog, _ ->
            if (dialog.inputEditText != null)
                webView!!.loadUrl("https://www.apkmirror.com/?s=" + dialog.inputEditText!!.text)
            else
                Toast.makeText(this@APKMirrorActivity, getString(R.string.search_error), Toast.LENGTH_SHORT).show()
        }.negativeText(android.R.string.cancel).show()
    }

    private fun scrollOrReload(url: String?) {
        if (url == null) return
        if (webView!!.scrollY != 0)
            webView!!.scrollY = 0
        else
            webView!!.loadUrl(url)
    }

    private fun selectNavigationItem(url: String) {
        if (settingsLayoutFragment!!.visibility != View.VISIBLE)
            webView!!.loadUrl(url) //settings is not visible, Load url
        else {
            //settings is visible, gonna hide it
            if (webView!!.url != url)
                webView!!.loadUrl(url)
            crossFade(settingsLayoutFragment!!, webContainer!!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                changeUIColor(previsionThemeColor)
        }
    }

    private fun crossFade(toHide: View, toShow: View) {
        toShow.alpha = 0f
        toShow.visibility = View.VISIBLE
        toShow.animate().alpha(1f).setDuration(shortAnimDuration!!.toLong()).setListener(null)
        toHide.animate().alpha(0f).setDuration(shortAnimDuration!!.toLong()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                toHide.visibility = View.GONE
            }
        })
    }

    private fun download(url: String, name: String) {
        if (!sharedPreferences!!.getBoolean("apkmirror_external_download", false)) {
            if (AdvancedWebView.handleDownload(this, url, name))
                Toast.makeText(this@APKMirrorActivity, getString(R.string.download_started), Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this@APKMirrorActivity, getString(R.string.cant_download), Toast.LENGTH_SHORT).show()
        } else
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onProcessFinish(themeColor: Int) {
        // updating interface
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) changeUIColor(themeColor)
        previsionThemeColor = themeColor
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun changeUIColor(color: Int) {
        val anim = ValueAnimator.ofArgb(previsionThemeColor, color)
        anim.setEvaluator(ArgbEvaluator())
        anim.addUpdateListener { valueAnimator ->
            progressBar!!.progressDrawable.colorFilter = LightingColorFilter(-0x1000000, valueAnimator.animatedValue as Int)
            setSystemBarColor(valueAnimator.animatedValue as Int)
            val toUpdate = ColorStateList(COLOR_STATES, intArrayOf(valueAnimator.animatedValue as Int, R.color.inactive_tabs))
            navigation!!.itemTextColor = toUpdate
            navigation!!.itemIconTintList = toUpdate
            fabSearch!!.backgroundTintList = ColorStateList.valueOf(valueAnimator.animatedValue as Int)

        }
        anim.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        anim.start()
        refreshLayout!!.setColorSchemeColors(color, color, color)

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun setSystemBarColor(color: Int) {
        val clr: Int
        //this makes the color darker or uses nicer orange color
        if (color != Color.parseColor("#FF8B14")) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[2] *= 0.8f
            clr = Color.HSVToColor(hsv)
        } else
            clr = Color.parseColor("#F47D20")

        val window = this@APKMirrorActivity.window
        window.statusBarColor = clr
    }

    private fun setupNFC(url: String) {
        if (nfcAdapter != null) { // in case there is no NFC
            try {
                // create an NDEF message containing the current URL:
                val rec = NdefRecord.createUri(url) // url: current URL (String or Uri)
                val ndef = NdefMessage(rec)
                // make it available via Android Beam:
                nfcAdapter!!.setNdefPushMessage(ndef, this, this)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    //WebView factory methods bellow
    override fun onPageStarted(url: String, favicon: Bitmap?) {
        if (!url.contains("https://www.apkmirror.com/wp-content/") || !url.contains("http://www.apkmirror.com/wp-content/")) {
            runAsync(url)
            setupNFC(url)

            //Updating bottom navigation
            if (navigation!!.selectedItemId == R.id.navigation_home) {
                if (url == APKMIRROR_UPLOAD_URL) {
                    triggerAction = false
                    navigation!!.selectedItemId = R.id.navigation_upload
                }
            } else if (navigation!!.selectedItemId == R.id.navigation_upload) {
                if (url != APKMIRROR_UPLOAD_URL) {
                    triggerAction = false
                    navigation!!.selectedItemId = R.id.navigation_home

                }
            }

            //Showing progress bar
            progressBarContainer!!.animate().alpha(1f).setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    progressBarContainer!!.visibility = View.VISIBLE
                }
            })
        }
    }

    override fun onPageFinished(url: String) {
        progressBarContainer!!.animate().alpha(0f).setDuration(resources.getInteger(android.R.integer.config_longAnimTime).toLong()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                progressBarContainer!!.visibility = View.GONE
            }
        })

        if (refreshLayout!!.isRefreshing) refreshLayout!!.isRefreshing = false
    }

    override fun onPageError(errorCode: Int, description: String, failingUrl: String) {
        if (errorCode == -2) {
            MaterialDialog.Builder(this).title(R.string.error).content(getString(R.string.error_while_loading_page) + " " + failingUrl + "(" + errorCode + " " + description + ")")
                    .positiveText(R.string.refresh).negativeText(android.R.string.cancel).neutralText("Dismiss").onPositive { dialog, _ ->
                        webView!!.reload()
                        dialog.dismiss()
                    }.onNegative { _, _ -> finish() }.onNeutral { materialDialog, _ -> materialDialog.dismiss() }.show()
        }
    }

    override fun onDownloadRequested(url: String, suggestedFilename: String, mimeType: String, contentLength: Long, contentDisposition: String, userAgent: String) {
        if (isWritePermissionGranted)
            download(url, suggestedFilename)
        else
            MaterialDialog.Builder(this@APKMirrorActivity).title(R.string.write_permission).content(R.string.storage_access)
                    .positiveText(R.string.request_permission).negativeText(android.R.string.cancel)
                    .onPositive { _, _ -> ActivityCompat.requestPermissions(this@APKMirrorActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1) }
                    .show()
    }


    override fun onExternalPageRequest(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}
