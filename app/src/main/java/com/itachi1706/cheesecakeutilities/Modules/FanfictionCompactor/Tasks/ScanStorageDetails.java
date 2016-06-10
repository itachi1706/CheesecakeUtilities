package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Tasks;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.FileHelper;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Storage.FanfictionDatabase;

import java.io.File;

/**
 * Created by Kenneth on 8/6/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Tasks in CheesecakeUtilities
 */
public class ScanStorageDetails extends AsyncTask<Void, Void, Void> {

    private Handler handler;

    public static final int SCAN_STORAGE_RESULT = 1111;

    public ScanStorageDetails(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected Void doInBackground(Void... params) {
        File file = FileHelper.getDefaultFolder();
        long totalSize = FileHelper.getFileSize(file);

        FanfictionDatabase db = new FanfictionDatabase();
        int storageSize = db.getAllStories().size();
        int storyFolderCount = FileHelper.getStoryFolderCount(file);

        Message msg = Message.obtain();
        msg.what = SCAN_STORAGE_RESULT;
        Bundle bundle = new Bundle();
        bundle.putLong("filesize", totalSize);
        bundle.putInt("dbcount", storageSize);
        bundle.putInt("filecount", storyFolderCount);
        msg.setData(bundle);
        handler.sendMessage(msg);
        return null;
    }
}
