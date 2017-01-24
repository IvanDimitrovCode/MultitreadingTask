package com.example.ivandimitrov.mutitreadingtask;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ivan Dimitrov on 1/23/2017.
 */

/**
 * Circular countdown indicator. The indicator is a filled arc which starts as a full circle ({@code
 * 360} degrees) and shrinks to {@code 0} degrees the less time is remaining.
 *
 * @author klyubin@google.com (Alex Klyubin)
 */
public class CountdownIndicator extends View {
    public static final int TIMER_REFRESH_RATE = 20;

    public static final int DRAW_CLOCKWISE         = 101;
    public static final int DRAW_COUNTER_CLOCKWISE = 102;

    private final Paint mRemainingSectorPaint;
    private final Paint mBorderPaint;
    private static final int DEFAULT_COLOR = 0xff3060c0;
    private float mStartDegree;
    private int   mFrom_color;
    private int   mTo_color;
    private int mDrawDirection = DRAW_CLOCKWISE;
    public  int mTimerInterval = 5000;

    private Timer mTimer;

    /**
     * Countdown phase starting with {@code 1} when a full cycle is remaining and shrinking to
     * {@code 0} the closer the countdown is to zero.
     */
    private double mPhase;

    public CountdownIndicator(Context context) {
        this(context, null);
        Log.d("DEGREE", "NULL");
    }

    public CountdownIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources.Theme theme = context.getTheme();
        TypedArray appearance = theme.obtainStyledAttributes(
                attrs, R.styleable.CountdownIndicator, 0, 0);

        if (appearance != null) {
            int n = appearance.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = appearance.getIndex(i);
                switch (attr) {
                    case R.styleable.CountdownIndicator_from_color:
                        mFrom_color = appearance.getColor(attr, DEFAULT_COLOR);
                        break;
                    case R.styleable.CountdownIndicator_to_color:
                        mTo_color = appearance.getColor(attr, -1);
                        break;
                    case R.styleable.CountdownIndicator_start_degree:
                        mStartDegree = appearance.getInteger(attr, 0);
                        //CHECK FOR INCORRECT INPUT
                        if (mStartDegree > 360) {
                            mStartDegree = 360;
                        } else if (mStartDegree < 0) {
                            mStartDegree = 0;
                        }
                        break;
                    case R.styleable.CountdownIndicator_draw_direction:
                        mDrawDirection = appearance.getInteger(attr, 0);
                        break;
                }
            }
        }

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStrokeWidth(0); // hairline
        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setColor(mFrom_color);
        mRemainingSectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRemainingSectorPaint.setColor(mBorderPaint.getColor());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float remainingSectorSweepAngle = (float) (mPhase * 360);
        float remainingSectorStartAngle;

        if (mDrawDirection == DRAW_CLOCKWISE) {
            remainingSectorStartAngle = mStartDegree - remainingSectorSweepAngle;
        } else {
            remainingSectorStartAngle = mStartDegree;
        }

        RectF drawingRect = new RectF(1, 1, getWidth() - 1, getHeight() - 1);

        if (!(mTo_color == -1)) {
            colorTransition();
        }

        if (remainingSectorStartAngle < 360) {
            canvas.drawArc(
                    drawingRect,
                    remainingSectorStartAngle,
                    remainingSectorSweepAngle,
                    true,
                    mRemainingSectorPaint);
        } else {
            canvas.drawOval(drawingRect, mRemainingSectorPaint);
        }
    }

    public void setDirection(int direction) {
        if (direction == DRAW_CLOCKWISE) {
            mDrawDirection = DRAW_CLOCKWISE;
        } else {
            mDrawDirection = DRAW_COUNTER_CLOCKWISE;
        }
    }

    public void setStartDegree(int degree) {
        if (degree > 360 || degree < 0) {
            degree %= 360;
        }
        mStartDegree = degree;
    }

    public void setColor(int from_color, int to_color, boolean isSingle) {
        if (isSingle) {
            mBorderPaint.setColor(from_color);
            mRemainingSectorPaint.setColor(mBorderPaint.getColor());
        } else {
            mFrom_color = from_color;
            mTo_color = to_color;
        }
    }

    public void setPhase(double phase) {
        if ((phase < 0) || (phase > 1)) {
            throw new IllegalArgumentException("phase: " + phase);
        }
        mPhase = phase;
        invalidate();
        getPercentage();
    }

    public double getPercentage() {
        double percentage = mPhase * 100;
        if (percentage > 100) {
            percentage = 100;
        }
        return percentage;
    }

    private void colorTransition() {
        int startColor = mFrom_color;
        int endColor = mTo_color;
        double newColorRed;
        double newColorBlue;
        double newColorGreen;
        //RED
        newColorRed = Color.red(endColor) + mPhase * (Color.red(startColor) - (Color.red(endColor)));

        //BLUE
        newColorBlue = Color.blue(endColor) + mPhase * (Color.blue(startColor) - (Color.blue(endColor)));

        //GREEN
        newColorGreen = Color.green(endColor) + mPhase * (Color.green(startColor) - (Color.green(endColor)));

        mBorderPaint.setColor(Color.rgb((int) newColorRed, (int) newColorGreen, (int) newColorBlue));
        mRemainingSectorPaint.setColor(mBorderPaint.getColor());
    }

    double mCurrentValue = 1;

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
                ((MainActivity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCurrentValue -= step;
                        if (mCurrentValue < 0) {
                            mCurrentValue = 1;
                        }
                        CountdownIndicator.this.setPhase(mCurrentValue);
                    }
                });
            }
        }, 0, TIMER_REFRESH_RATE);
    }

    public void setTimerInterval(int interval) {
        mTimerInterval = interval;
        startRefreshTimer();
    }

    void setOnTimerCompleateListener() {

    }

    private void standardColorChange() {
        double percentage = getPercentage();
        int red = 0;
        int green = 255;
        int blue = 0;
        if (percentage > 50) {
            double colorValue = (percentage - 50) * 2;
            colorValue = 255 * (colorValue / 100);
            red = 255 - (int) colorValue;
        }
        if (percentage < 50) {
            red = 255;
            double colorValue = percentage * 2;
            colorValue = 255 * (colorValue / 100);
            green = (int) colorValue;
        }
        mBorderPaint.setColor(Color.rgb(red, green, blue));
        mRemainingSectorPaint.setColor(mBorderPaint.getColor());

    }

}