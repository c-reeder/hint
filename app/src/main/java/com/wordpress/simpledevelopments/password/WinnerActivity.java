package com.wordpress.simpledevelopments.password;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class WinnerActivity extends AppCompatActivity {

    private static final String TAG = "WinnerActivity";

    private String[] aWords;
    private String[] bWords;
    private int[] aScores1;
    private int[] aScores2;
    private int[] bScores1;
    private int[] bScores2;
    private int score1;
    private int score2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);
        Intent parentIntent = getIntent();
        Log.d(TAG, parentIntent.getStringExtra("winnerTeamName"));
        TextView winnerView = (TextView) findViewById(R.id.winnerText);

        if (parentIntent.getStringExtra("winnerTeamName") != null) {
            winnerView.setText(parentIntent.getStringExtra("winnerTeamName"));
        } else {
            winnerView.setText("It's a Tie!!!");
        }

        if (parentIntent.getIntArrayExtra("aScores1") != null) {
            aScores1 = parentIntent.getIntArrayExtra("aScores1");
        } else {
            Log.d(TAG, "aScores1 not passed correctly!");
        }
        if (parentIntent.getIntArrayExtra("aScores2") != null) {
            aScores2 = parentIntent.getIntArrayExtra("aScores2");
        } else {
            Log.d(TAG, "aScores2 not passed correctly!");
        }

        if (parentIntent.getIntArrayExtra("bScores1") != null) {
            bScores1 = parentIntent.getIntArrayExtra("bScores1");
        } else {
            Log.d(TAG, "bScores1 not passed correctly!");
        }

        if (parentIntent.getIntArrayExtra("bScores2") != null) {
            bScores2 = parentIntent.getIntArrayExtra("bScores2");
        } else {
            Log.d(TAG, "bScores1 not passed correctly!");
        }

        if (parentIntent.getStringArrayExtra("aWords") != null) {
            aWords = parentIntent.getStringArrayExtra("aWords");
        } else {
            Log.d(TAG, "aWords not passed correctly!");
        }

        if (parentIntent.getStringArrayExtra("bWords") != null) {
            bWords = parentIntent.getStringArrayExtra("bWords");
        } else {
            Log.d(TAG, "bWords not passed correctly!");
        }

        if (parentIntent.getIntExtra("score1", -1) >= 0) {
            score1 = parentIntent.getIntExtra("score1", 0);
        } else {
            Log.d(TAG, "score1 not passed correctly!");
        }

        if (parentIntent.getIntExtra("score2", -1) >= 0) {
            score2 = parentIntent.getIntExtra("score2", 0);
        } else {
            Log.d(TAG, "score2 not passed correctly!");
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
        sum1View.setText("" + score1);
        sum2View.setText("" + score2);

    }
}
