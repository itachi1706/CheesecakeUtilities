package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class SyncMSLService extends JobIntentService {

    private static final String TAG = "SyncMSL-Svc";
    private final Handler handler;

    public static final String ACTION_SYNC_MSL = "com.itachi1706.cheesecakeutilities.MSL_SYNC_TASK";

    public SyncMSLService(Context context) {
        handler = new Handler(context.getMainLooper());
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_SYNC_MSL:

                default:
                    Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private void runOnUiThread(Runnable r) {
        handler.post(r);
    }
}
