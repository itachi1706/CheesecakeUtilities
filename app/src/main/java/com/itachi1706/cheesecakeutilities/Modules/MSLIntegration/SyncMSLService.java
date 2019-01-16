package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

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
import com.google.gson.Gson;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.appupdater.Util.ValidationHelper;
import com.itachi1706.cheesecakeutilities.BaseBroadcastReceiver;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarModel;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.MSLData;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.CalendarAddTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.CalendarLoadTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.RetrieveMSLData;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util.FileCacher;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util.MSLHelper;
import com.itachi1706.cheesecakeutilities.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.MSLActivity.MSL_SP_ACCESS_TOKEN;
import static com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.MSLActivity.MSL_SP_GOOGLE_OAUTH;

/**
 * Created by Kenneth on 14/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
public class SyncMSLService extends JobService {

    private static final String TAG = "SyncMSL-Svc";
    private MSLServiceReceiver receiver;
    private SharedPreferences sp;

    private JobParameters parameters;

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
        IntentFilter filter = new IntentFilter();
        filter.addAction(CalendarAsyncTask.BROADCAST_MSL_ASYNC);
        filter.addAction(RetrieveMSLData.BROADCAST_MSL_DATA_SYNC);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        sp = PrefHelper.getDefaultSharedPreferences(this);

        // Setup stuff
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccountName(sp.getString(MSL_SP_GOOGLE_OAUTH, null));
        client = new Calendar.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credential).setApplicationName("MSLIntegration/1.0").build();

        parameters = params;
        handleWork(params.getExtras(), params.getTag());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "System invoked. Stopping MSL Sync Job");
        cleanup();
        return false;
    }

    private void cleanup() {
        // Unregister receiver
        if (receiver != null) LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        receiver = null;
        // TODO: Schedule a new job that replaces any job currently and is recurring forever
    }
    
    private void stopJob(boolean needReschedule) {
        Log.i(TAG, "Job finished, stopping");
        cleanup();
        jobFinished(parameters, needReschedule);
    }

    protected void handleWork(@NonNull Bundle intent, @NonNull String action) {
        if (intent != null && hasToken() && hasGoogleOAuth()) {
            if (intent.getBoolean("manual", false)) Toast.makeText(this, "Sync Started", Toast.LENGTH_LONG).show();
            switch (action) {
                case ACTION_SYNC_MSL:
                    // Check that calendar exists
                    CalendarLoadTask.run(this, "TASK-SYNC", model, client);
                    break;
                default:
                    Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show();
                    stopJob(false);
                    break;
            }
        }

    }

    private void parseMSLData(String data) {
        if (data.equalsIgnoreCase("null")) {
            Log.e(TAG, "An error occurred retrieving data. Stopping job");
            stopJob(false);
            return;
        }
        Log.d(TAG, "JSON Data: " + data);

        Gson gson = new Gson();
        MSLData main = gson.fromJson(data, MSLData.class);
        String mainJson = gson.toJson(main);
        FileCacher c = new FileCacher(this);
        String existing = c.getStringFromFile();
        if (existing == null || existing.compareTo(mainJson) != 0) {
            // Update file
            c.writeToFile(mainJson);
        }

        HashMap<String, MSLData.Task> toAdd = new HashMap<>();
        HashMap<String, MSLData.Task> toRemove = new HashMap<>();
        HashMap<String, MSLData.Task> toUpdate = new HashMap<>(); // Old data
        for (MSLData.Task t : main.getTasks()) {
            toAdd.put(t.getGuid(), t);
        }
        if (existing != null) {
            MSLData eTask = gson.fromJson(existing, MSLData.class);
            HashMap<String, MSLData.Task> existingTasks = new HashMap<>();
            for (MSLData.Task t : eTask.getTasks()) {
                existingTasks.put(t.getGuid(), t);
            }

            for (Map.Entry<String, MSLData.Task> pair : existingTasks.entrySet()) {
                if (!toAdd.containsKey(pair.getKey())) {
                    // Remove from events
                    toRemove.put(pair.getKey(), pair.getValue());
                } else {
                    // Check complete match
                    MSLData.Task t1 = toAdd.get(pair.getKey());
                    assert t1 != null; // Asserted in if statement
                    if (MSLHelper.completeMatch(t1, pair.getValue())) {
                        // Complete match, no change, remove from both
                        toAdd.remove(t1.getGuid());
                    } else {
                        // Update value
                        toUpdate.put(pair.getKey(), pair.getValue());
                        toAdd.remove(t1.getGuid());
                    }
                }
            }

            Log.d(TAG, "================================================");
            Log.d(TAG, "              Results of Sync Job");
            Log.d(TAG, "================================================");
            Log.d(TAG, "To Add: " + toAdd.size());
            Log.d(TAG, "To Remove: " + toRemove.size());
            Log.d(TAG, "To Update: " + toUpdate.size());
            Log.d(TAG, "================================================");

            // TODO: Handle update (its the main object and toUpdate hashmap)

        }

        // TODO: Get events that needs to be added/edited/removed
        // TODO: Save new metric data to SharedPreference (add,remove,update:add,remove,update:...)
        // TODO: Sync with calendar
        stopJob(false); // TODO: Test only, remove if not stopping here
    }

    private void proceedWithSynchronization() {
        new RetrieveMSLData(LocalBroadcastManager.getInstance(this), ValidationHelper.getSignatureForValidation(this),
                this.getPackageName()).execute(sp.getString(MSLActivity.MSL_SP_ACCESS_TOKEN, "-"));

    }

    private void process(String task) {
        Log.i(TAG, "Task Completed: " + task);
        switch (task.toUpperCase()) {
            case "ADD":
                Log.i(TAG, "MSL Task Sync Calendar created. doing synchronization");
                proceedWithSynchronization();
                break;
            case "LOAD-TASK-SYNC":
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
            if (intent.getAction() == null) return;
            if (intent.getAction().equalsIgnoreCase(CalendarAsyncTask.BROADCAST_MSL_ASYNC)) {
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
            } else if (intent.getAction().equalsIgnoreCase(RetrieveMSLData.BROADCAST_MSL_DATA_SYNC)) {
                if (intent.hasExtra("error")) {
                    Log.e(TAG, "Error occurred, stopping task now and hope its fixed in the future");
                    stopJob(false);
                    return;
                }
                // Data received
                parseMSLData(intent.getStringExtra("data"));
            }
        }
    }
}
