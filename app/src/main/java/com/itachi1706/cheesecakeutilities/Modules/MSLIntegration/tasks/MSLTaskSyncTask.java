package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.CalendarAsyncTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarModel;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.MSLData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private MSLData main;
    private int totalTasks = 0, currentState = 0;

    public static final String BROADCAST_MSL_NOTIFICATION = "com.itachi1706.cheesecakeutilities.MSL_ASYNC_NOTIFY";

    @SuppressWarnings("unchecked")
    private MSLTaskSyncTask(Context context, CalendarModel model, com.google.api.services.calendar.Calendar client, String action, MSLData mainData, Object... maps) {
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
        subjects = new HashMap<>();
        this.main = mainData;
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

        for (MSLData.Subjects s : main.getSubjects()) {
            subjects.put(s.getGuid(), s.getName());
        }
        updateNotification("");

        // Add Task
        Log.i(TAG, "Adding new tasks");
        for (Map.Entry<String, MSLData.Task> entry : taskAdd.entrySet()) {
            Event e = generateEventObject(entry.getValue());
            updateNotification("Inserting Task: " + e.getSummary());
            Log.d(TAG, "Adding: " + e.getId());
            Event debug = client.events().insert(id, e).execute(); // TODO: Optimize it (maybe go towards queuing?
            Log.d(TAG, "Event ID: " + debug.getId());
            currentState++;
        }
        // Add Exam
        // Modify Task
        Log.i(TAG, "Updating tasks to current values");
        for (Map.Entry<String, MSLData.Task> entry : taskModify.entrySet()) {
            Event e = generateEventObject(entry.getValue());
            updateNotification("Updating Task: " + e.getSummary());
            Log.d(TAG, "Updating: " + e.getId());
            client.events().update(id, e.getId(), e).execute();
            currentState++;
        }
        // Modify Exam
        // Delete Task
        Log.i(TAG, "Deleting tasks");
        for (Map.Entry<String, MSLData.Task> entry : taskDelete.entrySet()) {
            Event e = generateEventObject(entry.getValue());
            updateNotification("Deleting Task: " + e.getSummary());
            Log.d(TAG, "Removing: " + e.getId());
            client.events().delete(id, e.getId());
            currentState++;
        }
        // Delete Exam
        Log.i(TAG, "Sync Complete");
    }

    private Event generateEventObject(MSLData.Task item) {
        String sanitizedId = "mslccl" + item.getSubject_guid() + item.getGuid();
        sanitizedId = sanitizedId.replaceAll("[^A-Za-z0-9]", "");
        Log.d(TAG, "Sanitized ID: " + sanitizedId);
        String title = "[" + item.getProgress() + "%] " + item.getTitle();
        String description = "Subject: " + subjects.get(item.getSubject_guid()) + "\nType: " + item.getType();
        description += "\nTask Status: " + ((item.getCompleted_at() != null && item.getProgress() >= 100) ? "Complete (100%)" : "Incomplete (" + item.getProgress() + "%)");
        description += "\n\nDetail:\n" + ((item.getDetail() != null) ? item.getDetail() : "No detail added to task");
        Event e = new Event().setId(sanitizedId).setSummary(title).setDescription(description);
        EventDateTime dueDate = new EventDateTime().setDate(new DateTime(item.getDue_date()));
        e.setStart(dueDate).setEnd(dueDate);

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
    }

    public static void run(Context context, String action, CalendarModel model, com.google.api.services.calendar.Calendar client, MSLData main, Object... maps) {
        new MSLTaskSyncTask(context, model, client, action, main, maps).execute();
    }

}
