package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.itachi1706.appupdater.Util.UpdaterHelper.HTTP_QUERY_TIMEOUT;

/**
 * Created by Kenneth on 14/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks in CheesecakeUtilities
 */
public class RetrieveMSLData extends AsyncTask<String, Void, Void> {

    public static final String BROADCAST_MSL_DATA_SYNC = "com.itachi1706.cheesecakeutilities.MSL_ASYNC_DATA_MSG";
    private static final String TAG = "RetrieveMSLData";

    private LocalBroadcastManager manager;
    private String siganture, packageName;

    public RetrieveMSLData(LocalBroadcastManager manager, String signature, String packageName) {
        this.manager = manager;
        this.siganture = signature;
        this.packageName = packageName;
    }

    @Override
    protected Void doInBackground(String... strings) {
        Intent returnIntent = new Intent(BROADCAST_MSL_DATA_SYNC);
        if (strings.length == 0) {
            returnIntent.putExtra("error", true);
            returnIntent.putExtra("reason", "No auth code");
            manager.sendBroadcast(returnIntent);
            return null; // Nothing to process
        }
        String url = "http://api.itachi1706.com/api/mobile/msl_api.php?authorization=" + strings[0];
        String tmp;
        url += "&sig=" + siganture + "&package=" + packageName;
        HttpURLConnection conn = null;
        try {
            URL urlConn = new URL(url);
            conn = (HttpURLConnection) urlConn.openConnection();
            conn.setConnectTimeout(HTTP_QUERY_TIMEOUT);
            conn.setReadTimeout(HTTP_QUERY_TIMEOUT);
            InputStream in = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }
            in.close();
            tmp = str.toString();

            returnIntent.putExtra("data", tmp);
            manager.sendBroadcast(returnIntent);
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                Log.e(TAG, "FileNotFoundException: " + e.getLocalizedMessage());
                if (conn != null) {
                    // Try and figure out if its a 401
                    try {
                        int status = conn.getResponseCode();
                        Log.e(TAG, "Response Code: " + status);
                        Log.e(TAG, "Response Message: " + conn.getResponseMessage());
                        if (conn.getResponseMessage().contains("Invalid Access Token")) {
                            // Token invalid, inform user
                            returnIntent.putExtra("error", true);
                            returnIntent.putExtra("accesstokeninvalid", true);
                            manager.sendBroadcast(returnIntent);
                            return null;
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } else {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
            e.printStackTrace();
            returnIntent.putExtra("error", true);
            returnIntent.putExtra("message", e.getLocalizedMessage());
            manager.sendBroadcast(returnIntent);
        }

        return null;
    }
}
