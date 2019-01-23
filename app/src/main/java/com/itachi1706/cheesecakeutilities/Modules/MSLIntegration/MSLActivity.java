package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.BaseBroadcastReceiver;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarModel;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.ExportFile;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.MSLData;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.CalendarAddTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.CalendarLoadTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util.FileCacher;
import com.itachi1706.cheesecakeutilities.Objects.DualLineString;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.DualLineStringRecyclerAdapter;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Kenneth on 12/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
public class MSLActivity extends BaseActivity {

    SignInButton googleSignIn;
    SwitchCompat syncTask, syncCal, dismissNotification;
    RecyclerView history;
    TextInputEditText accessToken;
    Button saveToken, forceSync;
    TextInputLayout til_accessToken;
    SharedPreferences sp;

    MSLReceiver receiver;
    DualLineStringRecyclerAdapter adapter;

    public static final String MSL_SP_ACCESS_TOKEN = "msl_access_token";
    public static final String MSL_SP_GOOGLE_OAUTH = "msl_google_oauth";

    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 0, REQUEST_ACCOUNT_PICKER = 1, REQUEST_AUTHORIZATION = 2, REQUEST_READ_FILE = 3, REQUEST_WRITE_FILE = 4;

    private static final String TAG = "MSL-SYNC";

    // Google OAuth
    GoogleAccountCredential credential;
    Calendar client;
    CalendarModel model = new CalendarModel();

    @Override
    public String getHelpDescription() {
        return "MSL Synchronization with Google Calendar\n\nRequires manual retrieval of MSL Access Token. " +
                "More information of how to do so is available by selecting the \"How to obtain MSL Token\" guide in the menu";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msl);

        // Init
        syncTask = findViewById(R.id.msl_task_toggle);
        syncCal = findViewById(R.id.msl_schedule_toggle);
        saveToken = findViewById(R.id.btn_msl_save);
        forceSync = findViewById(R.id.btn_msl_sync);
        history = findViewById(R.id.rv_msl_history);
        accessToken = findViewById(R.id.et_msl_AT);
        googleSignIn = findViewById(R.id.btn_msl_google);
        til_accessToken = findViewById(R.id.til_et_msl_AT);
        dismissNotification = findViewById(R.id.msl_notification_dismiss);
        sp = PrefHelper.getDefaultSharedPreferences(this);

        // Setup Layout
        googleSignIn.setColorScheme(SignInButton.COLOR_LIGHT);
        history.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        history.setLayoutManager(linearLayoutManager);
        history.setItemAnimator(new DefaultItemAnimator());

        String[] loading = new String[1];
        loading[0] = "Loading...";
        history.setAdapter(new StringRecyclerAdapter(loading));

        // Set On Click Listeners
        saveToken.setOnClickListener(v -> btnSave());
        googleSignIn.setOnClickListener(v -> btnLogin());
        forceSync.setOnClickListener(v -> btnSync());
        syncTask.setOnCheckedChangeListener((buttonView, isChecked) -> toggleTask(isChecked));
        syncCal.setOnCheckedChangeListener((buttonView, isChecked) -> toggleCal(isChecked));
        dismissNotification.setOnCheckedChangeListener(((buttonView, isChecked) -> sp.edit().putBoolean("msl_notification_dismiss", isChecked).apply()));

        // Setup Google Stuff
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccountName(sp.getString(MSL_SP_GOOGLE_OAUTH, null));
        client = new Calendar.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credential).setApplicationName("MSLIntegration/1.0").build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateToggles();
        receiver = new MSLReceiver();
        IntentFilter filter = new IntentFilter(CalendarAsyncTask.BROADCAST_MSL_ASYNC);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        updateState(); // Check from preference to autofill toggles

        updateHistory();
    }

    private boolean isUpdatingState = false;

    private void updateState() {
        isUpdatingState = true;
        syncCal.setChecked(sp.getBoolean("msl-toggle-cal", false));
        syncTask.setChecked(sp.getBoolean("msl-toggle-task", false));
        dismissNotification.setChecked(sp.getBoolean("msl_notification_dismiss", false));
        accessToken.setText(sp.getString(MSL_SP_ACCESS_TOKEN, ""));
        isUpdatingState = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_msl, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.msl_signout).setEnabled(hasGoogleOAuth());
        menu.findItem(R.id.msl_export_import).setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.msl_how_to:
                WebView howTo = new WebView(this);
                howTo.getSettings().setBuiltInZoomControls(true);
                howTo.getSettings().setDisplayZoomControls(false);
                howTo.loadUrl("file:///android_asset/msl/mslhelp.html");
                new AlertDialog.Builder(this).setTitle("How to obtain MSL Token").setView(howTo)
                        .setPositiveButton(R.string.dialog_action_positive_close, ((dialog, which) -> dialog.dismiss())).show();
                break;
            case R.id.msl_signout:
                credential.setSelectedAccountName(null);
                sp.edit().remove(MSL_SP_GOOGLE_OAUTH).apply();
                updateToggles();
                Toast.makeText(this, "Signed out successfully", Toast.LENGTH_LONG).show();
                break;
            case R.id.msl_debug_view_task:
            case R.id.msl_debug_view_exam:
                FileCacher c = new FileCacher(this);
                String val = c.getStringFromFile();
                String type = (item.getItemId() == R.id.msl_debug_view_task) ? "Tasks" : "Exams";
                AlertDialog.Builder alert = new AlertDialog.Builder(this).setTitle("View Existing " + type)
                        .setPositiveButton(R.string.dialog_action_positive_close, null);
                if (val == null) {
                    alert.setMessage("No Saved " + type + ". Please sync first").show();
                    break;
                }
                Gson gson = new Gson();
                MSLData data = gson.fromJson(val, MSLData.class);
                if (item.getItemId() == R.id.msl_debug_view_task)
                    parseTasks(data, alert);
                else
                    parseExams(data, alert);
                break;
            case R.id.msl_clear_hist:
                sp.edit().remove("msl-metric-history").apply();
                Toast.makeText(this, "History cleared", Toast.LENGTH_LONG).show();
                updateHistory();
                break;
            case R.id.msl_clear_all:
                new AlertDialog.Builder(this).setTitle("Delete All Data")
                        .setMessage("WARNING YOU ARE ABOUT TO DELETE ALL MSL DATA. THIS ACTION IS NOT REVERSIBLE!\nAre you sure you wish to continue?\n\n" +
                                "After deletion of data, the calendar will not be deleted. If you wish to delete the calendar, head over to calendar.google.com and manually delete the calendar.\n" +
                                "\nIf you wish to clear history instead, select the \"Clear History\" option.").setPositiveButton("YES DELETE!", (dialog, which) -> {
                                    SharedPreferences.Editor spEdit = sp.edit();
                                    spEdit.remove(MSL_SP_ACCESS_TOKEN);
                                    spEdit.remove(MSL_SP_GOOGLE_OAUTH);
                                    spEdit.remove("msl-metric-history");
                                    spEdit.remove("msl_notification_dismiss");
                                    spEdit.remove("msl-cal-task-id");
                                    spEdit.apply();
                                    credential.setSelectedAccountName(null);
                                    FileCacher f = new FileCacher(getApplicationContext());
                                    f.deleteFile();
                                    Toast.makeText(getApplicationContext(), "Cleared all data", Toast.LENGTH_LONG).show();
                                    updateToggles();
                                    updateHistory();
                                }).setNeutralButton("Cancel", null).show();
                break;
            case R.id.msl_export_import:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    new AlertDialog.Builder(this).setTitle("Export/Import Data")
                            .setMessage("Please select if you wish to import or export all MSL Data to and from your device\n\n" +
                                    "Note that sensitive data like your OAuth token and sync options will not be exported/restored")
                            .setPositiveButton("Export", (dialog, which) -> exportDataPre()).setNegativeButton("Import", ((dialog, which) -> importDataPre()))
                            .setNeutralButton("Cancel", null).show();
                } else {
                    new AlertDialog.Builder(this).setTitle("Operation Failed").setMessage("Your version of Android is too low to allow import/export")
                            .setPositiveButton(R.string.dialog_action_positive_close, null).show();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // Import/Export
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void exportDataPre() {
        Log.i(TAG, "Requesting file creation for data export");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/json");
        intent.putExtra(Intent.EXTRA_TITLE, "msl_sync_options.json");
        startActivityForResult(intent, REQUEST_WRITE_FILE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void importDataPre() {
        Log.i(TAG, "Requestion file to read");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/json");
        startActivityForResult(intent, REQUEST_READ_FILE);
    }

    private void importData(Uri uri) {
        StringBuilder data = new StringBuilder();
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) {
                Log.e(TAG, "Data export failed");
                Toast.makeText(this, "Data export failed!", Toast.LENGTH_LONG).show();
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                data.append(line);
            }
            br.close();
            is.close();

            Gson gson = new Gson();
            ExportFile f = gson.fromJson(data.toString(), ExportFile.class);

            FileCacher c = new FileCacher(this);
            if (f.getCache() != null) c.writeToFile(f.getCache());
            SharedPreferences.Editor edit = sp.edit();
            if (f.getHistory() != null) edit.putString("msl-metric-history", f.getHistory());
            if (f.getNotificationDismiss()) edit.putBoolean("msl_notification_dismiss", true);
            if (f.getCalendarId() != null) edit.putString("msl-cal-task-id", f.getCalendarId());
            if (f.getAccessToken() != null) edit.putString("msl_access_token", f.getAccessToken());
            edit.apply();

            updateHistory();
            updateState();
            updateToggles();
            Log.i(TAG, "Data Import completed");
            Toast.makeText(this, "Data has been imported successfully!", Toast.LENGTH_LONG).show();
        } catch (JsonSyntaxException e1) {
            Log.e(TAG, "Invalid JSON File Read");
            Toast.makeText(this, "Data import failed. Please make sure the right file is selected and is not corrupted", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error Importing Data. Check logs for more details", Toast.LENGTH_LONG).show();
        }
    }

    private void exportData(Uri uri) {
        Log.i(TAG, "Starting Data Export...");
        ExportFile f = new ExportFile();
        FileCacher fc = new FileCacher(this);
        f.setCache(fc.getStringFromFile());
        f.setHistory(sp.getString("msl-metric-history", null));
        f.setNotificationDismiss(sp.getBoolean("msl_notification_dismiss", false));
        f.setCalendarId(sp.getString("msl-cal-task-id", null));
        f.setAccessToken(sp.getString("msl_access_token", null));
        Gson gson = new Gson();
        String json = gson.toJson(f);

        // Save to file
        try {
            OutputStream os = getContentResolver().openOutputStream(uri);
            if (os == null) {
                Log.e(TAG, "Data export failed");
                Toast.makeText(this, "Data export failed!", Toast.LENGTH_LONG).show();
                return;
            }
            OutputStreamWriter osw = new OutputStreamWriter(os);
            osw.write(json);
            osw.close();
            Log.i(TAG, "Data export completed!");
            Toast.makeText(this, "Data exported successfully", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting data. Check logs for more info", Toast.LENGTH_LONG).show();
        }

    }

    // Checks
    private boolean hasToken() {
        return sp.contains(MSL_SP_ACCESS_TOKEN);
    }

    private boolean hasGoogleOAuth() {
        return sp.contains(MSL_SP_GOOGLE_OAUTH);
    }

    // On Click Listeners
    private void updateToggles() {
        // Only update if OAuth AND access token is found
        syncTask.setEnabled(false);
        syncCal.setEnabled(false);
        googleSignIn.setEnabled(!hasGoogleOAuth());
        if (hasToken() && hasGoogleOAuth()) {
            syncTask.setEnabled(true);
        } else {
            // Remove settings if any from SP
            if (sp.contains("msl-toggle-cal")) sp.edit().remove("msl-toggle-cal").apply();
            if (sp.contains("msl-toggle-task")) sp.edit().remove("msl-toggle-task").apply();
            updateState();
        }
    }

    private void btnSave() {
        if (isUpdatingState) return;
        til_accessToken.setErrorEnabled(false);
        if (accessToken.getText() == null || accessToken.getText().toString().isEmpty()) {
            til_accessToken.setError("Please enter an access token");
            til_accessToken.setErrorEnabled(true);
            if (hasToken()) sp.edit().remove(MSL_SP_ACCESS_TOKEN).apply();
            updateToggles();
            return;
        }

        sp.edit().putString(MSL_SP_ACCESS_TOKEN, accessToken.getText().toString().trim()).apply();
        updateToggles();
        Toast.makeText(this, "Saved token", Toast.LENGTH_LONG).show();
    }

    private void btnSync() {
        if (isUpdatingState) return;
        if (!hasToken()) {
            Toast.makeText(this, "No access token", Toast.LENGTH_LONG).show();
            return;
        }
        if (!hasGoogleOAuth()) {
            Toast.makeText(this, "Please login to your Google Account", Toast.LENGTH_LONG).show();
            return;
        }
        // Check if sync is enabled, otherwise dont sync
        if (!sp.getBoolean("msl-toggle-task", false) && !sp.getBoolean("msl-toggle-cal", false)) {
            Toast.makeText(this, "Nothing is enabled to be synced. Enable a sync option to continue", Toast.LENGTH_LONG).show();
            return;
        }

        if (sp.getBoolean("msl-toggle-task", false)) {
            Bundle manualJob = new Bundle();
            manualJob.putBoolean("manual", true);
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
            dispatcher.cancel(SyncMSLService.ACTION_SYNC_MSL);
            Job syncJob = dispatcher.newJobBuilder().setService(SyncMSLService.class).setRecurring(false)
                    .setTrigger(Trigger.NOW).setReplaceCurrent(true)
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL).setExtras(manualJob).setTag(SyncMSLService.ACTION_SYNC_MSL).build();
            dispatcher.mustSchedule(syncJob);
            Toast.makeText(this, "Scheduled a task sync job", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Scheduled a manual sync job for tasks");
        }
    }

    private void btnLogin() {
        if (checkGooglePlayServicesAvailable()) haveGooglePlayServices();
    }

    private void toggleTask(boolean isChecked) {
        if (isUpdatingState) return;
        if (!hasToken()) {
            Toast.makeText(this, "Please enter an Access Token to sync your tasks to Google Calendar", Toast.LENGTH_LONG).show();
            return;
        }

        sp.edit().putBoolean("msl-toggle-task", isChecked).apply();

        // if enabled, check that calendar exists, otherwise create it
        Log.d(TAG, "toggleTask(): " + isChecked);
        if (isChecked) {
            CalendarLoadTask.run(this,"TASK", model, client);
        }
    }

    private void toggleCal(boolean isChecked) {
        if (isUpdatingState) return;
        if (!hasToken()) {
            Toast.makeText(this, "Please enter an Access Token to sync your schedule to Google Calendar", Toast.LENGTH_LONG).show();
            return;
        }
        sp.edit().putBoolean("msl-toggle-cal", isChecked).apply();
        Toast.makeText(this, "Will be implemented in a future release", Toast.LENGTH_LONG).show();
        // TODO: Note
    }

    public void update(boolean success, String taskAction) {
        if (!success) {
            Toast.makeText(this, "Something went wrong, try again later", Toast.LENGTH_LONG).show();
            return;
        }
        Log.i(TAG, "Task Completed: " + taskAction);
        switch (taskAction.toUpperCase()) {
            case "ADD":
                Toast.makeText(this, "Calendar Inserted Asynchronously", Toast.LENGTH_LONG).show();
                btnSync();
                break;
            case "LOAD-TASK":
                String id = sp.getString("msl-cal-task-id", "");
                if (id.isEmpty() || model.get(id) == null) {
                    Log.w(TAG, "Calendar MSL Task not found, creating calendar");
                    com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
                    calendar.setSummary("MSL Task Calendar Sync");
                    calendar.setDescription("Calendar used by CheesecakeUtilities to synchronize with tasks/exams obtained from MSL\n\nCreated On: "
                            + DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));
                    calendar.setTimeZone("Asia/Singapore");
                    calendar.setLocation("Singapore");
                    new CalendarAddTask(this, model, client, calendar, "msl-cal-task-id").execute();
                    Toast.makeText(this, "Creating calendar for tasks sync", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.i(TAG, "MSL Task Calendar found. doing synchronization");
                btnSync();
                break;
            case "SYNC-EXAMTASK": updateHistory(); break;
            case "LOAD":
            case "LOAD-TASK-SYNC": break; // Dont do anything
            default: Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show(); break;
        }
    }


    private void updateHistory() {
        Log.i(TAG, "Updating Metric History...");
        String metrics = sp.getString("msl-metric-history", "");
        if (metrics.isEmpty()) {
            displayEmptyHistory();
            return;
        }

        // Parse
        List<DualLineString> metricData = new ArrayList<>();
        String[] metric = metrics.split(":");
        for (String m : metric) {
            String[] data = m.split(",");
            if (data.length != 4) continue; // Not valid data, skip
            String metricMsg = "<font color=\"green\">" + data[1] + " Added</font> / <font color=\"olive\">" + data[2] + " Updated</font> / <font color=\"red\">" + data[3] + " Removed</font>";
            // Data - Date, Add, Update, Remove
            metricData.add(new DualLineString(DateFormat.getDateTimeInstance().format(new Date(Long.parseLong(data[0]))), metricMsg));
        }

        Collections.reverse(metricData);
        if (adapter == null) {
            adapter = new DualLineStringRecyclerAdapter(metricData);
            adapter.setHtmlFormat(true);
            history.setAdapter(adapter);
        } else {
            adapter.update(metricData);
            adapter.notifyDataSetChanged();
        }
    }

    private void displayEmptyHistory() {
        String[] noHist = new String[1];
        noHist[0] = "No History";
        history.setAdapter(new StringRecyclerAdapter(noHist));
        adapter = null;
    }

    // MSL Data Manipulation
    @SuppressWarnings("ConstantConditions")
    private void parseTasks(MSLData data, AlertDialog.Builder builder) {
        HashMap<String, String> subjects = new HashMap<>();
        for (MSLData.Subjects s : data.getSubjects()) {
            subjects.put(s.getGuid(), s.getName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("TASKS\n");
        int i = 1;
        for (MSLData.Task t : data.getTasks()) {
            sb.append(i).append(") ").append("Title: ").append(t.getTitle()).append("\n")
                    .append("Detail: ").append(t.getDetail()).append("\n")
                    .append("Subject: ").append((subjects.containsKey(t.getSubject_guid())) ? subjects.get(t.getSubject_guid()) : "Unknown Subject").append("\n")
                    .append("Type: ").append(t.getType()).append("\n").append("Due: ").append(t.getDue_date())
                    .append("\nProgress: ").append(t.getProgress()).append("\nCompleted: ").append((t.getCompleted_at() == null) ? "No" : "Yes (" + t.getCompleted_at() + ")").append("\n\n");
            i++;
        }

        builder.setMessage(sb.toString()).show();
    }

    @SuppressWarnings("ConstantConditions")
    private void parseExams(MSLData data, AlertDialog.Builder builder) {
        HashMap<String, String> subjects = new HashMap<>();
        for (MSLData.Subjects s : data.getSubjects()) {
            subjects.put(s.getGuid(), s.getName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Exams\n");
        int i = 1;
        for (MSLData.Exam e : data.getExams()) {
            sb.append(i).append(") ").append("Module: ").append(e.getModule()).append("\n")
                    .append("Subject: ").append((subjects.containsKey(e.getSubject_guid())) ? subjects.get(e.getSubject_guid()) : "Unknown Subject").append("\n")
                    .append("Duration: ").append(e.getDuration()).append("\n").append("Is Resit: ").append(e.isResit())
                    .append("\nSeat: ").append((e.getSeat() == null) ? "-" : e.getSeat()).append("\nRoom: ").append((e.getRoom() == null) ? "-" : e.getRoom()).append("\n\n");
            i++;
        }

        builder.setMessage(sb.toString()).show();
    }

    // Receivers
    private class MSLReceiver extends BaseBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            if (intent.getBooleanExtra("exception", false)) {
                Exception e = (Exception) intent.getSerializableExtra("error");
                if (e instanceof GooglePlayServicesAvailabilityIOException) {
                    showGPSError(intent.getIntExtra("data", 0));
                } else if (e instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(intent.getParcelableExtra("data"), REQUEST_AUTHORIZATION);
                } else if (e instanceof IOException) {
                    Utils.logAndShow(MSLActivity.this, "GCal-Async", e);
                }
            } else {
                update(intent.getBooleanExtra("success", true), intent.getStringExtra("data"));
            }
        }
    }

    // GPS Stuff
    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (connectionStatusCode != ConnectionResult.SUCCESS && GoogleApiAvailability.getInstance().isUserResolvableError(connectionStatusCode)) {
            showGPSError(connectionStatusCode);
            return false;
        }
        return true;
    }

    public void showGPSError(final int connectionStatusCode) {
        runOnUiThread(() -> GoogleApiAvailability.getInstance().getErrorDialog(this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES).show());
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private void haveGooglePlayServices() {
        // check if there is already an account selected
        if (credential.getSelectedAccountName() == null) {
            // ask user to choose account
            chooseAccount();
        } else {
            // load calendars
            CalendarLoadTask.run(this, "", model, client);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) haveGooglePlayServices();
                else checkGooglePlayServicesAvailable();
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        sp.edit().putString(MSL_SP_GOOGLE_OAUTH, accountName).apply();
                        CalendarLoadTask.run(this, "", model, client);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    // Load calendars
                    CalendarLoadTask.run(this, "", model, client);
                    Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show();
                } else chooseAccount();
                break;
            case REQUEST_WRITE_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    if (uri == null) break;
                    Log.i(TAG, "WRITE Uri: " + uri.toString());
                    exportData(uri);
                }
                break;
            case REQUEST_READ_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    if (uri == null) break;
                    Log.i(TAG, "READ Uri: " + uri.toString());
                    importData(uri);
                }
                break;
        }
    }
}
