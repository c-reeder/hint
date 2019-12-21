package com.wordpress.simpledevelopments.password;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import java.util.Random;

public class WinnerActivity extends AppCompatActivity {

    private static final String TAG = "WinnerActivity";

    private static final int NUM_ROUNDS = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
        }

        setContentView(R.layout.activity_winner_new);

        Intent parentIntent = new Intent();
        parentIntent.putExtra(GK.WINNER_TEAM_NAME, "Team 1");

        //Intent parentIntent = getIntent();
        TextView winnerView = findViewById(R.id.winnerText);

        if (parentIntent.getStringExtra(GK.WINNER_TEAM_NAME) != null) {
            winnerView.setText(parentIntent.getStringExtra(GK.WINNER_TEAM_NAME));
        } else {
            winnerView.setText(R.string.its_a_tie);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ImageView[] balloons = new ImageView[3];
                int[] xs = new int[3];
                ObjectAnimator[] animators = new ObjectAnimator[3];


                balloons[0] = findViewById(R.id.greenBalloon);
                balloons[1] = findViewById(R.id.redBalloon);
                balloons[2] = findViewById(R.id.blueBalloon);

                Random random = new Random();
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size);
                float screenHeight = size.y;
                float screenWidth = size.x;

                for (int i = 0; i < 3; i++) {
                    xs[i] = random.nextInt((int) (screenWidth - balloons[0].getWidth() + 1));
                    balloons[i].setTranslationX(xs[i]);
                    //balloons[i].setTranslationY(-1 * screenHeight);
                    animators[i] = ObjectAnimator.ofFloat(balloons[i], "translationY",0, -1 * (screenHeight + balloons[0].getHeight()));
                    animators[i].setDuration(5000);
                    animators[i].setInterpolator(new LinearInterpolator());
                    animators[i].setStartDelay(random.nextInt(4001));
                    animators[i].start();
                }

            }
        };
        Handler handler =  new Handler();
        handler.postDelayed(runnable, 1000);


    }
    public void restartGame(View view) {
        //supportFinishAfterTransition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();

    }

    /**
     * Sets app to fullscreen mode
     */
    private void setToFullScreen() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
