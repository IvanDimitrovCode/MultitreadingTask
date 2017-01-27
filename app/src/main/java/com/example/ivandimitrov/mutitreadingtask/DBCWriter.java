package com.example.ivandimitrov.mutitreadingtask;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by Ivan Dimitrov on 1/26/2017.
 */

public class DBCWriter extends Thread {
    public static final int    DATABASE_MAX_LENGTH = 100;
    public static final String SQL_CREATE_ENTRIES  =
            "CREATE TABLE " + FeedReaderContract.FeedEntry.TABLE_NAME + " (" +
                    FeedReaderContract.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.FeedEntry.COLUMN_JOKE + " TEXT);";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedReaderContract.FeedEntry.TABLE_NAME;

    private Handler           mHandler;
    private DBCWriterListener mListener;
    private Context           mContext;

    //SQL
    private SQLiteDatabase     mDataBase;
    private FeedReaderDbHelper mDbHelper;
    private ContentValues      mContentValues;
    private boolean isDatabaseFull = false;
    private boolean isRunning      = false;

    DBCWriter(DBCWriterListener listener, Context context) {
        this.mListener = listener;
        mContext = context;
    }

    @Override
    public void run() {
        Looper.prepare();
        isRunning = true;
        initDB();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = (String) msg.obj;
                if (getDBJokeCount() >= DATABASE_MAX_LENGTH) {
                    isDatabaseFull = true;
                    isRunning = false;
                    mListener.onDBCWriterStop();
                }
                if (!isDatabaseFull) {
                    writeToDB(message);
                }
            }
        };
        if (getDBJokeCount() < DATABASE_MAX_LENGTH) {
            mListener.onDBCWriterStart(mHandler);
        }
        Looper.loop();
    }

    public void stopWriter() {
        Looper.myLooper().quit();
    }

    public long getDBJokeCount() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        long cnt = DatabaseUtils.queryNumEntries(db, FeedReaderContract.FeedEntry.TABLE_NAME);
        return cnt;
    }

    private void initDB() {
        mDbHelper = new FeedReaderDbHelper(mContext);
        mContentValues = new ContentValues();
        mDataBase = mDbHelper.getWritableDatabase();
    }

    private void writeToDB(String joke) {
        mContentValues.put(FeedReaderContract.FeedEntry.COLUMN_JOKE, joke);
        long newRowId = mDataBase.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, mContentValues);
    }

    interface DBCWriterListener {
        void onDBCWriterStart(Handler mHandler);

        void onDBCWriterStop();
    }
}
