package com.itachi1706.cheesecakeutilities.Modules.WhoisLookup;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.itachi1706.cheesecakeutilities.Util.LogHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.itachi1706.appupdater.Util.UpdaterHelper.HTTP_QUERY_TIMEOUT;

/**
 * Created by Kenneth on 27/3/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.WhoisLookup in CheesecakeUtilities
 */
public class WhoisQuery extends AsyncTask<String, Void, Void> {

    public static final int WHOIS_RESULT = 13;

    private Handler handler;

    public WhoisQuery(Handler h) {
        this.handler = h;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String url = "http://api.itachi1706.com/api/whois.php?domain=" + strings[0];
        LogHelper.i("WhoisQuery", "Querying: " + url);
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
            msg.what = WHOIS_RESULT;
            msg.obj = tmp;
            handler.sendMessage(msg);
        } catch (IOException e) {
            LogHelper.e("WhoisQuery", "Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
