package com.example.ivandimitrov.mutitreadingtask;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private CountdownIndicator mIndicator_1;
    private CountdownIndicator mIndicator_2;
    private CountdownIndicator mIndicator_3;
    private Handler            mMessageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Random timeGen = new Random();

        new CNTruthTask().start();

        mIndicator_1 = (CountdownIndicator) findViewById(R.id.countdown_icon_1);
        mIndicator_2 = (CountdownIndicator) findViewById(R.id.countdown_icon_2);
        mIndicator_3 = (CountdownIndicator) findViewById(R.id.countdown_icon_3);

        mIndicator_1.setTimerInterval((timeGen.nextInt(9) + 1) * 1000);
        mIndicator_2.setTimerInterval((timeGen.nextInt(9) + 1) * 1000);
        mIndicator_3.setTimerInterval((timeGen.nextInt(9) + 1) * 1000);

        mIndicator_1.startRefreshTimer();
        mIndicator_2.startRefreshTimer();
        mIndicator_3.startRefreshTimer();

        if (mMessageHandler != null) {
            mMessageHandler.sendEmptyMessage(5);
        }
//        try {
//            Thread.sleep(5000);
//            mMessageHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d("LOL", "Execute");
//                }
//            }, 400);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    void setMessageHandler(Handler handler) {
        mMessageHandler = handler;
    }


}