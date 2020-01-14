package com.itachi1706.cheesecakeutilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.itachi1706.cheesecakeutilities.util.LogInit;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Kenneth on 2/1/2019.
 * for com.itachi1706.cheesecakeutilities in CheesecakeUtilities
 */
public abstract class BaseBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Fabric fabric = new Fabric.Builder(context).kits(new Crashlytics()).debuggable(BuildConfig.DEBUG).build();
        if (!BuildConfig.DEBUG) Fabric.with(fabric);
        LogInit.initLogger();
    }
}
