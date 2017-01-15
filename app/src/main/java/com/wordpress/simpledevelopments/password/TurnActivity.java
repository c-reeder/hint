package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;


public class TurnActivity extends AppCompatActivity implements OneDirectionViewPager.SwipeController, View.OnTouchListener, MenuFragment.MenuActionsHandler, TextPagerAdapter.OnReadyListener {

    private static final String TAG = "TurnActivity";

    // Components of the Display
    private TextView roundView;
    private TextView scoreView;
    private TenSpinner ppSpinnerView;
    private TextView partnerLetterView;
    private TextView teamNameView;
    private OneDirectionViewPager viewPager;
    //Word-Swiper Functionality
    private TextPagerAdapter adapter;
    private GestureDetector gestureDetector;

    // Values Constant for the Entirety of one Game
    private boolean inPlay;
    private String teamName1;
    private String teamName2;
    private String difficulty;
    private String[] wordList;

    // Ever-Changing "Current" Variables
    private int currRound;
    private int currPP;
    private boolean isPartnerB;
    private boolean isTeam2;
    private int totalScore1;
    private int totalScore2;
    private int currSkipCountA;
    private int currSkipCountB;
    private boolean wordTransition;
    private boolean previousCorrect;

    // Results Variables to be Passed to the Winner Screen
    private String[] aWords;
    private String[] bWords;
    private int[] aScores1;
    private int[] aScores2;
    private int[] bScores1;
    private int[] bScores2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn);
        Intent parentIntent = getIntent();

        // Setup Display
        roundView = (TextView) findViewById(R.id.roundText);
        roundView.setTextSize(25);
        scoreView = (TextView) findViewById(R.id.scoreText);
        ppSpinnerView = (TenSpinner) findViewById(R.id.ppSpinner);
        partnerLetterView = (TextView) findViewById(R.id.partnerLetterText);
        teamNameView = (TextView) findViewById(R.id.teamName);
        viewPager = (OneDirectionViewPager) findViewById(R.id.pager);
        viewPager.setSwipeController(this);

        //Setup Tap-Action on the Word-Swyper which is used to exit the word-transition state
        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                // Single tap used to release the game from word-transition mode
                if (wordTransition) {
                    nextWord();
                    wordTransition = false;
                    return true;
                } else {
                    return false;
                }
            }
        });
        viewPager.setOnTouchListener(this);

        // If the game is started for the first time
        if (savedInstanceState == null) {
            // Init Game Values
            teamName1 = parentIntent.getStringExtra(GV.TEAM_NAME_1);
            teamName2 = parentIntent.getStringExtra(GV.TEAM_NAME_2);
            difficulty = parentIntent.getStringExtra(GV.DIFFICULTY);
            currRound = 1;
            currPP = 10;
            isPartnerB = false;
            isTeam2 = false;
            totalScore1 = 0;
            totalScore2 = 0;
            currSkipCountA = 0;
            currSkipCountB = 0;
            wordTransition = false;

            // Init Results Variables
            aWords = new String[5];
            bWords = new String[5];
            aScores1 = new int[5];
            aScores2 = new int[5];
            bScores1 = new int[5];
            bScores2 = new int[5];

            // Check Network Status and if connected perform GET request to acquire word list from server
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                // Define behavior to occur upon receiving the JSON word data
                JSONTask task = new JSONTask() {
                    @Override
                    protected void onPostExecute(String result) {
                        try {
                            wordList = new String[20];
                            JSONArray response = new JSONArray(result);
                            for (int i = 0; i < response.length(); i++) {
                                wordList[i] = response.getString(i);
                            }
                            if (response.length() != 20) throw new AssertionError("DID NOT GET 20 WORDS!!!");
                            //Hide Loading Icon now that Data has been received
                            ProgressBar loadingIcon = (ProgressBar) findViewById(R.id.progressBar);
                            loadingIcon.setVisibility(View.GONE);
                            initWords();
                            updateDisplay();
                            inPlay = true;
                            //Game has now begun
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                task.execute("https://wordvault.herokuapp.com/passwords/" + difficulty);
                Log.d(TAG, "URL: " + "https://wordvault.herokuapp.com/passwords/" + difficulty);
            } else {
                Log.e(TAG, "Not connected to network");
            }
        } else { //if savedInstanceState != null  -----> We are RE-starting our activity

            // Values Constant for the Entirety of one Game
            inPlay = savedInstanceState.getBoolean(GV.IN_PLAY);
            teamName1 = savedInstanceState.getString(GV.TEAM_NAME_1);
            teamName2 = savedInstanceState.getString(GV.TEAM_NAME_2);
            difficulty = savedInstanceState.getString(GV.DIFFICULTY);
            wordList = savedInstanceState.getStringArray(GV.WORD_LIST);

            // Ever-Changing "Current" Variables
            currRound = savedInstanceState.getInt(GV.CURR_ROUND);
            currPP = savedInstanceState.getInt(GV.CURR_PP);
            isPartnerB = savedInstanceState.getBoolean(GV.IS_PARTNER_B);
            isTeam2 = savedInstanceState.getBoolean(GV.IS_TEAM_2);
            totalScore1 = savedInstanceState.getInt(GV.CURR_SCORE_1);
            totalScore2 = savedInstanceState.getInt(GV.CURR_SCORE_2);
            currSkipCountA = savedInstanceState.getInt(GV.CURR_SKIP_COUNT_A);
            currSkipCountB = savedInstanceState.getInt(GV.CURR_SKIP_COUNT_B);
            wordTransition = savedInstanceState.getBoolean(GV.WORD_TRANSITION);
            previousCorrect = savedInstanceState.getBoolean(GV.PREVIOUS_CORRECT);


            // Results Variables to be Passed to the Winner Screen
            aWords = savedInstanceState.getStringArray(GV.A_WORDS);
            bWords = savedInstanceState.getStringArray(GV.B_WORDS);
            aScores1 = savedInstanceState.getIntArray(GV.A_SCORES_1);
            aScores2 = savedInstanceState.getIntArray(GV.A_SCORES_2);
            bScores1 = savedInstanceState.getIntArray(GV.B_SCORES_1);
            bScores2 = savedInstanceState.getIntArray(GV.B_SCORES_2);

            // Hide the loading icon IMMEDIATELY since we are only re-starting the activity and have already obtained our word data
            ProgressBar loadingIcon = (ProgressBar) findViewById(R.id.progressBar);
            loadingIcon.setVisibility(View.GONE);
            initWords();
            ppSpinnerView.setSpinner(currPP);
            updateDisplay();
            // Game has been successfully restarted
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Values Constant for the Entirety of one Game
        savedInstanceState.putBoolean(GV.IN_PLAY,inPlay);
        savedInstanceState.putString(GV.TEAM_NAME_1,teamName1);
        savedInstanceState.putString(GV.TEAM_NAME_2,teamName2);
        savedInstanceState.putString(GV.DIFFICULTY,difficulty);
        savedInstanceState.putStringArray(GV.WORD_LIST,wordList);

        // Ever-Changing "Current" Variables
        savedInstanceState.putInt(GV.CURR_ROUND,currRound);
        savedInstanceState.putInt(GV.CURR_PP,currPP);
        savedInstanceState.putBoolean(GV.IS_PARTNER_B,isPartnerB);
        savedInstanceState.putBoolean(GV.IS_TEAM_2,isTeam2);
        savedInstanceState.putInt(GV.CURR_SCORE_1, totalScore1);
        savedInstanceState.putInt(GV.CURR_SCORE_2, totalScore2);
        savedInstanceState.putInt(GV.CURR_SKIP_COUNT_A,currSkipCountA);
        savedInstanceState.putInt(GV.CURR_SKIP_COUNT_B,currSkipCountB);
        savedInstanceState.putBoolean(GV.WORD_TRANSITION,wordTransition);
        savedInstanceState.putBoolean(GV.PREVIOUS_CORRECT, previousCorrect);

        // Results Variables to be Passed to the Winner Screen
        savedInstanceState.putStringArray(GV.A_WORDS,aWords);
        savedInstanceState.putStringArray(GV.B_WORDS,bWords);
        savedInstanceState.putIntArray(GV.A_SCORES_1,aScores1);
        savedInstanceState.putIntArray(GV.A_SCORES_2,aScores2);
        savedInstanceState.putIntArray(GV.B_SCORES_1,bScores1);
        savedInstanceState.putIntArray(GV.B_SCORES_2,bScores2);

        super.onSaveInstanceState(savedInstanceState);
    }



    /**
     * Initialize the Word Viewpager (for swiping/skipping through words)
     * This method sets up the word-slider by creating the adapter for the word list
     */
    public void initWords() {
        adapter = new TextPagerAdapter(this, wordList);
        adapter.setReadyListener(this);
        viewPager.setAdapter(adapter);
    }

    /**
     * Callback Method implementing the OneDirectionViewPager which is called upon a swype being performed.
     * In this case we are using it to update the counts of how many times each set of opposing players has skipped a word
     * @param newIndex the index of the OneDirectionViewPager after being swiped.
     */
    @Override
    public void onSwiped(int newIndex) {
        if (isPartnerB) {
            currSkipCountB++;
        } else {
            currSkipCountA++;
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

    /**
     * Helper method that updates the various components of the view with the current values of the
     * variables that back them.
     */
    private void updateDisplay() {
        roundView.setText("#" + currRound);
        scoreView.setText(totalScore1 + ":" + totalScore2);
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
     * The method called when a guess has been made
     * @param view The button pressed to signal that a guess has been made
     */
    public void guessMade(View view) {
        // Do nothing if a guess has already been made but we have not advanced
        // or the game is not currently in play.
        if (wordTransition || !inPlay)
            return;

        // If the guess was correct
        if (view.getId() == R.id.successButton) {
            storeResult();

            // Score Addition Logic
            if (!isTeam2) {
                totalScore1 += currPP;
            } else {
                totalScore2 += currPP;
            }

            // Next Turn Logic
            //-------CHANGE WORD HERE----------
            ppSpinnerView.resetSpinner();
            transitionToNextWord(true);
            currPP = 10;

            // Increment the round number if both sets of opposing players has played
            if (isPartnerB)
                currRound++;
            // Alternate which team begins each round
            isTeam2 = ((currRound % 2) == 0);
            // Flip back and forth between pairs of opposing players
            isPartnerB = !isPartnerB;

            // If the guess was incorrect
        } else if (view.getId() == R.id.failureButton) {
            storeResult();
            // Decrement current possible points
            currPP--;

            // The word was NEVER guessed and there are not more tries left
            // Next Turn Logic
            if (currPP < 1) {
                // if the word was not guessed AT ALL
                //-------CHANGE WORD HERE----------
                ppSpinnerView.resetSpinner();
                transitionToNextWord(false);
                currPP = 10;

                // Increment the round number if both sets of opposing players has played
                if (isPartnerB)
                    currRound++;
                // Alternate which team begins each round
                isTeam2 = ((currRound % 2) == 0);
                // Flip back and forth between pairs of opposing players
                isPartnerB = !isPartnerB;

            } else {// The word has not yet been correctly guessed but there are still chances left
                // Switch teams and decrement possible points
                isTeam2 = !isTeam2;
                ppSpinnerView.spinToNext();
            }
        }

        // Check if end of game
        if (currRound > 5) {
            // Game is Over
            inPlay = false;

            // Create Winner Screen intent
            Intent winnerIntent = new Intent(this, WinnerActivity.class);

            // Determine who the winner is based off the final scores
            if (totalScore1 > totalScore2)
                winnerIntent.putExtra(GV.WINNER_TEAM_NAME, teamName1);
            else if (totalScore2 > totalScore1)
                winnerIntent.putExtra(GV.WINNER_TEAM_NAME, teamName2);

            // Attach the team names and necessary scoring values to the Winner Screen intent
            winnerIntent.putExtra(GV.A_SCORES_1, aScores1);
            winnerIntent.putExtra(GV.A_SCORES_2, aScores2);
            winnerIntent.putExtra(GV.B_SCORES_1, bScores1);
            winnerIntent.putExtra(GV.B_SCORES_2, bScores2);
            winnerIntent.putExtra(GV.A_WORDS, aWords);
            winnerIntent.putExtra(GV.B_WORDS, bWords);
            winnerIntent.putExtra(GV.TOTAL_SCORE_1, totalScore1);
            winnerIntent.putExtra(GV.TOTAL_SCORE_2, totalScore2);
            winnerIntent.putExtra(GV.TEAM_NAME_1, teamName1);
            winnerIntent.putExtra(GV.TEAM_NAME_2, teamName2);
            winnerIntent.putExtra(GV.DIFFICULTY, difficulty);

            //Launch Winner Activity
            startActivity(winnerIntent);
        } else { // If not the end of the game
            updateDisplay();
        }
    }

    /**
     * Helper method to be called every time is word is completed
     * Updates the result variables based on who successfully guessed the word and how many points
     *  they earned.
     */
    private void storeResult() {
        TextView currentView = (TextView) adapter.getCurrentView().findViewById(R.id.singleTextView);
        String currWord = currentView.getText().toString();
        if (isPartnerB) {
            bWords[currRound - 1] = currWord;
            if (isTeam2) {
                bScores1[currRound - 1] = 0;
                bScores2[currRound - 1] = currPP;
            } else {
                bScores1[currRound - 1] = currPP;
                bScores2[currRound - 1] = 0;
            }
        } else {
            aWords[currRound - 1] = currWord;
            if (isTeam2) {
                aScores1[currRound - 1] = 0;
                aScores2[currRound - 1] = currPP;
            } else {
                aScores1[currRound - 1] = currPP;
                aScores2[currRound - 1] = 0;
            }
        }
    }

    /**
     * Puts the game into word-transition mode
     * Once the game is in this mode, the background color of the word-swiper indicates whether the word was succesfully guessed or not
     * and the screen must be tapped for the game to advance.
     * This mode is used when handing the phone to the other set of opposing players
     * @param success
     */
    private void transitionToNextWord(boolean success) {
        previousCorrect = success;
        View currentView = adapter.getCurrentView();
        if (success)
            currentView.setBackgroundColor(Color.GREEN);
        else
            currentView.setBackgroundColor(Color.RED);
        wordTransition = true;
    }

    /**
     * Advances the word-swiper to the next word
     */
    private void nextWord() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    /**
     * Callback method from the OneDirectionViewPager interface
     * @return whether or not to permit the word-swiper to swipe at the moment
     */
    @Override
    public boolean canSwipe() {
        Log.d(TAG, "canSwipe: " + wordTransition);
        if (wordTransition) {
            return false;
        }
        if (isPartnerB) {
            boolean canSkip =  (currPP == 10) && currSkipCountB < 5;
            return canSkip;
        } else {
            boolean canSkip = (currPP == 10) && currSkipCountA < 5;
            return canSkip;
        }
    }


    /**
     * onTouch method used for detecting a tap on the word-swiper
     * @param view the view that the onTouch event was fired from
     * @param motionEvent the motion event that occurred on the touched view
     * @return whether or not the touch was received
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        return false;
    }

    public void pauseGame(View view) {
        MenuFragment menuFragment = new MenuFragment();

        TextView wordText = (TextView) viewPager.findViewById(R.id.singleTextView);
        wordText.setVisibility(View.INVISIBLE);
        menuFragment.show(getSupportFragmentManager(), "MENU_FRAGMENT");
    }

    @Override
    public void restartGame() {
        finish();
    }

    @Override
    public void resumeGame() {
        TextView wordText = (TextView) viewPager.findViewById(R.id.singleTextView);
        wordText.setVisibility(View.VISIBLE);
    }

    /**
     * Called when the viewPager has initialized its first view
     * allowing us to set the background color if we are in the middle of a word transition
     */
    @Override
    public void onTextPagerAdapterReady() {
        if (wordTransition) {
            if (previousCorrect)
                adapter.getCurrentView().setBackgroundColor(Color.GREEN);
            else
                adapter.getCurrentView().setBackgroundColor(Color.RED);
        }
    }
}
