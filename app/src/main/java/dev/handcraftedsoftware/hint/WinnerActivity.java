package dev.handcraftedsoftware.hint;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;

import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.widget.AppCompatImageView;


import java.util.Arrays;
import java.util.Random;

/**
 * Activity which displays the name of the winning team at the end of the game in a fun way!
 * @author Connor Reeder
 * written December 21, 2019
 */
public class WinnerActivity extends AppCompatActivity {

    //private static final String TAG = "WinnerActivity";

    private final int NUM_BALLOONS = 16;

    private Bundle scoreExtras;

    private static final String TAG = "WinnerActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
        }

        setContentView(R.layout.activity_winner);

        Intent parentIntent = getIntent();
        // For testing purposes only
//        Intent parentIntent = new Intent();
//        parentIntent.putExtra(GK.A_SCORES_1,new int[]{10, 9, 10, 0, 10, 9});
//        parentIntent.putExtra(GK.A_SCORES_2,new int[]{0, 0, 0, 10,0, 0});
//        parentIntent.putExtra(GK.B_SCORES_1,new int[]{0, 9, 0, 0, 10, 9});
//        parentIntent.putExtra(GK.B_SCORES_2,new int[]{9, 0, 9, 10, 0, 0});
//        parentIntent.putExtra(GK.A_WORDS,new String[]{"bunk bed", "stove", "condition", "sweater", "rope", "edit"});
//        parentIntent.putExtra(GK.B_WORDS,new String[]{"flight", "president", "bushes", "tomorrow", "pastry", "disc golf (frisbee golf)"});
//        parentIntent.putExtra(GK.TOTAL_SCORE_1, 76);
//        parentIntent.putExtra(GK.TOTAL_SCORE_2, 38);
//        parentIntent.putExtra(GK.TEAM_NAME_1, "Team 1");
//        parentIntent.putExtra(GK.TEAM_NAME_2, "Team 2");
//        parentIntent.putExtra(GK.DIFFICULTY, "easy");
//        parentIntent.putExtra(GK.LANGUAGE, "English");
//        parentIntent.putExtra(GK.WINNER_TEAM_NAME, "Team 1");


        Log.d(TAG, "GK.A_SCORES_1: " + parentIntent.getIntArrayExtra(GK.A_SCORES_1));
        Log.d(TAG, "GK.A_SCORES_2: " + parentIntent.getIntArrayExtra(GK.A_SCORES_2));
        Log.d(TAG, "GK.B_SCORES_1: " + parentIntent.getIntArrayExtra(GK.B_SCORES_1));
        Log.d(TAG, "GK.B_SCORES_2: " + parentIntent.getIntArrayExtra(GK.B_SCORES_2));
        Log.d(TAG, "GK.A_WORDS: " + parentIntent.getStringArrayExtra(GK.A_WORDS));
        Log.d(TAG, "GK.B_WORDS: " + parentIntent.getStringArrayExtra(GK.B_WORDS));
        Log.d(TAG, "GK.TOTAL_SCORE_1: " + parentIntent.getIntExtra(GK.TOTAL_SCORE_1,0));
        Log.d(TAG, "GK.TOTAL_SCORE_2: " + parentIntent.getIntExtra(GK.TOTAL_SCORE_2, 0));
        Log.d(TAG, "GK.TEAM_NAME_1: " + parentIntent.getStringExtra(GK.TEAM_NAME_1));
        Log.d(TAG, "GK.TEAM_NAME_2: " + parentIntent.getStringExtra(GK.TEAM_NAME_2));
        Log.d(TAG, "GK.DIFFICULTY: " + parentIntent.getStringExtra(GK.DIFFICULTY));
        Log.d(TAG, "GK.LANGUAGE: " + parentIntent.getStringExtra(GK.LANGUAGE));


//        Intent parentIntent = getIntent();

        scoreExtras = parentIntent.getExtras();
        Log.d(TAG, "Passing: " + Arrays.toString(parentIntent.getIntArrayExtra(GK.A_SCORES_1)));
        final TextView winnerView = findViewById(R.id.winnerText);

        if (parentIntent.getStringExtra(GK.WINNER_TEAM_NAME) != null) {
            winnerView.setText(parentIntent.getStringExtra(GK.WINNER_TEAM_NAME));
        } else {
            winnerView.setText(R.string.its_a_tie);
        }

        Runnable balloonRunnable = new Runnable() {
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
                float screenHeight;
                float screenWidth;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    display.getRealSize(size);
                } else {
                    display.getSize(size);
                }
                screenHeight = size.y;
                screenWidth = size.x;


                //ImageView[] balloons = new ImageView[NUM_BALLOONS];
                //ObjectAnimator[] animators = new ObjectAnimator[NUM_BALLOONS];


                Random random = new Random();
                ConstraintLayout layout = findViewById(R.id.activity_winner);

                for (int i = 0; i < NUM_BALLOONS; i++) {
                    // Create Balloon ImageView with given size
                    AppCompatImageView balloonView = new AppCompatImageView(getApplicationContext());
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        balloonView.setId(androidx.core.view.ViewCompat.generateViewId());
                    }
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(px,px);
                    balloonView.setLayoutParams(params);

                    // Create Drawable to add to ImageView
                    balloonView.setImageResource(R.drawable.balloon_list);
                    LayerDrawable layerDrawable = (LayerDrawable) balloonView.getDrawable();
                    DrawableCompat.setTint(layerDrawable.getDrawable(0).mutate(),generateRandomRedColor(random));

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
                    balloonAnimator.start();
                }

            }
        };
        Runnable textRunnable = new Runnable() {
            @Override
            public void run() {
                ObjectAnimator textAnimation = ObjectAnimator.ofPropertyValuesHolder(
                        winnerView,
                        PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                        PropertyValuesHolder.ofFloat("scaleY", 1.2f));
                textAnimation.setDuration(310);

                textAnimation.setRepeatCount(ObjectAnimator.INFINITE);
                textAnimation.setRepeatMode(ObjectAnimator.REVERSE);

                textAnimation.start();
            }
        };
        Handler handler =  new Handler();
        handler.postDelayed(textRunnable, 500);
        handler.postDelayed(balloonRunnable, 1000);

        Button scoreButton = findViewById(R.id.scoreButton);
        Button restartButton = findViewById(R.id.restartButton);

        scoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewScore();
            }
        });
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartGame();
            }
        });


    }

    private int generateRandomRedColor(Random rand) {
        final int greenAndBlue = rand.nextInt(165);
        return Color.rgb(255, greenAndBlue, greenAndBlue);
    }

    private void restartGame() {
        supportFinishAfterTransition();
    }

    private void viewScore() {
        Intent viewScoreIntent = new Intent(this, ScoreActivity.class);
        viewScoreIntent.putExtras(scoreExtras);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(viewScoreIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(viewScoreIntent);
        }
        supportFinishAfterTransition();
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
        int value = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            value = value | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(value);
    }


}
