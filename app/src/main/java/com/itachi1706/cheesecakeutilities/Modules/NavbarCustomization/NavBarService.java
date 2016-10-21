package com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.R;
import com.squareup.picasso.Picasso;

import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_IMAGE_TYPE_APP;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_IMAGE_TYPE_RANDOM_IMG;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_IMAGE_TYPE_STATIC;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_SHOW_APPNAME;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_SHOW_CLOCK;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_SHOW_IMAGE;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_SHOW_IMAGE_TYPE;

public class NavBarService extends AccessibilityService {

    private static final String TAG = "NavBarService";

    private WindowManager mWindowManager;
    private static SharedPreferences sharedPreferences;

    private View mNavBarView;
    private TextView tvAppName;
    private TextClock clock;
    private ImageView ivImage;

    private static boolean useAppColor = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        Log.i(TAG, "NavBarService connected");
        receiver = new ResponseReceiver();
        IntentFilter filter = new IntentFilter(Broadcasts.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        // Set the type of events that this service wants to listen to.  Others
        // won't be passed to this service. `TYPE_WINDOW_STATE_CHANGED`
        // has been used only for demo purposes.
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        // no delay before we are notified about an accessibility event
        info.notificationTimeout = 0;

        setServiceInfo(info);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        addNavView();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (tvAppName == null) return; // Can't proceed

        CharSequence display = null;
        PackageManager pm = getPackageManager();

        // We'll retrieve and display the current application's label
        if (event != null && !TextUtils.isEmpty(event.getPackageName())) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(event.getPackageName().toString(), 0);

                if (appInfo != null) {
                    display = pm.getApplicationLabel(appInfo);
                }

                // If App Color is true
                if (useAppColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Resources res = pm.getResourcesForApplication(event.getPackageName().toString());
                    final int[] attrs = new int[]{
                            res.getIdentifier("colorPrimaryDark", "attr", event.getPackageName().toString())
                    };
                    Resources.Theme t = res.newTheme();
                    Intent launchIntent = pm.getLaunchIntentForPackage(event.getPackageName().toString());
                    if (launchIntent != null) {
                        ComponentName cn = launchIntent.getComponent();
                        t.applyStyle(pm.getActivityInfo(cn, 0).theme, false);
                        TypedArray a = t.obtainStyledAttributes(attrs); // Obtain the colorPrimary color from the attrs
                        int colorPrimaryDark = a.getColor(0, 0); // Do something with the color
                        a.recycle();
                        ColorDrawable d = new ColorDrawable(colorPrimaryDark);
                        ivImage.setImageDrawable((colorPrimaryDark == 0) ? null : d);
                    } else {
                        ivImage.setImageDrawable(null);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        tvAppName.setText(display); // update label
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
        Log.i(TAG, "Orientation is: "
                + (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait"
                : (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape"
                : "unknown")));

        // Act on orientation change

        // #addNavView() can be modified to handle both orientations.
        // If we make the required changes, we will call the following
        // methods:

        // tryRemovingNavView();
        // addNavView();
    }

    @SuppressLint("InflateParams")
    @TargetApi(Build.VERSION_CODES.M)
    private void addNavView() {
        if (sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (Utils.IS_AT_LEAST_MARSHMALLOW && !Settings.canDrawOverlays(this))
            return; // Cannot draw overlay, exiting

        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);

        final int navBarSize = getResources().getDimensionPixelSize(R.dimen.nav_bar_size); // nav bar height since we're only designing for the portrait orientation
        String imageLink = "http://lorempixel.com/" + displayMetrics.widthPixels + "/" + navBarSize + "/abstract";

        // view that will be added/removed
        mNavBarView = LayoutInflater.from(this).inflate(R.layout.service_navbar, null);
        tvAppName = (TextView) mNavBarView.findViewById(R.id.tv_app_name); // Current App Name Label
        ivImage = (ImageView) mNavBarView.findViewById(R.id.iv_image); // Image Label (retrieve from lorempixel.com)
        clock = (TextClock) mNavBarView.findViewById(R.id.tc_clock);

        // See Image Type and do stuff with it
        String res = sharedPreferences.getString(NAVBAR_SHOW_IMAGE_TYPE, NAVBAR_IMAGE_TYPE_APP);
        switch (res) {
            case NAVBAR_IMAGE_TYPE_RANDOM_IMG:
                Picasso.with(this).load(imageLink).into(ivImage);
                useAppColor = false;
                break; // Load Image
            case NAVBAR_IMAGE_TYPE_STATIC: // TODO: To Implement, using app for now
            case NAVBAR_IMAGE_TYPE_APP:
            default:
                ivImage.setImageDrawable(null);
                useAppColor = true;
                break;
        }

        // PORTRAIT orientation
        WindowManager.LayoutParams lpNavView = new WindowManager.LayoutParams();
        lpNavView.width = WindowManager.LayoutParams.MATCH_PARENT; // match the screen's width
        lpNavView.height = navBarSize; // height was looked up in the framework's source code
        lpNavView.x = 0; // start from the left edge
        lpNavView.y = -navBarSize;
        lpNavView.format = PixelFormat.TRANSLUCENT;
        lpNavView.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY; // we need this to draw over other apps
        // Lets us draw outside screen bounds
        lpNavView.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // Since we are using Gravity.BOTTOM to position the view,
        // any value we specify to WindowManager.LayoutParams#y
        // will be measured from the bottom edge of the screen.
        // At y = 0, the view's bottom edge will sit just above
        // the navigation bar. A positive value such as y = 50 will
        // make our view 50 pixels above the top edge of the nav bar.
        // That's why we choose a negative value equal to the nav bar's height.
        lpNavView.gravity = Gravity.BOTTOM;

        updateVisibility();

        // add the view
        mWindowManager.addView(mNavBarView, lpNavView);
    }

    private void updateVisibility() {
        // Do hiding based on preferences stated
        // Clock
        if (sharedPreferences.getBoolean(NAVBAR_SHOW_CLOCK, true) && clock.getVisibility() == View.GONE)
            clock.setVisibility(View.VISIBLE);
        else if (!sharedPreferences.getBoolean(NAVBAR_SHOW_CLOCK, true) && clock.getVisibility() == View.VISIBLE)
            clock.setVisibility(View.GONE);
        // App Name
        if (sharedPreferences.getBoolean(NAVBAR_SHOW_APPNAME, true) && tvAppName.getVisibility() == View.GONE)
            tvAppName.setVisibility(View.VISIBLE);
        else if (!sharedPreferences.getBoolean(NAVBAR_SHOW_APPNAME, true) && tvAppName.getVisibility() == View.VISIBLE)
            tvAppName.setVisibility(View.GONE);
        // Image
        if (sharedPreferences.getBoolean(NAVBAR_SHOW_IMAGE, true) && ivImage.getVisibility() == View.GONE)
            ivImage.setVisibility(View.VISIBLE);
        else if (!sharedPreferences.getBoolean(NAVBAR_SHOW_IMAGE, true) && ivImage.getVisibility() == View.VISIBLE)
            ivImage.setVisibility(View.GONE);
    }

    /**
     * Try removing the view from the window.
     */
    private void tryRemovingNavView() {
        // if the window token is not null, the view is attached/added
        if (mNavBarView != null && mNavBarView.getWindowToken() != null) {
            mWindowManager.removeView(mNavBarView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "NavBarService destroyed");
        tryRemovingNavView();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG, "NavBarService interrupted");
        tryRemovingNavView();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    ResponseReceiver receiver;

    private class ResponseReceiver extends BroadcastReceiver {
        private ResponseReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            tryRemovingNavView();
            addNavView();
        }
    }
}
