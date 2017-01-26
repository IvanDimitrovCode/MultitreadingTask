package com.example.ivandimitrov.mutitreadingtask;

import android.app.Activity;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.Random;

/**
 * Created by Ivan Dimitrov on 1/26/2017.
 */

public class DBCReader implements Runnable {
    private FeedReaderDbHelper mDbHelper;
    private Activity           mActivity;
    private SQLiteDatabase     mDataBase;
    private DBCReaderListener  mListener;
    private Random mRandomGenerator = new Random();

    public DBCReader(Activity activity, DBCReaderListener listener) {
        this.mActivity = activity;
        this.mListener = listener;
        mDbHelper = new FeedReaderDbHelper(mActivity);
        mDataBase = mDbHelper.getReadableDatabase();
    }

    @Override
    public void run() {
        try {
            int currentDBCSize = getDBJokeCount();
            if (currentDBCSize > 0) {
                String selectQuery = "SELECT  * FROM " + FeedReaderContract.FeedEntry.TABLE_NAME + " WHERE "
                        + FeedReaderContract.FeedEntry._ID + " = " + (mRandomGenerator.nextInt((currentDBCSize - 1)) + 1);
                Cursor c = mDataBase.rawQuery(selectQuery, null);
                if (c != null) {
                    c.moveToFirst();
                    String result = c.getString(c.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_JOKE));
                    mListener.onReadFinish(result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getDBJokeCount() {
        int cnt = (int) DatabaseUtils.queryNumEntries(mDataBase, FeedReaderContract.FeedEntry.TABLE_NAME);
        return cnt;
    }

    interface DBCReaderListener {
        void onReadFinish(String newJoke);
    }
}
