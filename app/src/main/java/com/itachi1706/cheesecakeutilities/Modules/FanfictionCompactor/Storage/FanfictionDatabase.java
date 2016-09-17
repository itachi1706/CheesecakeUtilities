package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects.FanficStories;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Kenneth on 21/4/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Storage in CheesecakeUtilities
 */
public class FanfictionDatabase {
    //Database Version
    @SuppressWarnings("unused")
    private static final int DATABASE_VERSION = 1;

    //Database Name
    @SuppressWarnings("unused")
    private static final String DATABASE_NAME = "stories.db";

    //Tables
    private static final String TABLE_STORIES = "stories";

    private static final String DB_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/stories.db";

    private static SQLiteDatabase db;

    public FanfictionDatabase(){
        File file = new File(DB_FILE_PATH);
        db = SQLiteDatabase.openOrCreateDatabase(file, null);
    }

    public ArrayList<FanficStories> getAllStories() {
        String queryString = "SELECT * FROM " + TABLE_STORIES + ";";
        try {
            Cursor cursor = db.rawQuery(queryString, null);
            ArrayList<FanficStories> result = new ArrayList<>();

            if (cursor.moveToFirst()) {
                do {
                    result.add(new FanficStories(cursor.getInt(0), cursor.getInt(1), cursor.getInt(3), cursor.getString(2), cursor.getString(5)));
                } while (cursor.moveToNext());
            }
            cursor.close();
            return result;
        } catch (SQLiteException e) {
            Log.e("FanficDB", "Error: " + e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }

    public static boolean databaseExists() {
        return new File(DB_FILE_PATH).exists();
    }

    public static String getDbFileFolder() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getDbFilePath() {
        return DB_FILE_PATH;
    }
}
