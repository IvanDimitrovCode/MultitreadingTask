package com.example.ivandimitrov.mutitreadingtask;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DBCWriter.DBCWriterListener {
    ArrayList<CNTruthTask> workerThreadList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new DBCWriter(this, this).start();
    }

    @Override
    public void onDBCWriterStart(Handler dBCWriterHandler) {
        CountdownIndicator mIndicator_1 = (CountdownIndicator) findViewById(R.id.countdown_icon_1);
        CountdownIndicator mIndicator_2 = (CountdownIndicator) findViewById(R.id.countdown_icon_2);
        CountdownIndicator mIndicator_3 = (CountdownIndicator) findViewById(R.id.countdown_icon_3);

        workerThreadList.add(new CNTruthTask(mIndicator_1, this, getString(R.string.name_1), dBCWriterHandler));
        workerThreadList.add(new CNTruthTask(mIndicator_2, this, getString(R.string.name_2), dBCWriterHandler));
        workerThreadList.add(new CNTruthTask(mIndicator_3, this, getString(R.string.name_3), dBCWriterHandler));

        for (CNTruthTask task : workerThreadList) {
            task.start();
        }
    }

    @Override
    public void onDBCWriterStop() {
        for (CNTruthTask task : workerThreadList) {
            task.stopThreadTimer();
        }
    }
}