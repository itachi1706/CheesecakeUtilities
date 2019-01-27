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

import com.crashlytics.android.Crashlytics;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.gson.Gson;
import com.itachi1706.appupdater.Util.ConnectivityHelper;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.appupdater.Util.ValidationHelper;
import com.itachi1706.cheesecakeutilities.BaseBroadcastReceiver;
import com.itachi1706.cheesecakeutilities.BuildConfig;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarModel;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.MSLData;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.CalendarAddTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.CalendarLoadTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.MSLTaskSyncTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.RetrieveMSLData;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util.FileCacher;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util.MSLHelper;
import com.itachi1706.cheesecakeutilities.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.fabric.sdk.android.Fabric;

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
    private NotificationManager manager;
    private int notificationId;
    private NotificationCompat.Builder serviceNotification;
    private boolean isSyncing = false;

    public static final String ACTION_SYNC_MSL = "msl-sync-task-svc", CHANNEL_ERROR = "error-messages", INTENT_MESSAGE = "message";

    // Google Stuff
    Calendar client;
    CalendarModel model = new CalendarModel();
    GoogleAccountCredential credential;


    public SyncMSLService() {
        // Required for Firebase JobDispatcher API
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "Starting MSL Sync Job");
        Fabric fabric = new Fabric.Builder(this).kits(new Crashlytics()).debuggable(BuildConfig.DEBUG).build();
        if (!BuildConfig.DEBUG) Fabric.with(fabric);

        parameters = params;
        // Check if network access is available before starting ANYTHING
        if (!ConnectivityHelper.hasInternetConnection(this)) {
            Log.e(TAG, "Network Connection not found, delegating to next job instead");
            cleanup(true);
            return false;
        }

        // Register receiver
        receiver = new MSLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CalendarAsyncTask.BROADCAST_MSL_ASYNC);
        filter.addAction(RetrieveMSLData.BROADCAST_MSL_DATA_SYNC);
        filter.addAction(MSLTaskSyncTask.BROADCAST_MSL_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        sp = PrefHelper.getDefaultSharedPreferences(this);
        manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        assert manager != null;
        if (notificationId != 0 || serviceNotification != null) manager.cancel(notificationId);

        Random random = new Random();
        serviceNotification = new NotificationCompat.Builder(this, "msl-sync-service").setContentTitle("MSL Calendar Sync")
                .setContentText("Starting sync...").setSmallIcon(R.drawable.notification_icon).setProgress(0, 0, true).setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MSLActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK), 0));
        notificationId = random.nextInt(Integer.MAX_VALUE - 10) + 1;

        Intent cancel = new Intent(this, MSLCancelNotification.class);
        cancel.putExtra("id", notificationId);
        PendingIntent pCancel = PendingIntent.getBroadcast(this,  random.nextInt(5000), cancel, PendingIntent.FLAG_CANCEL_CURRENT);
        serviceNotification.addAction(new NotificationCompat.Action(0, "Force Dismiss Notification", pCancel));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setupNotificationChannel(manager, false);
        manager.notify(notificationId, serviceNotification.build());

        // Setup stuff
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccountName(sp.getString(MSL_SP_GOOGLE_OAUTH, null));
        client = new Calendar.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credential).setApplicationName("MSLIntegration/1.0").build();

        handleWork((params.getExtras() == null) ? new Bundle() : params.getExtras(), params.getTag());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "System invoked. Stopping MSL Sync Job");
        if (isSyncing) updateNotification("Sync Interrupted. Not all tasks may have finished\n\nStopped at: " + lastDescription, 0, 0, false);
        cleanup(true);
        return false;
    }

    private String lastDescription = "";

    private void updateNotification(String description, int max, int progress, boolean indeterminate) {
        if (serviceNotification == null) return;
        serviceNotification.setContentText(description).setProgress(max, progress, indeterminate);
        serviceNotification.setStyle(new NotificationCompat.BigTextStyle().bigText(description));
        manager.notify(notificationId, serviceNotification.build());
        lastDescription = description;
    }

    private void cleanup(boolean needReschedule) {
        // Unregister receiver
        if (receiver != null) LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        receiver = null;

        if (serviceNotification != null) {
            serviceNotification.setProgress(0, 0, false).setOngoing(false);
            // Reflection to remove action (this is some next level shit)
            try {
                //Use reflection clean up old actions
                Field f = serviceNotification.getClass().getDeclaredField("mActions");
                f.setAccessible(true);
                f.set(serviceNotification, new ArrayList<NotificationCompat.Action>());
            } catch (NoSuchFieldException e) {
                // no field
            } catch (IllegalAccessException e) {
                // wrong types
            }

            if (sp.getBoolean(MSLActivity.MSL_SP_TOGGLE_NOTIF, false)) manager.cancel(notificationId);
            else manager.notify(notificationId, serviceNotification.build());
            serviceNotification = null;
            notificationId = 0;
        }

        if (notificationId != 0) manager.cancel(notificationId); // If somehow there is a notificationId still

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        if (needReschedule) {
            int minTime = 1800; // 1800
            if (!parameters.isRecurring()) {
                // Reschedules the job (every hour and when you are idle) [Every 0.5-1.5 hours] (1800-3600)
                Job syncJob = dispatcher.newJobBuilder().setService(SyncMSLService.class).setRecurring(true)
                        .setTrigger(Trigger.executionWindow(minTime, 3600)).setReplaceCurrent(true).setConstraints(Constraint.ON_ANY_NETWORK, Constraint.DEVICE_IDLE)
                        .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL).setLifetime(Lifetime.FOREVER).setExtras(new Bundle()).setTag(SyncMSLService.ACTION_SYNC_MSL).build();
                dispatcher.mustSchedule(syncJob);
                Log.i(TAG, "Scheduled a new recurring sync task up to an hour from now");
            }
            Log.i(TAG, "Next recurring job will start on " + DateFormat.getDateTimeInstance().format(System.currentTimeMillis() + (minTime * 1000)) + " (estimated)");
        } else {
            // Make sure no job is ran
            dispatcher.cancel(SyncMSLService.ACTION_SYNC_MSL);
            Log.e(TAG, "Cancelled all recurring jobs as an error has occurred");
        }
    }
    
    private void stopJob(boolean success, boolean rescheduleJob) {
        Log.i(TAG, "Job finished, stopping");
        isSyncing = false;
        String datetime = DateFormat.getDateTimeInstance().format(System.currentTimeMillis());
        if (success) updateNotification("Sync completed at " + datetime, 0, 0, false);
        else updateNotification("Sync failed at " + datetime, 0, 0, false);

        cleanup(rescheduleJob);
        jobFinished(parameters, false);
    }

    protected void handleWork(@NonNull Bundle intent, @NonNull String action) {
        if (hasToken() && hasGoogleOAuth()) {
            if (intent.getBoolean("manual", false)) Toast.makeText(this, "Sync Started", Toast.LENGTH_LONG).show();
            switch (action) {
                case ACTION_SYNC_MSL:
                    // Check that calendar exists
                    updateNotification("Preparing sync... (Checking for existing calendar)", 0, 0, true);
                    isSyncing = true;
                    CalendarLoadTask.run(this, "TASK-SYNC", model, client);
                    break;
                default:
                    Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show();
                    Crashlytics.logException(new IllegalStateException("Reached the wrong place when invoking action " + action));
                    stopJob(false, false);
                    break;
            }
        }

    }

    @SuppressWarnings("ConstantConditions")
    private void parseMSLData(String data) {
        if (data.equalsIgnoreCase("null")) {
            Log.e(TAG, "An error occurred retrieving data. Stopping job");
            Crashlytics.logException(new FileNotFoundException("Data obtained from MSL is null"));
            stopJob(false, true);
            return;
        }
        Log.d(TAG, "JSON Data: " + data);
        updateNotification("Preparing Sync... (Parsing data from MSL)", 0, 0, true);

        Gson gson = new Gson();
        MSLData main = gson.fromJson(data, MSLData.class);
        String mainJson = gson.toJson(main);
        FileCacher c = new FileCacher(this);
        String existing = c.getStringFromFile();
        if (existing == null || existing.compareTo(mainJson) != 0) {
            // Update file
            c.writeToFile(mainJson);
        }

        HashMap<String, String> subjects = new HashMap<>();
        for (MSLData.Subjects s : main.getSubjects()) {
            subjects.put(s.getGuid(), s.getName());
        }

        // Moved exam code up here as tasks MAY need exams
        HashMap<String, MSLData.Exam> examToAdd = new HashMap<>();
        for (MSLData.Exam e : main.getExams()) {
            examToAdd.put(e.getGuid(), e);
        }

        HashMap<String, MSLData.Task> taskToAdd = new HashMap<>();
        HashMap<String, MSLData.Task> taskToRemove = new HashMap<>();
        HashMap<String, MSLData.Task> taskToUpdate = new HashMap<>(); // Old data
        for (MSLData.Task t : main.getTasks()) {
            if (t.getType().equalsIgnoreCase("revision")) {
                // Handle exams
                MSLData.Exam e = examToAdd.get(t.getExam_guid());
                if (e != null) t.setExamString(subjects.get(e.getSubject_guid()) + ": " + e.getModule());
            }
            taskToAdd.put(t.getGuid(), t);
        }
        if (existing != null) {
            MSLData eTask = gson.fromJson(existing, MSLData.class);
            HashMap<String, MSLData.Task> existingTasks = new HashMap<>();
            for (MSLData.Task t : eTask.getTasks()) {
                existingTasks.put(t.getGuid(), t);
            }

            for (Map.Entry<String, MSLData.Task> pair : existingTasks.entrySet()) {
                if (!taskToAdd.containsKey(pair.getKey())) {
                    // Remove from events
                    taskToRemove.put(pair.getKey(), pair.getValue());
                } else {
                    // Check complete match
                    MSLData.Task t1 = taskToAdd.get(pair.getKey());
                    assert t1 != null; // Asserted in if statement
                    taskToAdd.remove(t1.getGuid());
                    if (!MSLHelper.INSTANCE.completeMatch(t1, pair.getValue())) taskToUpdate.put(pair.getKey(), t1); // Update Value
                }
            }
        }

        HashMap<String, MSLData.Exam> examToRemove = new HashMap<>();
        HashMap<String, MSLData.Exam> examToUpdate = new HashMap<>(); // Old data
        if (existing != null) {
            MSLData eTask = gson.fromJson(existing, MSLData.class);
            HashMap<String, MSLData.Exam> existingExam = new HashMap<>();
            for (MSLData.Exam e : eTask.getExams()) {
                existingExam.put(e.getGuid(), e);
            }

            for (Map.Entry<String, MSLData.Exam> pair : existingExam.entrySet()) {
                if (!examToAdd.containsKey(pair.getKey())) {
                    // Remove from events
                    examToRemove.put(pair.getKey(), pair.getValue());
                } else {
                    // Check complete match
                    MSLData.Exam e1 = examToAdd.get(pair.getKey());
                    assert e1 != null; // Asserted in if statement
                    examToAdd.remove(e1.getGuid());
                    if (!MSLHelper.INSTANCE.completeMatch(e1, pair.getValue())) examToUpdate.put(pair.getKey(), e1); // Update value
                }
            }
        }

        Log.d(TAG, "================================================");
        Log.d(TAG, "              Results of Sync Job");
        Log.d(TAG, "================================================");
        Log.d(TAG, "Task To Add: " + taskToAdd.size());
        Log.d(TAG, "Task To Remove: " + taskToRemove.size());
        Log.d(TAG, "Task To Update: " + taskToUpdate.size());
        Log.d(TAG, "Exam To Add: " + examToAdd.size());
        Log.d(TAG, "Exam To Remove: " + examToRemove.size());
        Log.d(TAG, "Exam To Update: " + examToUpdate.size());
        Log.d(TAG, "================================================");

        if (checkIfNoUpdatesNeeded(taskToAdd, taskToRemove, taskToUpdate) && checkIfNoUpdatesNeeded(examToAdd, examToRemove, examToUpdate)) {
            Log.i(TAG, "No update required");
            stopJob(true, true);
            return;
        }

        String newMetaData = System.currentTimeMillis() + "," + (taskToAdd.size() + examToAdd.size()) + ","
                + (taskToUpdate.size() + examToUpdate.size()) + "," + (examToRemove.size() + taskToRemove.size()); // (time,add,remove,update:time,add,remove,update:...)
        Log.i(TAG, "Updating metric data: " + newMetaData);
        String oldString = sp.getString(MSLActivity.MSL_SP_METRIC_HIST, "");
        if (!oldString.isEmpty()) oldString += ":" + newMetaData;
        else oldString = newMetaData;
        sp.edit().putString(MSLActivity.MSL_SP_METRIC_HIST, oldString).apply();
        Log.i(TAG, "Metric Data Updated");
        isSyncing = true;
        MSLTaskSyncTask.run(this, "EXAMTASK", model, client, subjects, taskToAdd, taskToUpdate, taskToRemove, examToAdd, examToUpdate, examToRemove);
    }

    private boolean checkIfNoUpdatesNeeded(HashMap a1, HashMap a2, HashMap a3) {
        return a1.isEmpty() && a2.isEmpty() && a3.isEmpty();
    }

    private void proceedWithSynchronization() {
        updateNotification("Preparing Sync (Retrieving data from MSL)", 0, 0, true);
        isSyncing = true;
        new RetrieveMSLData(LocalBroadcastManager.getInstance(this), ValidationHelper.getSignatureForValidation(this),
                this.getPackageName()).execute(sp.getString(MSLActivity.MSL_SP_ACCESS_TOKEN, "-"));
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager manager, boolean isErrorChannel) {
        Log.d(TAG, "Creating notification channel");
        NotificationChannel c;
        if (isErrorChannel) c = new NotificationChannel(CHANNEL_ERROR, "Service Errors", NotificationManager.IMPORTANCE_DEFAULT);
        else c = new NotificationChannel("msl-sync-service", "MSL Sync Service", NotificationManager.IMPORTANCE_LOW);
        c.setDescription((isErrorChannel)? "Display errors related to app services" : "MSL Sync Service notifications");
        manager.createNotificationChannel(c);
    }

    private void process(String task) {
        Log.i(TAG, "Task Completed: " + task);
        switch (task.toUpperCase()) {
            case "ADD":
                Log.i(TAG, "MSL Task Sync Calendar created. doing synchronization");
                // Since there is no calendar, make sure there are no cache
                FileCacher fc = new FileCacher(this);
                fc.deleteFile();
                isSyncing = false;
                proceedWithSynchronization();
                break;
            case "LOAD-TASK-SYNC":
                String id = sp.getString(MSLActivity.MSL_SP_TASK_CAL_ID, "");
                isSyncing = false;
                if (id.isEmpty() || model.get(id) == null) {
                    Log.w(TAG, "Calendar MSL Task not found, creating calendar");
                    com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
                    calendar.setSummary("MSL Task Calendar Sync");
                    calendar.setDescription("Calendar used by CheesecakeUtilities to synchronize with tasks/exams obtained from MSL\n\nCreated On: "
                            + DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));
                    calendar.setTimeZone("Asia/Singapore");
                    calendar.setLocation("Singapore");
                    updateNotification("Preparing Sync... (Creating new Calendar)", 0, 0, false);
                    new CalendarAddTask(this, model, client, calendar, MSLActivity.MSL_SP_TASK_CAL_ID).execute();
                    return;
                }
                Log.i(TAG, "MSL Task Calendar found. doing synchronization");
                proceedWithSynchronization();
                break;
            case "SYNC-EXAMTASK":
                Log.i(TAG, "Sync completed");
                updateNotification("Sync Complete. Finishing Up...", 0, 0, true);
                stopJob(true, true);
                break;
            default: break;
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
            Intent launchIntent = new Intent(context, MSLActivity.class);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);

            if (intent.getAction().equalsIgnoreCase(CalendarAsyncTask.BROADCAST_MSL_ASYNC)) {
                isSyncing = false;
                if (intent.getBooleanExtra(CalendarAsyncTask.INTENT_EXCEPTION, false)) {
                    Exception e = (Exception) intent.getSerializableExtra(CalendarLoadTask.INTENT_ERROR);
                    manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                    if (manager == null) return;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setupNotificationChannel(manager, true);
                    }
                    String errorMessage = "An error has occurred (" + e.getLocalizedMessage() + ")";
                    NotificationCompat.Builder errorNotification = new NotificationCompat.Builder(context, CHANNEL_ERROR).setContentTitle("MSL Sync Error")
                            .setContentText("An error has occurred (" + e.getLocalizedMessage() + ")").setSmallIcon(R.drawable.notification_icon)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(errorMessage + "\nClick to enter the application to do a manual sync\n\nException: " + e.toString()))
                            .setContentIntent(pendingIntent);
                    if (e instanceof IOException && e.getMessage().equalsIgnoreCase("NetworkError")) {
                        errorNotification.setStyle(new NotificationCompat.BigTextStyle().bigText(errorMessage + "\nRetrying on next sync"));
                        if (!sp.getBoolean(MSLActivity.MSL_SP_TOGGLE_NOTIF, false)) manager.notify(new Random().nextInt(), errorNotification.build());
                        stopJob(false, true);
                    } else {
                        manager.notify(new Random().nextInt(), errorNotification.build());
                        stopJob(false, false);
                    }
                } else {
                    process(intent.getStringExtra(CalendarAsyncTask.INTENT_DATA));
                }
            } else if (intent.getAction().equalsIgnoreCase(RetrieveMSLData.BROADCAST_MSL_DATA_SYNC)) {
                isSyncing = false;
                if (intent.hasExtra(CalendarAsyncTask.INTENT_ERROR)) {
                    // Check if its due to invalid access token
                    if (intent.hasExtra("accesstokeninvalid") && intent.getBooleanExtra("accesstokeninvalid", false)) {
                        Notification errorNotification = new NotificationCompat.Builder(context, CHANNEL_ERROR).setContentTitle("MSL Sync Error (Token Expired)")
                                .setContentText("Your MSL Access Token has expired").setSmallIcon(R.drawable.notification_icon)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Your MSL Access Token has expired!" + "\nClick to enter the application to update your token " +
                                        "and then press the Sync button to continue syncing"))
                                .setContentIntent(pendingIntent).build();
                        manager.notify(new Random().nextInt(), errorNotification);
                        stopJob(false, false);
                        return;
                    }
                    Log.e(TAG, "Error occurred, stopping task now and hope its fixed in the future");
                    Crashlytics.logException(new Exception("SyncMSLSvc: " + (intent.hasExtra(INTENT_MESSAGE) ? intent.getStringExtra(INTENT_MESSAGE) : "Exception occurred")));
                    if (intent.hasExtra(INTENT_MESSAGE)) Log.e(TAG, "Error Message: " + intent.getStringExtra(INTENT_MESSAGE));
                    stopJob(false, true);
                    return;
                }
                // Data received
                parseMSLData(intent.getStringExtra(CalendarAsyncTask.INTENT_DATA));
            } else if (intent.getAction().equalsIgnoreCase(MSLTaskSyncTask.BROADCAST_MSL_NOTIFICATION) && (intent.hasExtra(INTENT_MESSAGE) && intent.hasExtra("max") && intent.hasExtra("progress")))
                    updateNotification(intent.getStringExtra(INTENT_MESSAGE), intent.getIntExtra("max", 0), intent.getIntExtra("progress", 0), false);
        }
    }
}
