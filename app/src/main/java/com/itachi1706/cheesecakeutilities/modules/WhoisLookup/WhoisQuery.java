package com.itachi1706.cheesecakeutilities.modules.WhoisLookup;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.itachi1706.appupdater.Util.URLHelper;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

import java.io.IOException;

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
        String url = "https://api.itachi1706.com/api/whois.php?domain=" + strings[0];
        LogHelper.i("WhoisQuery", "Querying: " + url);
        String tmp;
        URLHelper urlHelper = new URLHelper(url);
        try {
            tmp = urlHelper.executeString();

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
