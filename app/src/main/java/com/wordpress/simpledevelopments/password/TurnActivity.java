package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class TurnActivity extends AppCompatActivity implements OneDirectionViewPager.SwipeController {

    private static final String TAG = "TurnActivity";

    // Ever-Changing "Current" Variables
    private int currRound;
    private int currPP;
    private boolean isPartnerB;
    private boolean isTeam2;
    private int currScore1;
    private int currScore2;
    private int currSkipCountA;
    private int currSkipCountB;

    // Values Constant for the Entirety of one Game
    private boolean inPlay;
    private String teamName1;
    private String teamName2;
    private List<String> wordList;

    // Components of the Display
    private TextView roundView;
    private TextView scoreView;
    private TextView ppView;
    private TextView partnerLetterView;
    private TextView teamNameView;
    OneDirectionViewPager viewPager;
    TextPagerAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn);
        Intent parentIntent = getIntent();

        // Setup Display
        roundView = (TextView) findViewById(R.id.roundText);
        scoreView = (TextView) findViewById(R.id.scoreText);
        ppView = (TextView) findViewById(R.id.possPointsText);
        partnerLetterView = (TextView) findViewById(R.id.partnerLetterText);
        teamNameView = (TextView) findViewById(R.id.teamName);
        viewPager = (OneDirectionViewPager) findViewById(R.id.pager);
        viewPager.setSwipeController(this);

        // Init Game Values
        teamName1 = parentIntent.getStringExtra("teamName1");
        teamName2 = parentIntent.getStringExtra("teamName2");
        currRound = 1;
        currPP = 10;
        //currWord = "Magnificent";
        isPartnerB = false;
        isTeam2 = false;
        currScore1 = 0;
        currScore2 = 0;
        currSkipCountA = 0;
        currSkipCountB = 0;

        // Init Display Values
        //updateDisplay();

        Log.d(TAG, "Getting Words!");
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            JSONTask task = new JSONTask() {
                @Override
                protected void onPostExecute(String result) {
                    try {
                        //Change later to statically sized array once server is updated
                        wordList = new ArrayList<>();
                        JSONArray response = new JSONArray(result);
                        for (int i = 0; i < response.length(); i++) {
                            //Log.v(TAG, "WORD: " + response.getString(i));
                            wordList.add(response.getString(i));
                        }
                        Log.d(TAG, "Got " + wordList.size() + " words!");
                        ProgressBar loadingIcon = (ProgressBar) findViewById(R.id.progressBar);
                        loadingIcon.setVisibility(View.GONE);
                        initWords();
                        updateDisplay();
                        inPlay = true;
                        Log.d(TAG, "Beginning Game!");
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                    //Log.d(TAG, "RESULT: " + result);
                }
            };

            //task.execute("https://www.thegamegal.com/wordgenerator/generator.php?game=2&category=6");
            task.execute("https://wordvault.herokuapp.com/passwords");
        } else {
            Log.e(TAG, "Not connected to network");
        }


    }
    public void initWords() {
        adapter = new TextPagerAdapter(this, wordList);
        viewPager.setAdapter(adapter);
        /*viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    onSwiped(viewPager.getCurrentItem());
                }
            }
        });*/
        Log.d(TAG, "Words Initialized!");
    }
    @Override
    public void onSwiped(int newIndex) {
        Log.d(TAG, "onSwiped, newIndex: " + newIndex);
        if (isPartnerB) {
            currSkipCountB++;
            Log.d(TAG, "Partner B swiped, new SkipCount is: " + currSkipCountB);
        } else {
            currSkipCountA++;
            Log.d(TAG, "Partner A swiped, new SkipCount is: " + currSkipCountA);
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
        if (!inPlay)
            return;
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
            nextWord();
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
                nextWord();
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
            inPlay = false;
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
    private void nextWord() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    @Override
    public boolean canSwipe() {
        if (isPartnerB) {
            boolean canSkip =  (currPP == 10) && currSkipCountB < 5;
            Log.d(TAG, "B canSwipe: " + canSkip);
            if (!canSkip)
                Toast.makeText(this,"No Skips left!",Toast.LENGTH_SHORT).show();
            return canSkip;
        } else {
            boolean canSkip = (currPP == 10) && currSkipCountA < 5;
            Log.d(TAG, "A canSwipe: " + canSkip);
            if(!canSkip)
                Toast.makeText(this,"No Skips left!",Toast.LENGTH_SHORT).show();
            return canSkip;
        }
    }
}
