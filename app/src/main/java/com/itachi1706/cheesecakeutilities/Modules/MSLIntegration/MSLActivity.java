package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.ExportFile;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util.FileCacher;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.Util.LogHelper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by Kenneth on 12/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
public class MSLActivity extends BaseModuleActivity {

    SharedPreferences sp;

    public static final String MSL_SP_ACCESS_TOKEN = "msl_access_token";
    public static final String MSL_SP_GOOGLE_OAUTH = "msl_google_oauth";
    public static final String MSL_SP_TOGGLE_NOTIF = "msl_notification_dismiss";
    public static final String MSL_SP_METRIC_HIST = "msl-metric-history";
    public static final String MSL_SP_TASK_CAL_ID = "msl-cal-task-id";

    public static final int REQUEST_WRITE_FILE = 4;
    private static final String TAG = "MSL-SYNC";

    @Override
    @NotNull
    public String getHelpDescription() {
        return "Note: Utility has been deprecated. Click Why? to find out more\n\n\nMSL Synchronization with Google Calendar\n\nRequires an access token from MSL.\n" +
                "More information of how to do so is available by selecting the \"How to obtain MSL Token\" guide in the menu";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msl);

        Button exportBtn = findViewById(R.id.msl_export_button);
        Button whyBtn = findViewById(R.id.msl_why_button);

        sp = PrefHelper.getDefaultSharedPreferences(this);

        // Set On Click Listeners
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            exportBtn.setEnabled(true);
            exportBtn.setOnClickListener(v -> exportDataPre());
        }

        whyBtn.setOnClickListener(v -> startActivity(new Intent(this, MslWebViewWhyActivity.class)));
    }

    // Export
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void exportDataPre() {
        LogHelper.i(TAG, "Requesting file creation for data export");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/json");
        intent.putExtra(Intent.EXTRA_TITLE, "msl_sync_options.json");
        startActivityForResult(intent, REQUEST_WRITE_FILE);
    }

    private void exportData(Uri uri) {
        LogHelper.i(TAG, "Starting Data Export...");
        ExportFile f = new ExportFile();
        FileCacher fc = new FileCacher(this);
        f.setCache(fc.getStringFromFile());
        f.setHistory(sp.getString(MSL_SP_METRIC_HIST, null));
        f.setNotificationDismiss(sp.getBoolean(MSL_SP_TOGGLE_NOTIF, false));
        f.setCalendarId(sp.getString(MSL_SP_TASK_CAL_ID, null));
        f.setAccessToken(sp.getString(MSL_SP_ACCESS_TOKEN, null));
        Gson gson = new Gson();
        String json = gson.toJson(f);

        // Save to file
        try {
            OutputStream os = getContentResolver().openOutputStream(uri);
            if (os == null) {
                LogHelper.e(TAG, "Data export failed");
                Toast.makeText(this, "Data export failed!", Toast.LENGTH_LONG).show();
                return;
            }
            OutputStreamWriter osw = new OutputStreamWriter(os);
            osw.write(json);
            osw.close();
            LogHelper.i(TAG, "Data export completed!");
            Toast.makeText(this, "Data exported successfully", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting data. Check logs for more info", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    @Deprecated
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_WRITE_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) exportData(uri);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
