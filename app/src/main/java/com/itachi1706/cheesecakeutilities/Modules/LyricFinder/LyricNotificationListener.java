package com.itachi1706.cheesecakeutilities.Modules.LyricFinder;

import android.annotation.SuppressLint;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.support.annotation.RequiresApi;

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class LyricNotificationListener extends NotificationListenerService {
    public LyricNotificationListener() {
    }
}
