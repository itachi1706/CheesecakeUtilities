package com.itachi1706.cheesecakeutilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.itachi1706.cheesecakeutilities.util.LogInit;

/**
 * Created by Kenneth on 2/1/2019.
 * for com.itachi1706.cheesecakeutilities in CheesecakeUtilities
 */
public abstract class BaseBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        LogInit.initLogger();
    }
}
