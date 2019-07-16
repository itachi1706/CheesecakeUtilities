package com.itachi1706.cheesecakeutilities.Modules.SGPsi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.firebase.perf.metrics.AddTrace;
import com.itachi1706.cheesecakeutilities.Util.LogHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.itachi1706.appupdater.Util.UpdaterHelper.HTTP_QUERY_TIMEOUT;

/**
 * Created by Kenneth on 18/2/2018.
 * for com.itachi1706.cheesecakeutilities.Modules.SGPsi in CheesecakeUtilities
 */

public class RetrievePsiData extends AsyncTask<Void, Void, Void> {

    private Handler handler;

    static final int DATA_RESULT = 1212;

    RetrievePsiData(Handler handler) {
        this.handler = handler;
    }

    @Override
    @AddTrace(name = "get_psi_data")
    protected Void doInBackground(Void... voids) {
        String url = "http://api.itachi1706.com/api/dbToPSI.php?type=GEN";
        String tmp;
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
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

            Message msg = Message.obtain();
            msg.what = DATA_RESULT;
            Bundle bundle = new Bundle();
            bundle.putString("data", tmp);
            msg.setData(bundle);
            handler.sendMessage(msg);
        } catch (IOException e) {
            LogHelper.e("RetrievePsiData", "Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
