package com.itachi1706.cheesecakeutilities.Modules.LyricFinder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.itachi1706.appupdater.Util.URLHelper;
import com.itachi1706.cheesecakeutilities.Util.CommonVariables;
import com.itachi1706.cheesecakeutilities.Util.LogHelper;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by Kenneth on 26/12/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.LyricFinder in CheesecakeUtilities
 */

public class RetrieveLyricTask extends AsyncTask<String, Void, Void> {

    private Handler handler;
    public static final int LYRIC_TASK_COMPLETE = 1112;

    public RetrieveLyricTask(Handler handler) {
        this.handler = handler;
    }

    public static final String TAG = "RetrieveLyric";

    /**
     * Background task, requires 2 minimum
     *
     * @param strings 1st string being title, 2nd string being artist
     * @return null
     */
    @Override
    protected Void doInBackground(String... strings) {
        if (strings.length < 2) {
            LogHelper.e(TAG, "Error retrieving lyrics. Insufficient params");
            Message msg = Message.obtain();
            msg.what = LYRIC_TASK_COMPLETE;
            Bundle bundle = new Bundle();
            bundle.putString("error", "Insufficient parameter passed through");
            bundle.putBoolean("result", false);
            msg.setData(bundle);
            handler.sendMessage(msg);
            return null;
        }
        String artist = strings[1];
        String title = strings[0];
        String tmp = "";
        try {
            String url = CommonVariables.BASE_API_URL + "lyricget.php?title=" +
                    URLEncoder.encode(title, "UTF-8") + "&artist=" + URLEncoder.encode(artist, "UTF-8");
            URLHelper urlHelper = new URLHelper(url);
            tmp = urlHelper.executeString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message msg = Message.obtain();
        msg.what = LYRIC_TASK_COMPLETE;
        Bundle bundle = new Bundle();
        bundle.putString("data", tmp);
        bundle.putBoolean("result", true);
        msg.setData(bundle);
        handler.sendMessage(msg);
        return null;
    }
}
