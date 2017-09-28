package com.wordpress.simpledevelopments.password;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

public class TimerPieFragment extends Fragment {
    private static final String TAG = "TimerPieFragment";

    private TimerPie.TimerListener listener;

    private TimerPie timerPie;
    private int angle;
    boolean canceled;
    private ValueAnimator valueAnimator;
    private MyUpdateListener updateListener;
    private MyAnimatorListener animatorListener;


    public TimerPieFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        animatorListener = new MyAnimatorListener();
        updateListener = new MyUpdateListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        timerPie = (TimerPie) inflater.inflate(R.layout.fragment_timer_pie, container,false);
        timerPie.setTimerListener(listener);
        return timerPie;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TimerPie.TimerListener) {
            listener = (TimerPie.TimerListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement TimerPie.TimerListener!");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    public void startTimer() {
        startTimer(0L);
    }
    public void startTimer(long startTime) {
        if (valueAnimator != null) {
            Log.d(TAG, "TimerPie already started!");
            return;
        }
        valueAnimator = ValueAnimator.ofInt(360,0);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(30 * 1000);
        valueAnimator.setCurrentPlayTime(startTime);
        valueAnimator.addUpdateListener(updateListener);
        valueAnimator.addListener(animatorListener);
        valueAnimator.start();
    }

    public void resetTimer() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        angle = 360;
        timerPie.setAngle(360);
    }


    public interface TimerPieFragmentListener {
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
                if (listener != null) {
                    listener.onTimerComplete();
                } else {
                    Log.e(TAG, "null listener!");
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
            timerPie.setAngle(angle);
        }
    }

    public void setVisibility(int visibility) {
        timerPie.setVisibility(visibility);
    }
}
