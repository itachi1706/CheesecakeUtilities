package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.CalendarAsyncTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarModel;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.MSLData;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Created by Kenneth on 20/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks in CheesecakeUtilities
 */
public class MSLTaskSyncTask extends CalendarAsyncTask {

    private String action;
    private static String TAG = "MSL-SYNC-ASYNC";

    private HashMap<String, MSLData.Task> taskAdd, taskModify, taskDelete;
    private HashMap<String, MSLData.Exam> examAdd, examModify, examDelete;
    private HashMap<String, String> subjects;
    private int totalTasks = 0, currentState = 0;

    public static final String BROADCAST_MSL_NOTIFICATION = "com.itachi1706.cheesecakeutilities.MSL_ASYNC_NOTIFY";

    @SuppressWarnings("unchecked")
    private MSLTaskSyncTask(Context context, CalendarModel model, com.google.api.services.calendar.Calendar client, String action, HashMap<String, String> subjects, Object... maps) {
        super(context, model, client);
        this.action = (action.isEmpty()) ? "" : action;

        // get all hashmaps
        if (maps.length != 6) throw new ArrayIndexOutOfBoundsException("Expected 6 hashmaps, found " + maps.length);

        taskAdd = (HashMap<String, MSLData.Task>) maps[0];
        taskModify = (HashMap<String, MSLData.Task>) maps[1];
        taskDelete = (HashMap<String, MSLData.Task>) maps[2];
        examAdd = (HashMap<String, MSLData.Exam>) maps[3];
        examModify = (HashMap<String, MSLData.Exam>) maps[4];
        examDelete = (HashMap<String, MSLData.Exam>) maps[5];
        this.subjects = subjects;
    }

    @Override
    public String getTaskAction() {
        if (!action.isEmpty()) return "SYNC-" + action;
        return "SYNC";
    }

    @Override
    protected void doInBackground() throws IOException {
        Log.d(TAG, "Syncing calendars for " + action + "...");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String id = sp.getString("msl-cal-task-id", "");
        if (id.isEmpty()) throw new IllegalStateException("GCal ID is missing");
        totalTasks = taskAdd.size() + taskModify.size() + taskDelete.size() + examDelete.size() + examModify.size() + examAdd.size();

        updateNotification("");

        // TODO: Optimize all these (perhaps through queuing of task?)
        // Add Task
        BatchRequest taskBatchRequest = client.batch();
        Log.i(TAG, "Adding new tasks");
        for (Map.Entry<String, MSLData.Task> entry : taskAdd.entrySet()) {
            Event e = generateEventObject(entry.getValue());
            updateNotification("Processing Task: " + e.getSummary());
            Log.d(TAG, "Adding: " + e.getId());
            client.events().insert(id, e).queue(taskBatchRequest, GoogleJsonErrorContainer.class, new JsonBatchCallback<Event>() {
                @Override
                public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
                    Log.e(TAG, "GoogleJsonError occurred: " + e.getMessage());
                }

                @Override
                public void onSuccess(Event event, HttpHeaders responseHeaders) {
                    Log.d(TAG, "Task Event " + event.getId() + " added");
                }
            });
            currentState++;
        }
        updateNotification("Bulk inserting task insertions to calendar");
        taskBatchRequest.execute();
        // Add Exam
        BatchRequest examBatchRequest = client.batch();
        Log.i(TAG, "Adding new exams");
        for (Map.Entry<String, MSLData.Exam> entry : examAdd.entrySet()) {
            Event e = generateEventObject(entry.getValue());
            updateNotification("Processing Exam: " + e.getSummary());
            Log.d(TAG, "Adding: " + e.getId());
            client.events().insert(id, e).queue(examBatchRequest, GoogleJsonErrorContainer.class, new JsonBatchCallback<Event>() {
                @Override
                public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
                    Log.e(TAG, "GoogleJsonError occurred: " + e.getMessage());
                }

                @Override
                public void onSuccess(Event event, HttpHeaders responseHeaders) {
                    Log.d(TAG, "Exam Event " + event.getId() + " added");
                }
            });
            currentState++;
        }
        updateNotification("Bulk sending exam insertions to calendar");
        examBatchRequest.execute();
        // Modify Task
        Log.i(TAG, "Updating tasks to current values");
        for (Map.Entry<String, MSLData.Task> entry : taskModify.entrySet()) {
            Event e = generateEventObject(entry.getValue());
            updateNotification("Updating Task: " + e.getSummary());
            e.setSequence(client.events().get(id, e.getId()).execute().getSequence() + 1);
            Log.d(TAG, "Updating " + e.getId() + " to Sequence " + e.getSequence());
            client.events().update(id, e.getId(), e).execute();
            currentState++;
        }
        // Modify Exam
        Log.i(TAG, "Updating exams to current values");
        for (Map.Entry<String, MSLData.Exam> entry : examModify.entrySet()) {
            Event e = generateEventObject(entry.getValue());
            updateNotification("Updating Exam: " + e.getSummary());
            e.setSequence(client.events().get(id, e.getId()).execute().getSequence() + 1);
            Log.d(TAG, "Updating " + e.getId() + " to Sequence " + e.getSequence());
            client.events().update(id, e.getId(), e).execute();
            currentState++;
        }
        // Delete Task
        Log.i(TAG, "Deleting tasks");
        for (Map.Entry<String, MSLData.Task> entry : taskDelete.entrySet()) {
            Event e = generateEventObject(entry.getValue());
            updateNotification("Deleting Task: " + e.getSummary());
            Log.d(TAG, "Removing: " + e.getId());
            client.events().delete(id, e.getId()).execute();
            currentState++;
        }
        // Delete Exam
        Log.i(TAG, "Deleting exams");
        for (Map.Entry<String, MSLData.Exam> entry : examDelete.entrySet()) {
            Event e = generateEventObject(entry.getValue());
            updateNotification("Deleting Exam: " + e.getSummary());
            Log.d(TAG, "Removing: " + e.getId());
            client.events().delete(id, e.getId()).execute();
            currentState++;
        }

        Log.i(TAG, "Sync Complete");
    }

    private Event generateEventObject(MSLData.Task item) {
        String sanitizedId = "mslccl" + item.getSubject_guid() + item.getGuid();
        sanitizedId = sanitizedId.replaceAll("[^A-Za-z0-9]", "");
        String title = "[" + item.getProgress() + "%] " + item.getTitle();
        String description = "Subject: " + subjects.get(item.getSubject_guid()) + "\nType: " + StringUtils.capitalize(item.getType());
        description += "\nTask Status: " + ((item.getCompleted_at() != null && item.getProgress() >= 100) ? "Complete (100%)" : "Incomplete (" + item.getProgress() + "%)");
        if (item.getExamString() != null) description += "\nExam: " + item.getExamString();
        description += "\n\nDetail:\n" + ((item.getDetail() != null && !item.getDetail().isEmpty()) ? item.getDetail() : "No detail added to task");
        Event e = new Event().setId(sanitizedId).setSummary(title).setDescription(description);
        EventDateTime dueDate = new EventDateTime().setDate(new DateTime(item.getDue_date()));
        e.setStart(dueDate).setEnd(dueDate);

        // TODO: Handle Reminders
        return e;
    }

    private Event generateEventObject(MSLData.Exam item) {
        String sanitizedId = "mslccl" + item.getSubject_guid() + item.getGuid();
        sanitizedId = sanitizedId.replaceAll("[^A-Za-z0-9]", "");

        String module = (item.getModule() == null) ? "Exam" : item.getModule();

        String title = subjects.get(item.getSubject_guid()) + " - " + module;
        String description = "Subject: " + subjects.get(item.getSubject_guid()) + "\nModule: " + module;
        description += "\nResit: " +  item.isResit();
        if (item.getSeat() != null) description += "\nSeat: " + item.getSeat();
        if (item.getRoom() != null) description += "\nExam Venue: " + item.getRoom();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy hh:mm aaa z", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new DateTime(item.getDate()).getValue());
        description += "\nExam Starts: " + sdf.format(cal.getTime());
        EventDateTime startTime = new EventDateTime().setDateTime(new DateTime(cal.getTimeInMillis()));
        cal.add(Calendar.MINUTE, item.getDuration());
        description += "\nExam Ends: " + sdf.format(cal.getTime());
        EventDateTime endTime = new EventDateTime().setDateTime(new DateTime(cal.getTimeInMillis()));
        description += "\nDuration: " + item.getDuration() + " minutes";

        Event e = new Event().setId(sanitizedId).setSummary(title).setDescription(description);
        e.setStart(startTime).setEnd(endTime);

        // TODO: Handle Reminders
        return e;
    }


    private void updateNotification(String extraMessage) {
        String message = "Syncing " + (currentState + 1) + " of " + totalTasks;
        if (!extraMessage.isEmpty()) message += " (" + extraMessage + ")";
        updateNotification(currentState + 1, message, totalTasks);
    }

    private void updateNotification(int progress, String message, int max) {
        Intent i = new Intent(BROADCAST_MSL_NOTIFICATION);
        i.putExtra("progress", progress);
        i.putExtra("message", message);
        i.putExtra("max", max);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    public static void run(Context context, String action, CalendarModel model, com.google.api.services.calendar.Calendar client, HashMap<String, String> subjects, Object... maps) {
        new MSLTaskSyncTask(context, model, client, action, subjects, maps).execute();
    }

}
