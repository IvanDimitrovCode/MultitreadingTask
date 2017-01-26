package com.example.ivandimitrov.mutitreadingtask;

/**
 * Created by Ivan Dimitrov on 1/25/2017.
 */

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class CNTruthTask extends Thread {
    public static final int TIMER_REFRESH_RATE = 20;

    public final static String MY_URL = "http://api.icndb.com/jokes/random";
    private Handler            mMessageHandler;
    private CountdownIndicator mIndicator;
    private Timer              mTimer;
    private Activity           mActivity;
    private String             mThreadName;
    private Handler            mDBCWriterHandler;

    private double mCurrentValue  = 1;
    private int    mTimerInterval = 5000;

    public CNTruthTask(CountdownIndicator indicator, Activity activity, String name, Handler dBCWriterHandler) {
        this.mIndicator = indicator;
        this.mActivity = activity;
        this.mThreadName = name;
        this.mDBCWriterHandler = dBCWriterHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        Random timeGen = new Random();
        setTimerInterval((timeGen.nextInt(9) + 1) * 1000);
        startRefreshTimer();
        mMessageHandler = new Handler();
        Looper.loop();
    }

    public void setTimerInterval(int interval) {
        mTimerInterval = interval;
        startRefreshTimer();
    }

    public void startRefreshTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        double stepsNeeded = mTimerInterval / TIMER_REFRESH_RATE;
        double percent = 100 / stepsNeeded;
        final double step = percent / 100;
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCurrentValue -= step;
                        if (mCurrentValue < 0) {
                            mCurrentValue = 1;
                            onTimerComplete();
                        }
                        mIndicator.setPhase(mCurrentValue);
                    }
                });
            }
        }, 0, TIMER_REFRESH_RATE);
    }

    public void stopThreadTimer() {
        mTimer.cancel();
        Looper.myLooper().quit();
    }

    private void onTimerComplete() {
        mMessageHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    sendMassageToDBCWriter(sendPost());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendMassageToDBCWriter(String message) {
        Message msg = Message.obtain();
        msg.obj = message;
        msg.setTarget(mDBCWriterHandler);
        msg.sendToTarget();
    }

    private String sendPost() throws Exception {
        URL url = new URL(MY_URL);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return readStream(in);
        } finally {
            urlConnection.disconnect();
        }
    }

    private String readStream(InputStream in) {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseMessage(total.toString());
    }

    private String parseMessage(String result) {
        JSONObject value = null;
        try {
            JSONObject jsonObj = new JSONObject(result);
            value = jsonObj.getJSONObject("value");
            return value.getString("joke");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
