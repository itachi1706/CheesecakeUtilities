package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarModel;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks.CalendarAddTask;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;

import java.util.Collections;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MSLActivity extends BaseActivity {

    SignInButton googleSignIn;
    SwitchCompat syncTask, syncCal;
    RecyclerView history;
    TextInputEditText accessToken;
    Button saveToken, forceSync;
    TextInputLayout til_accessToken;
    SharedPreferences sp;

    public static final String MSL_SP_ACCESS_TOKEN = "msl_access_token";
    public static final String MSL_SP_GOOGLE_OAUTH = "msl_google_oauth";

    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 0, REQUEST_ACCOUNT_PICKER = 1, REQUEST_AUTHORIZATION = 2;

    // Google OAuth
    GoogleAccountCredential credential;
    Calendar client;
    CalendarModel model = new CalendarModel();
    int numAsyncTasks;

    @Override
    public String getHelpDescription() {
        return "MSL Synchronization with Google Calendar\n\nRequires manual retrieval of MSL Access Token. " +
                "More information of how to do so coming soon™";
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

        // Setup Google Stuff
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccountName(sp.getString(MSL_SP_GOOGLE_OAUTH, null));
        client = new Calendar.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credential).setApplicationName("MSLIntegration/1.0").build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateToggles();
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
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.msl_how_to:
                Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show(); // TODO: Implement
                return true;
            case R.id.msl_signout:
                credential.setSelectedAccountName(null);
                sp.edit().remove(MSL_SP_GOOGLE_OAUTH).apply();
                updateToggles();
                Toast.makeText(this, "Signed out successfully", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
            // TODO: Add Sync Calendar when implemented
            // TODO: Add check for OAuth token as well
            syncTask.setEnabled(true);
        }
    }

    private void btnSave() {
        til_accessToken.setErrorEnabled(false);
        if (accessToken.getText().toString().isEmpty()) {
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
        if (!hasToken()) {
            Toast.makeText(this, "No access token", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void btnLogin() {
        if (checkGooglePlayServicesAvailable()) haveGooglePlayServices();
    }

    private void toggleTask(boolean isChecked) {
        if (!hasToken()) {
            Toast.makeText(this, "Please enter an Access Token to sync your tasks to Google Calendar", Toast.LENGTH_LONG).show();
            return;
        }

        // Test, if toggled, create calendar
        // TODO: Test code to remove
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary("Test Calendar Summary");
        calendar.setTimeZone("Asia/Singapore");

        new CalendarAddTask(this, calendar).execute();
        Toast.makeText(this, "Calendar Inserting Asynchronously", Toast.LENGTH_LONG).show();

    }

    private void toggleCal(boolean isChecked) {
        if (!hasToken()) {
            Toast.makeText(this, "Please enter an Access Token to sync your schedule to Google Calendar", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Will be implemented in a future release", Toast.LENGTH_LONG).show();
        // TODO: Note
    }

    public void update(boolean success, String taskAction) {
        // TODO: Implement if async tasks are true
        switch (taskAction.toUpperCase()) {
            case "ADD":
                Toast.makeText(this, "Calendar Inserted Asynchronously", Toast.LENGTH_LONG).show();
                break;
            default: Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show(); break;
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
            // TODO: Do stuff with calendars
            //AsyncLoadCalendars.run(this);
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
                        // TODO: Do stuff with calendars
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    // Load calendars
                    Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show();
                } else chooseAccount();
        }
    }
}
