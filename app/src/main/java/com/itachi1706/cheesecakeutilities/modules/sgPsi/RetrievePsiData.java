package com.itachi1706.cheesecakeutilities.modules.sgPsi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.firebase.perf.metrics.AddTrace;
import com.itachi1706.cheesecakeutilities.util.LogHelper;
import com.itachi1706.helperlib.helpers.URLHelper;

import java.io.IOException;

/**
 * Created by Kenneth on 18/2/2018.
 * for com.itachi1706.cheesecakeutilities.modules.SGPsi in CheesecakeUtilities
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
        String url = "https://api.itachi1706.com/api/dbToPSI.php?type=GEN";
        String tmp;
        URLHelper urlHelper = new URLHelper(url);
        try {
            tmp = urlHelper.executeString();

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
