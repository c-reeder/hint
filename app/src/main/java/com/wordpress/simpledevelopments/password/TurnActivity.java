package com.wordpress.simpledevelopments.password;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TurnActivity extends AppCompatActivity {

    private static final String TAG = "TurnActivity";

    // Ever-Changing "Current" Variables
    private int currRound;
    private int currPP;
    private String currWord;
    private boolean isPartnerB;
    private boolean isTeam2;
    private int currScore1;
    private int currScore2;

    // Values Constant for the Entirety of one Game
    private String teamName1;
    private String teamName2;

    // Components of the Display
    private TextView roundView;
    private TextView scoreView;
    private TextView ppView;
    private TextView wordView;
    private TextView partnerLetterView;
    private TextView teamNameView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn);
        Intent parentIntent = getIntent();

        // Init Game Values
        teamName1 = parentIntent.getStringExtra("teamName1");
        teamName2 = parentIntent.getStringExtra("teamName2");
        currRound = 1;
        currPP = 10;
        currWord = "Magnificent";
        isPartnerB = false;
        isTeam2 = false;
        currScore1 = 0;
        currScore2 = 0;


        // Setup Display
        roundView = (TextView) findViewById(R.id.roundText);
        scoreView = (TextView) findViewById(R.id.scoreText);
        ppView = (TextView) findViewById(R.id.possPointsText);
        wordView = (TextView) findViewById(R.id.wordText);
        partnerLetterView = (TextView) findViewById(R.id.partnerLetterText);
        teamNameView = (TextView) findViewById(R.id.teamName);

        // Init Display Values
        updateDisplay();

        Log.d(TAG, "Beginning Game!");
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
        ViewGroup rootLayout = (ViewGroup) findViewById(R.id.activity_turn);
        rootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    private void updateDisplay() {
        roundView.setText("Round #" + currRound);
        scoreView.setText(currScore1 + ":" + currScore2);
        ppView.setText("" + currPP);
        wordView.setText(currWord);
        if(!isPartnerB)
            partnerLetterView.setText("A");
        else
            partnerLetterView.setText("B");
        if(!isTeam2)
            teamNameView.setText(teamName1);
        else
            teamNameView.setText(teamName2);
    }

    /**
     * The method called when the guess has been made
     * @param view The button pressed to signal a guess has been made
     */
    public void guessMade(View view) {
        if (view.getId() == R.id.successButton) {
            Log.d(TAG, "Correct!");

            // Score Addition Logic
            if (!isTeam2)
                currScore1 += currPP;
            else
                currScore2 += currPP;
            currPP = 10;

            // Next Turn Logic
            //-------CHANGE WORD HERE----------
            if (isPartnerB)
                currRound++;
            isPartnerB = !isPartnerB;
            isTeam2 = false;
        } else if (view.getId() == R.id.failureButton) {
            Log.d(TAG, "Failure!");
            currPP--;

            // Next Turn Logic
            if (currPP < 1) {
                // if the word was not guessed AT ALL
                //-------CHANGE WORD HERE----------
                currPP = 10;
                if (isPartnerB)
                    currRound++;
                isPartnerB = !isPartnerB;
            } else {
                isTeam2 = !isTeam2;
            }
        }

        // Check if end of game
        if (currRound > 5) {
            //Launch Winner Activity
            Intent winnerIntent = new Intent(this, WinnerActivity.class);
            if (currScore1 > currScore2)
                winnerIntent.putExtra("winnerTeamName", teamName1);
            else if (currScore2 > currScore1)
                winnerIntent.putExtra("winnerTeamName", teamName2);
            startActivity(winnerIntent);
        }


        updateDisplay();
    }
}
