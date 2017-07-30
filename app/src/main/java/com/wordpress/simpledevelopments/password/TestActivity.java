package com.wordpress.simpledevelopments.password;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TestActivity extends AppCompatActivity {

    TimerPie timerPie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        timerPie = (TimerPie) findViewById(R.id.timerPie);
    }

    public void start(View view) {
        timerPie.startTimer();
//        ObjectAnimator animation = ObjectAnimator.ofInt (pb, "progress", 0, 100); // see this max value coming back here, we animale towards that value
//        animation.setDuration (4000); //in milliseconds
//        animation.setInterpolator (new LinearInterpolator());
//        animation.start();
    }
}
