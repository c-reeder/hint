package com.wordpress.simpledevelopments.password;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.view.Display;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

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
                ImageView vectorView = findViewById(R.id.vectorView);
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                float screenHeight = size.y;
                float vectorHeight = vectorView.getHeight();

                ObjectAnimator textViewAnimator = ObjectAnimator.ofFloat(vectorView, "translationY",screenHeight + vectorHeight, 0f);
                textViewAnimator.setDuration(5000);
                textViewAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                textViewAnimator.start();
            }
        };
        Handler handler =  new Handler();
        handler.postDelayed(runnable, 1100);


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
