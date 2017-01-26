package com.example.ivandimitrov.mutitreadingtask;

/**
 * Created by Ivan Dimitrov on 1/25/2017.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


public class CNTruthTask extends Thread {
    public final static String MY_URL = "http://api.icndb.com/jokes/random";
    Handler mMessageHandler;
    String  mAPIMessage;

    public CNTruthTask() {
    }

    public Handler getHandler() {
        return mMessageHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        mMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                String cNJoke = msg.getData().getString("CNJoke");
                Log.d("MESSAGE RECEIVED", "Time is up");
            }
        };
        try {
            sendPost();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Looper.loop();
    }

    private void sendPost() throws Exception {
        URL url = new URL(MY_URL);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            readStream(in);
        } finally {
            urlConnection.disconnect();
        }
    }

    private void readStream(InputStream in) {
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
        mAPIMessage = total.toString();
    }

    private String parseMessage(String result) {
        JSONObject value = null;
        try {
            JSONObject jsonObj = new JSONObject(result);
            value = jsonObj.getJSONObject("value");
            Log.d("JSON", value.getString("joke"));
            return value.getString("joke");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
