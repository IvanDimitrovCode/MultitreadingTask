package com.example.ivandimitrov.mutitreadingtask;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements DBCWriter.DBCWriterListener, DBCReader.DBCReaderListener {
    private ArrayList<CNTruthTask> mWorkerThreadList = new ArrayList<>();
    private ArrayList<Animation>   mAnimationList    = new ArrayList<>();
    private TextView                 mRandomJokeView;
    private Button                   mToggleButton;
    private ScheduledExecutorService mScheduleTaskExecutor;
    private Future<?>                mFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAnimationList();
        mRandomJokeView = (TextView) findViewById(R.id.random_joke);
        mToggleButton = (Button) findViewById(R.id.button_toggle);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mToggleButton.getText().equals(getString(R.string.button_toggle_stop))) {
                    mToggleButton.setText(getString(R.string.button_toggle_continue));
                    mFuture.cancel(true);
                } else {
                    mToggleButton.setText(getString(R.string.button_toggle_stop));
                    startDBCReader();
                }
            }
        });
        new DBCWriter(this, this).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startDBCReader();
    }

    @Override
    public void onDBCWriterStart(Handler dBCWriterHandler) {
        CountdownIndicator mIndicator_1 = (CountdownIndicator) findViewById(R.id.countdown_icon_1);
        CountdownIndicator mIndicator_2 = (CountdownIndicator) findViewById(R.id.countdown_icon_2);
        CountdownIndicator mIndicator_3 = (CountdownIndicator) findViewById(R.id.countdown_icon_3);

        mWorkerThreadList.add(new CNTruthTask(mIndicator_1, this, getString(R.string.name_1), dBCWriterHandler));
        mWorkerThreadList.add(new CNTruthTask(mIndicator_2, this, getString(R.string.name_2), dBCWriterHandler));
        mWorkerThreadList.add(new CNTruthTask(mIndicator_3, this, getString(R.string.name_3), dBCWriterHandler));

        for (CNTruthTask task : mWorkerThreadList) {
            task.start();
        }
    }

    private void startDBCReader() {
        Runnable runnable = new DBCReader(this, this);
        mScheduleTaskExecutor = Executors.newSingleThreadScheduledExecutor();
        mFuture = mScheduleTaskExecutor.scheduleAtFixedRate(runnable, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void onDBCWriterStop() {
        for (CNTruthTask task : mWorkerThreadList) {
            task.stopThreadTimer();
        }
    }

    private void initAnimationList() {
        mAnimationList.add(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_fade));
        mAnimationList.add(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_rotate));
        mAnimationList.add(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_scale));
        mAnimationList.add(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_translate_right));
    }

    private Animation getRandomAnimation() {
        Random randomGen = new Random();
        Animation randAnim = mAnimationList.get(randomGen.nextInt(mAnimationList.size()));
        randAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRandomJokeView.setText(mNewJoke);
                mRandomJokeView.setBackgroundColor(getRandomColor());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return randAnim;
    }

    public int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    String mNewJoke;

    @Override
    public void onReadFinish(String newJoke) {
        mNewJoke = newJoke;
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        mRandomJokeView.clearAnimation();
                        mRandomJokeView.startAnimation(getRandomAnimation());

                    }
                }
        );
    }

}