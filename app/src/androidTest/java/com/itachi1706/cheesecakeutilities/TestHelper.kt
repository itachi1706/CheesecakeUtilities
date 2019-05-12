package com.itachi1706.cheesecakeutilities

import android.os.Build
import android.view.WindowManager
import androidx.test.rule.ActivityTestRule

/**
 * Created by Kenneth on 12/5/2019.
 * for com.itachi1706.cheesecakeutilities in CheesecakeUtilities
 */
class TestHelper {

    companion object {

        @JvmStatic
        @Suppress("deprecation")
        fun wakeUpDevice(mActivityTestRule: ActivityTestRule<MainMenuActivity>) {
            val activity = mActivityTestRule.activity
            val wakeUpDevice = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    activity.setTurnScreenOn(true)
                    activity.setShowWhenLocked(true)
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else activity.window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            activity.runOnUiThread(wakeUpDevice)
        }
    }
}