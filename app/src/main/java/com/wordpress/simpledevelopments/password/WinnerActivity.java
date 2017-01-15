package com.wordpress.simpledevelopments.password;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.View;

public class WinnerActivity extends AppCompatActivity {

    private static final String TAG = "WinnerActivity";

    private String[] aWords;
    private String[] bWords;
    private int[] aScores1;
    private int[] aScores2;
    private int[] bScores1;
    private int[] bScores2;
    private int totalScore1;
    private int totalScore2;
    private String teamName1;
    private String teamName2;
    private String difficulty;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);
        Intent parentIntent = getIntent();
        Log.d(TAG, parentIntent.getStringExtra(GV.WINNER_TEAM_NAME));
        TextView winnerView = (TextView) findViewById(R.id.winnerText);

        if (parentIntent.getStringExtra(GV.WINNER_TEAM_NAME) != null) {
            winnerView.setText(parentIntent.getStringExtra(GV.WINNER_TEAM_NAME));
        } else {
            winnerView.setText("It's a Tie!!!");
        }

        if (parentIntent.getIntArrayExtra(GV.A_SCORES_1) != null) {
            aScores1 = parentIntent.getIntArrayExtra(GV.A_SCORES_1);
        } else {
            Log.d(TAG, "aScores1 not passed correctly!");
        }
        if (parentIntent.getIntArrayExtra(GV.A_SCORES_2) != null) {
            aScores2 = parentIntent.getIntArrayExtra(GV.A_SCORES_2);
        } else {
            Log.d(TAG, "aScores2 not passed correctly!");
        }

        if (parentIntent.getIntArrayExtra(GV.B_SCORES_1) != null) {
            bScores1 = parentIntent.getIntArrayExtra(GV.B_SCORES_1);
        } else {
            Log.d(TAG, "bScores1 not passed correctly!");
        }

        if (parentIntent.getIntArrayExtra(GV.B_SCORES_2) != null) {
            bScores2 = parentIntent.getIntArrayExtra(GV.B_SCORES_2);
        } else {
            Log.d(TAG, "bScores1 not passed correctly!");
        }

        if (parentIntent.getStringArrayExtra(GV.A_WORDS) != null) {
            aWords = parentIntent.getStringArrayExtra(GV.A_WORDS);
        } else {
            Log.d(TAG, "aWords not passed correctly!");
        }

        if (parentIntent.getStringArrayExtra(GV.B_WORDS) != null) {
            bWords = parentIntent.getStringArrayExtra(GV.B_WORDS);
        } else {
            Log.d(TAG, "bWords not passed correctly!");
        }

        if (parentIntent.getIntExtra(GV.TOTAL_SCORE_1, -1) >= 0) {
            totalScore1 = parentIntent.getIntExtra(GV.TOTAL_SCORE_1, 0);
        } else {
            Log.d(TAG, "totalScore1 not passed correctly!");
        }

        if (parentIntent.getIntExtra(GV.TOTAL_SCORE_2, -1) >= 0) {
            totalScore2 = parentIntent.getIntExtra(GV.TOTAL_SCORE_2, 0);
        } else {
            Log.d(TAG, "totalScore2 not passed correctly!");
        }

        if (parentIntent.getStringExtra(GV.TEAM_NAME_1) != null) {
            teamName1 = parentIntent.getStringExtra(GV.TEAM_NAME_1);
        } else {
            Log.d(TAG, "teamName1 not passed to WinnerActivity correctly!");
        }

        if (parentIntent.getStringExtra(GV.TEAM_NAME_2) != null) {
            teamName2 = parentIntent.getStringExtra(GV.TEAM_NAME_2);
        } else {
            Log.d(TAG, "teamName2 not passed to WinnerActivity correctly!");
        }

        if (parentIntent.getStringExtra(GV.DIFFICULTY) != null) {
            difficulty = parentIntent.getStringExtra(GV.DIFFICULTY);
        } else {
            difficulty = "easy";
        }


        TableLayout scoreTable = (TableLayout) findViewById(R.id.scoreTable);
        for (int i = 0; i < 5; i++) {
            TableRow row = (TableRow) scoreTable.getChildAt(i + 1);
            DiagonalDoubleTextView wordDouble = (DiagonalDoubleTextView) row.getChildAt(1);
            wordDouble.setText1("" + aWords[i]);
            wordDouble.setText2("" + bWords[i]);

            DiagonalDoubleTextView team1View = (DiagonalDoubleTextView) row.getChildAt(2);
            team1View.setText1("" + aScores1[i]);
            team1View.setText2("" + bScores1[i]);

            DiagonalDoubleTextView team2View = (DiagonalDoubleTextView) row.getChildAt(3);
            team2View.setText1("" + aScores2[i]);
            team2View.setText2("" + bScores2[i]);
        }

        TextView sum1View = (TextView) findViewById(R.id.sum1);
        TextView sum2View = (TextView) findViewById(R.id.sum2);
        sum1View.setText("" + totalScore1);
        sum2View.setText("" + totalScore2);

    }
    public void restartGame(View view) {
        finish();
    }
}
