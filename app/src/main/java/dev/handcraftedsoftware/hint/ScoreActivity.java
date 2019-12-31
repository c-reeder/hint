package dev.handcraftedsoftware.hint;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Locale;


/**
 * Activity which shows the score table and totals at the end of the game (after the winner screen)
 * @author Connor Reeder
 * written December 22, 2019
 */
public class ScoreActivity extends AppCompatActivity {

    private static final int NUM_ROUNDS = 6;
    private static final String TAG = "ScoreActivity";

    private String[] aWords;
    private String[] bWords;
    private int[] aScores1;
    private int[] aScores2;
    private int[] bScores1;
    private int[] bScores2;
    private int totalScore1;
    private int totalScore2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
        }
        setContentView(R.layout.activity_score);

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

        Intent parentIntent = getIntent();

//        TextView winnerView = findViewById(R.id.winnerText);
//        if (parentIntent.getStringExtra(GK.WINNER_TEAM_NAME) != null) {
//            winnerView.setText(parentIntent.getStringExtra(GK.WINNER_TEAM_NAME));
//        } else {
//            winnerView.setText(R.string.its_a_tie);
//        }

        if (parentIntent.getIntArrayExtra(GK.A_SCORES_1) != null) {
            aScores1 = parentIntent.getIntArrayExtra(GK.A_SCORES_1);
        } else {
            Log.d(TAG, "aScores1 not passed correctly!");
        }
        if (parentIntent.getIntArrayExtra(GK.A_SCORES_2) != null) {
            aScores2 = parentIntent.getIntArrayExtra(GK.A_SCORES_2);
        } else {
            Log.d(TAG, "aScores2 not passed correctly!");
        }

        if (parentIntent.getIntArrayExtra(GK.B_SCORES_1) != null) {
            bScores1 = parentIntent.getIntArrayExtra(GK.B_SCORES_1);
        } else {
            Log.d(TAG, "bScores1 not passed correctly!");
        }

        if (parentIntent.getIntArrayExtra(GK.B_SCORES_2) != null) {
            bScores2 = parentIntent.getIntArrayExtra(GK.B_SCORES_2);
        } else {
            Log.d(TAG, "bScores1 not passed correctly!");
        }

        if (parentIntent.getStringArrayExtra(GK.A_WORDS) != null) {
            aWords = parentIntent.getStringArrayExtra(GK.A_WORDS);
        } else {
            Log.d(TAG, "aWords not passed correctly!");
        }

        if (parentIntent.getStringArrayExtra(GK.B_WORDS) != null) {
            bWords = parentIntent.getStringArrayExtra(GK.B_WORDS);
        } else {
            Log.d(TAG, "bWords not passed correctly!");
        }

        if (parentIntent.getIntExtra(GK.TOTAL_SCORE_1, -1) >= 0) {
            totalScore1 = parentIntent.getIntExtra(GK.TOTAL_SCORE_1, 0);
        } else {
            Log.d(TAG, "totalScore1 not passed correctly!");
        }

        if (parentIntent.getIntExtra(GK.TOTAL_SCORE_2, -1) >= 0) {
            totalScore2 = parentIntent.getIntExtra(GK.TOTAL_SCORE_2, 0);
        } else {
            Log.d(TAG, "totalScore2 not passed correctly!");
        }

        TableLayout scoreTable = findViewById(R.id.scoreTable);
        for (int i = 0; i < NUM_ROUNDS; i++) {
            // Get references to each part of the row
            TableRow row = (TableRow) scoreTable.getChildAt(i + 1);
            LinearLayout leftSide = (LinearLayout) row.getChildAt(0);
            LinearLayout rightSide = (LinearLayout) row.getChildAt(1);

            // Create components of each score record (for word and score)
            LinearLayout aWordLayout = new LinearLayout(getApplicationContext());
            LinearLayout bWordLayout = new LinearLayout(getApplicationContext());

            @SuppressLint("InflateParams") TextView aWordView = (TextView) getLayoutInflater().inflate(R.layout.winner_table_textview, null);
            @SuppressLint("InflateParams") TextView bWordView = (TextView) getLayoutInflater().inflate(R.layout.winner_table_textview, null);

            TextView aWordPoints = new TextView(getApplicationContext());
            TextView bWordPoints = new TextView(getApplicationContext());


            // Join components together for form a score record
            aWordLayout.setOrientation(LinearLayout.HORIZONTAL);
            aWordLayout.addView(aWordView);
            aWordLayout.addView(aWordPoints);
            aWordView.setText(aWords[i]);

            bWordLayout.setOrientation(LinearLayout.HORIZONTAL);
            bWordLayout.addView(bWordView);
            bWordLayout.addView(bWordPoints);
            bWordView.setText(bWords[i]);

            // Place score record on the correct side (left or right)
            // and correctly format the score (color and size)

            // for the a words
            if (aScores1[i] > 0) {
                aWordPoints.setText(String.format(Locale.getDefault()," +%d",aScores1[i]));
                formatScore(aWordPoints,aScores1[i]);
                leftSide.addView(aWordLayout);
            } else {
                aWordPoints.setText(String.format(Locale.getDefault()," +%d",aScores2[i]));
                formatScore(aWordPoints,aScores2[i]);
                rightSide.addView(aWordLayout);
            }

            // for the b words
            if (bScores1[i] > 0) {
                bWordPoints.setText(String.format(Locale.getDefault()," +%d",bScores1[i]));
                formatScore(bWordPoints,bScores1[i]);
                leftSide.addView(bWordLayout);
            } else {
                bWordPoints.setText(String.format(Locale.getDefault()," +%d",bScores2[i]));
                formatScore(bWordPoints,bScores2[i]);
                rightSide.addView(bWordLayout);
            }
        }

        // Fill and format the score totals at the bottom
        TextView sum1View = findViewById(R.id.sum1);
        TextView sum2View = findViewById(R.id.sum2);
        sum1View.setText(String.format(Locale.getDefault(),"%d",totalScore1));
        sum2View.setText(String.format(Locale.getDefault(),"%d",totalScore2));


        if (totalScore1 > totalScore2) {
            sum1View.setTextColor(Color.parseColor("#006400"));
        } else if (totalScore2 > totalScore1) {
            sum2View.setTextColor(Color.parseColor("#006400"));
        }
    }

    // Helper method to format the score TextView for an individual score record
    private void formatScore(TextView textView, int score) {
        switch (score) {
            case 10:
            case 9:
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                textView.setTextColor(Color.parseColor("#006400"));
                textView.setTextSize(22);
                break;
            case 8:
            case 7:
            case 6:
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                textView.setTextColor(Color.parseColor("#006400"));
                textView.setTextSize(20);
                break;
            default:
                textView.setTextColor(Color.parseColor("#006400"));
                textView.setTextSize(20);
                break;
        }
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


    public void restartGame(View view) {
        supportFinishAfterTransition();
    }
}
