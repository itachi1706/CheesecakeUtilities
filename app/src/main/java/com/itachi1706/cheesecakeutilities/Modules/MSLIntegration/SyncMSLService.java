package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.itachi1706.cheesecakeutilities.Util.LogHelper;

/**
 * Created by Kenneth on 14/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
public class SyncMSLService extends JobService {

    private static final String TAG = "SyncMSL-Svc";
    public static final String ACTION_SYNC_MSL = "msl-sync-task-svc";

    public SyncMSLService() {
        // Required for Firebase JobDispatcher API
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        dispatcher.cancel(ACTION_SYNC_MSL);
        stopJob(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        LogHelper.i(TAG, "Stopping MSL Sync Job");
        return false;
    }
    
    private void stopJob(JobParameters params) {
        LogHelper.i(TAG, "Job finished, stopping");
        jobFinished(params, false);
    }
}
