package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours;

/**
 * Created by Kenneth on 28/4/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours in CheesecakeUtilities
 */

public class QHConstants {

    public static final String QH_BT_STATE = "quiethour_bt_status", QH_WIFI_STATE = "quiethour_wifi_status";
    public static final String QH_BT_TIME = "quiethour_bt", QH_WIFI_TIME = "quiethour_wifi";
    public static final String QH_BT_NOTIFICATION = "quiethour_bt_notification", QH_WIFI_NOTIFICATION = "quiethour_wifi_notification";
    public static final String QH_HISTORY = "quiethour_history", QH_HISTORY_VIEW = "quiethour_history_view";

    public static final int QH_NOTIFY_NEVER = 0, QH_NOTIFY_WHEN_TRIGGERED = 1, QH_NOTIFY_ALWAYS = 2, QH_NOTIFY_DEBUG = 3;

    public static final int BT_START_INTENT = 2000, BT_END_INTENT = 2001, WIFI_START_INTENT = 2002, WIFI_END_INTENT = 2003;
}
