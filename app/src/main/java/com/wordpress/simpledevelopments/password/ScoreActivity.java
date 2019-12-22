package com.wordpress.simpledevelopments.password;

import android.content.Intent;
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
            TableRow row = (TableRow) scoreTable.getChildAt(i + 1);
            LinearLayout leftSide = (LinearLayout) row.getChildAt(0);
            LinearLayout rightSide = (LinearLayout) row.getChildAt(1);

            TextView aWordView = (TextView) getLayoutInflater().inflate(R.layout.winner_table_textview, null);
            if (aScores1[i] > 0) {
                aWordView.setText(aWords[i] + " +" + aScores1[i]);
                leftSide.addView(aWordView);
            } else {
                aWordView.setText(aWords[i] + " +" + aScores2[i]);
                rightSide.addView(aWordView);
            }
            TextView bWordView = (TextView) getLayoutInflater().inflate(R.layout.winner_table_textview, null);
            if (bScores1[i] > 0) {
                bWordView.setText(bWords[i] + " +" + bScores1[i]);
                leftSide.addView(bWordView);
            } else {
                bWordView.setText(bWords[i] + " +" + bScores2[i]);
                rightSide.addView(bWordView);
            }
        }

        TextView sum1View = findViewById(R.id.sum1);
        TextView sum2View = findViewById(R.id.sum2);
        sum1View.setText(String.format("%d",totalScore1));
        sum2View.setText(String.format("%d",totalScore2));
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

    public void restartGame(View view) {
        supportFinishAfterTransition();
    }
}
