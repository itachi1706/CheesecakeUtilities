package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.BaseBroadcastReceiver;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarModel;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.CalendarAddTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.CalendarLoadTask;
import com.itachi1706.cheesecakeutilities.R;

import java.util.Collections;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.MSLActivity.MSL_SP_ACCESS_TOKEN;
import static com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.MSLActivity.MSL_SP_GOOGLE_OAUTH;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class SyncMSLService extends JobService {

    private static final String TAG = "SyncMSL-Svc";
    private MSLServiceReceiver receiver;
    private SharedPreferences sp;

    public static final String ACTION_SYNC_MSL = "msl-sync-task-svc";

    // Google Stuff
    Calendar client;
    CalendarModel model = new CalendarModel();
    GoogleAccountCredential credential;

    public SyncMSLService() {}

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "Starting MSL Sync Job");
        // Register receiver
        receiver = new MSLServiceReceiver();
        IntentFilter filter = new IntentFilter(CalendarAsyncTask.BROADCAST_MSL_ASYNC);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        sp = PrefHelper.getDefaultSharedPreferences(this);

        // Setup stuff
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccountName(sp.getString(MSL_SP_GOOGLE_OAUTH, null));
        client = new Calendar.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credential).setApplicationName("MSLIntegration/1.0").build();

        handleWork(params.getExtras(), params.getTag());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "Stopping MSL Sync Job");
        // Unregister receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        // TODO: Schedule a new job that replaces any job currently and is recurring forever
        return false;
    }

    protected void handleWork(@NonNull Bundle intent, @NonNull String action) {
        if (intent != null && hasToken() && hasGoogleOAuth()) {
            if (intent.getBoolean("manual", false)) Toast.makeText(this, "Sync Started", Toast.LENGTH_LONG).show();
            switch (action) {
                case ACTION_SYNC_MSL:
                    // Check that calendar exists
                    CalendarLoadTask.run(this, "TASK", model, client);
                default:
                    Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show();
                    break;
            }
        }

    }

    private void proceedWithSynchronization() {
        // TODO: Get data from MSL
        // TODO: Compare with existing data
        // TODO: Get events that needs to be added/edited/removed
        // TODO: Save new data to disk
        // TODO: Save new metric data to disk
        // TODO: Sync with calendar
    }

    private void process(String task) {
        Log.i(TAG, "Task Completed: " + task);
        switch (task.toUpperCase()) {
            case "ADD":
                Log.i(TAG, "MSL Task Sync Calendar created. doing synchronization");
                proceedWithSynchronization();
                break;
            case "LOAD-TASK":
                String id = sp.getString("msl-cal-task-id", "");
                if (id.isEmpty() || model.get(id) == null) {
                    Log.w(TAG, "Calendar MSL Task not found, creating calendar");
                    com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
                    calendar.setSummary("MSL Task Calendar Sync");
                    calendar.setDescription("Calendar used by CheesecakeUtilities to store tasks obtained from MSL and updated");
                    calendar.setTimeZone("Asia/Singapore");
                    calendar.setLocation("Singapore");
                    new CalendarAddTask(this, model, client, calendar, "msl-cal-task-id").execute();
                    return;
                }
                Log.i(TAG, "MSL Task Calendar found. doing synchronization");
                proceedWithSynchronization();
                break;
        }
    }

    private boolean hasToken() {
        return sp.contains(MSL_SP_ACCESS_TOKEN);
    }

    private boolean hasGoogleOAuth() {
        return sp.contains(MSL_SP_GOOGLE_OAUTH);
    }

    class MSLServiceReceiver extends BaseBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            if (intent.getBooleanExtra("exception", false)) {
                Exception e = (Exception) intent.getSerializableExtra("error");
                NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                if (manager == null) return;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.createNotificationChannel(new NotificationChannel("error-messages", "Service Errors", NotificationManager.IMPORTANCE_DEFAULT));
                }
                String errorMessage = "An error has occurred (" + e.getLocalizedMessage() + ")";
                Intent launchIntent = new Intent(context, MSLActivity.class);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
                Notification errorNotification = new NotificationCompat.Builder(context, "error-messages").setContentTitle("MSL Sync Error")
                        .setContentText("An error has occurred (" + e.getLocalizedMessage() + ")").setSmallIcon(R.drawable.notification_icon)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(errorMessage + "\nClick to enter the application to do a manual sync")).setContentIntent(pendingIntent).build();
                manager.notify(new Random().nextInt(), errorNotification);
            } else {
                process(intent.getStringExtra("data"));
            }
        }
    }
}
