package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        googleSignIn.setEnabled(true); // TODO: Disable button if valid
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
        // TODO: enable sign out if logged in
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.msl_how_to:
                Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show(); // TODO: Implement
                return true;
            case R.id.msl_signout:
                Toast.makeText(this, "Unimplemented", Toast.LENGTH_LONG).show(); // TODO: Implement
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean hasToken() {
        return sp.contains(MSL_SP_ACCESS_TOKEN);
    }

    private void updateToggles() {
        // Only update if OAuth AND access token is found
        syncTask.setEnabled(false);
        syncCal.setEnabled(false);
        if (hasToken()) {
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

    }

    private void toggleTask(boolean isChecked) {
        if (!hasToken()) {
            Toast.makeText(this, "Please enter an Access Token to sync your tasks to Google Calendar", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void toggleCal(boolean isChecked) {
        if (!hasToken()) {
            Toast.makeText(this, "Please enter an Access Token to sync your schedule to Google Calendar", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Will be implemented in a future release", Toast.LENGTH_LONG).show();
        // TODO: Note
    }
}
