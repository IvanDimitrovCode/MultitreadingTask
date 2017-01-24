package com.example.ivandimitrov.mutitreadingtask;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private Timer              mTimer;
    private CountdownIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIndicator = (CountdownIndicator) findViewById(R.id.countdown_icon);
        mIndicator.startRefreshTimer();
    }
}