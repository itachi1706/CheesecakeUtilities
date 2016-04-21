package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects.FanficStories;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Kenneth on 21/4/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Storage in CheesecakeUtilities
 */
public class FanfictionDatabase {
    //Database Version
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "stories.db";

    //Tables
    private static final String TABLE_AUTHORS = "authors";
    private static final String TABLE_BOOKMARKS = "bookmarks";
    private static final String TABLE_CHAPTERS = "chapters";
    private static final String TABLE_SEARCHMARKS = "searchmarks";
    private static final String TABLE_STORIES = "stories";

    //Author Keys
    private static final String KEY_AUTHORS_ID = "id";
    private static final String KEY_AUTHORS_PAGE_ID = "page_id";
    private static final String KEY_AUTHORS_NAME = "name";
    private static final String KEY_AUTHORS_STORY_SITE = "story_site";

    //Bookmarks Keys
    private static final String KEY_BOOKMARKS_ID = "id";
    private static final String KEY_BOOKMARKS_NAME = "name";
    private static final String KEY_BOOKMARKS_CHAPTER_ID = "chapter_id";
    private static final String KEY_BOOKMARKS_STORY_ID = "story_id";
    private static final String KEY_BOOKMARKS_POSITION = "position";

    //Chapter Keys
    private static final String KEY_CHAPTERS_ID = "id";
    private static final String KEY_CHAPTERS_TITLE = "title";
    private static final String KEY_CHAPTERS_NUMBER = "number";
    private static final String KEY_CHAPTERS_CONTENT = "content";
    private static final String KEY_CHAPTERS_PATH = "path";
    private static final String KEY_CHAPTERS_ISREAD = "isread";
    private static final String KEY_CHAPTERS_STORY_ID = "story_id";

    //Searchmarks Keys
    private static final String KEY_SEARCHMARKS_ID = "id";
    private static final String KEY_SEARCHMARKS_NAME = "name";
    private static final String KEY_SEARCHMARKS_PATH = "path";
    private static final String KEY_SEARCHMARKS_QUERY = "query";
    private static final String KEY_SEARCHMARKS_DESC = "desc";

    //Stories Keys
    private static final String KEY_STORIES_ID = "id";
    private static final String KEY_STORIES_PAGE_ID = "page_id";
    private static final String KEY_STORIES_TITLE = "title";
    private static final String KEY_STORIES_AUTHOR_ID = "author_id";
    private static final String KEY_STORIES_WORD_COUNT = "wordCount";
    private static final String KEY_STORIES_CHAPTERS = "chapters";
    private static final String KEY_STORIES_LANGUAGE = "language";
    private static final String KEY_STORIES_UPDATED_DATE = "updatedDate";
    private static final String KEY_STORIES_RATING = "rating";
    private static final String KEY_STORIES_CATEGORY = "category";
    private static final String KEY_STORIES_IS_READ = "isread";
    private static final String KEY_STORIES_CLASSIFICATION = "classification";
    private static final String KEY_STORIES_MISC = "misc";
    private static final String KEY_STORIES_ISPREVIEW = "ispreview";
    private static final String KEY_STORIES_SUMMARY = "summary";
    private static final String KEY_STORIES_TYPE = "type";
    private static final String KEY_STORIES_TAG = "tag";

    private static final String DB_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/stories.db";

    private static SQLiteDatabase db;

    public FanfictionDatabase(){
        File file = new File(DB_FILE_PATH);
        db = SQLiteDatabase.openOrCreateDatabase(file, null);
    }

    public ArrayList<FanficStories> getAllStories(){
        String queryString = "SELECT * FROM " + TABLE_STORIES + ";";
        Cursor cursor = db.rawQuery(queryString, null);
        ArrayList<FanficStories> result = new ArrayList<>();

        if (cursor.moveToFirst()){
            do {
                result.add(new FanficStories(cursor.getInt(0), cursor.getInt(1), cursor.getInt(3), cursor.getString(2), cursor.getString(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public static String getDbFilePath() {
        return DB_FILE_PATH;
    }

    public String getAuthorName(int authorId){
        String queryString = "SELECT * FROM " + TABLE_AUTHORS + " WHERE " + KEY_AUTHORS_ID + " = " + authorId + ";";
        Cursor cursor = db.rawQuery(queryString, null);
        String authorName = "";
        if (cursor.moveToFirst()){
            do{
                authorName = cursor.getString(2);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return authorName;
    }
}
