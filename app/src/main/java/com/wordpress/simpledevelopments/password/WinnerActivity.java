package com.wordpress.simpledevelopments.password;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.ViewGroup;
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

    private final int NUM_BALLOONS = 16;


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

                // Calculate the number of pixels for a dimension of 100dp
                int px = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        100,
                        getResources().getDisplayMetrics()
                );

                // Get screen dimensions
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size);
                float screenHeight = size.y;
                float screenWidth = size.x;


                //ImageView[] balloons = new ImageView[NUM_BALLOONS];
                //ObjectAnimator[] animators = new ObjectAnimator[NUM_BALLOONS];


                Random random = new Random();
                ConstraintLayout layout = findViewById(R.id.activity_winner);

                for (int i = 0; i < NUM_BALLOONS; i++) {
                    // Create Balloon ImageView with given size
                    ImageView balloonView = new ImageView(getApplicationContext());
                    balloonView.setId(View.generateViewId());
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(px,px);
                    balloonView.setLayoutParams(params);

                    // Create Drawable to add to ImageView
                    LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getApplicationContext(), R.drawable.balloon_list);
                    balloonView.setImageDrawable(layerDrawable);
                    DrawableCompat.setTint(layerDrawable.getDrawable(0).mutate(),generateRandomPastelColor(random));

                    // Add the Balloon ImageView at the default position
                    // at X=0, Y=screenHeight (just below view)
                    layout.addView(balloonView);
                    balloonView.setTranslationY(screenHeight);


                    // Give balloon random x value somewhere within the bounds of the screen
                    balloonView.setTranslationX(random.nextInt((int) (screenWidth - px + 1)));

                    //Animate balloon
                    ObjectAnimator balloonAnimator = ObjectAnimator.ofFloat(balloonView, "translationY",screenHeight, -1 * px);
                    balloonAnimator.setDuration(5000);
                    balloonAnimator.setInterpolator(new LinearInterpolator());
                    balloonAnimator.setStartDelay(500 * i);
                    //balloonAnimator.setStartDelay(random.nextInt(10000));
                    balloonAnimator.start();
                }

            }
        };
        Handler handler =  new Handler();
        handler.postDelayed(runnable, 1000);


    }
    public int generateRandomPastelColor(Random rand) {
        final int baseColor = Color.WHITE;

        final int red = (Color.red(baseColor) + rand.nextInt(256)) / 2;
        final int green = (Color.green(baseColor) + rand.nextInt(256)) / 2;
        final int blue = (Color.blue(baseColor) + rand.nextInt(256)) / 2;

        return Color.rgb(red, green, blue);
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
