package com.wordpress.simpledevelopments.password;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by connor on 5/23/17.
 */

public class TimerPie extends View {

    private static final String TAG = "TimerPie";

    private Paint piePaint;
    private int angle;
    private int color;
    private boolean canceled;

    private ValueAnimator valueAnimator;
    private MyUpdateListener updateListener;
    private MyAnimatorListener animatorListener;

    private TimerListener timerListener;

    public TimerPie(Context context) {
        super(context);
        color = Color.GRAY;
        init();
    }
    public TimerPie(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TimerPie,
                0, 0);
        color = a.getColor(R.styleable.TimerPie_color, Color.GRAY);
        init();
    }
    private void init() {
        piePaint = new Paint();
        piePaint.setColor(color);
        angle = 360;
        canceled = false;

        animatorListener = new MyAnimatorListener();
        updateListener = new MyUpdateListener();
    }
    @Override
    public void onDraw(Canvas canvas) {
        RectF rectF = new RectF(0,0,canvas.getWidth(),canvas.getHeight());
        canvas.drawArc(rectF,270,-1 * angle,true,piePaint);
    }
    public void startTimer() {
        if (valueAnimator != null) {
            Log.d(TAG, "TimerPie already started!");
            return;
        }
        valueAnimator = ValueAnimator.ofInt(360,0);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(30 * 1000);
        valueAnimator.addUpdateListener(updateListener);
        valueAnimator.addListener(animatorListener);
        valueAnimator.start();
    }
    public void resetTimer() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        angle = 360;
        invalidate();
    }

    private class MyAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animator) {
            canceled = false;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            valueAnimator = null;
            if (!canceled) {
                if (timerListener != null) {
                    timerListener.onTimerComplete();
                }
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            canceled = true;
            valueAnimator = null;
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
    private class MyUpdateListener implements  ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            angle = (Integer) valueAnimator.getAnimatedValue();
            invalidate();
        }
    }

    public interface TimerListener {
        public void onTimerComplete();
    }

    public void setTimerListener(TimerListener timerListener) {
        this.timerListener = timerListener;
    }
}
