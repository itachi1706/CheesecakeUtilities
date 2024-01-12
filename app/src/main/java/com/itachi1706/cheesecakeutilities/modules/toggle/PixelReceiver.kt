package com.itachi1706.cheesecakeutilities.modules.toggle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.itachi1706.helperlib.helpers.LogHelper

/**
 * Created by Kenneth on 6/2/2020.
 * for com.itachi1706.cheesecakeutilities.modules.toggle in CheesecakeUtilities
 */
class PixelReceiver(val callback: Action) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        LogHelper.d(TAG, "Received Broadcast Intent for ${intent.action}")
        if (!(intent.extras != null && intent.hasExtra(ToggleHelper.DATA_RESULT) && intent.hasExtra(ToggleHelper.DATA_EXTRA_DATA))) return // Do not handle
        callback.doAction(intent.getStringExtra(ToggleHelper.DATA_EXTRA_DATA).toString(), intent.getBooleanExtra(ToggleHelper.DATA_RESULT, false))
    }
    companion object {
        private const val TAG = "PixelReceiver"
    }

    interface Action {
        fun doAction(message: String, success: Boolean)
    }
}
