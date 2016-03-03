package com.itachi1706.cheesecakeutilities.Updater.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Kenneth on 3/3/2016.
 * for com.itachi1706.cheesecakeutilities.Updater.Util in CheesecakeUtilities
 */
public class ConnectivityHelper {

    /**
     * Gets the Network Info Object
     * @param context Context
     * @return Network Info
     */
    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager ) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if the android device has Internet
     * @param context Context
     * @return True if internet, false otherwise
     */
    public static boolean hasInternetConnection(Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Check if connection is a WIFI connection
     * @param context Context
     * @return True if WIFI, false otherwise
     */
    public static boolean isWifiConnection(Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);
        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Check if connection is Mobile Data
     * @param context Context
     * @return True if Cellular (Mobile), false otherwise
     */
    public static boolean isCellularConnection(Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);
        return activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * Get the current active network type
     * @param context Context
     * @return Active Netwrok Type (ConnectivityManager.TYPE_WIFI etc.)
     */
    public static int getActiveNetworkType(Context context) {
        return getNetworkInfo(context).getType();
    }
}
