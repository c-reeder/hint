package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
    private int currScore1;
    private int currScore2;
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
        //Setup Tap-Action on the Word-Swyper
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
            teamName1 = parentIntent.getStringExtra("teamName1");
            teamName2 = parentIntent.getStringExtra("teamName2");
            difficulty = parentIntent.getStringExtra("difficulty");
            currRound = 1;
            currPP = 10;
            isPartnerB = false;
            isTeam2 = false;
            currScore1 = 0;
            currScore2 = 0;
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
                task.execute("https://wordvault.herokuapp.com/passwords/"
                        + difficulty);
                Log.d(TAG, "URL: " + "https://wordvault.herokuapp.com/passwords/"
                        + difficulty);
            } else {
                Log.e(TAG, "Not connected to network");
            }
        } else { //if savedInstanceState != null  -----> We are RE-starting our activity

            // Values Constant for the Entirety of one Game
            inPlay = savedInstanceState.getBoolean("inPlay");
            teamName1 = savedInstanceState.getString("teamName1");
            teamName2 = savedInstanceState.getString("teamName2");
            difficulty = savedInstanceState.getString("difficulty");
            wordList = savedInstanceState.getStringArray("wordList");

            // Ever-Changing "Current" Variables
            currRound = savedInstanceState.getInt("currRound");
            currPP = savedInstanceState.getInt("currPP");
            isPartnerB = savedInstanceState.getBoolean("isPartnerB");
            isTeam2 = savedInstanceState.getBoolean("isTeam2");
            currScore1 = savedInstanceState.getInt("currScore1");
            currScore2 = savedInstanceState.getInt("currScore2");
            currSkipCountA = savedInstanceState.getInt("currSkipCountA");
            currSkipCountB = savedInstanceState.getInt("currSkipCountB");
            wordTransition = savedInstanceState.getBoolean("wordTransition");
            previousCorrect = savedInstanceState.getBoolean("previousCorrect");


            // Results Variables to be Passed to the Winner Screen
            aWords = savedInstanceState.getStringArray("aWords");
            bWords = savedInstanceState.getStringArray("bWords");
            aScores1 = savedInstanceState.getIntArray("aScores1");
            aScores2 = savedInstanceState.getIntArray("aScores2");
            bScores1 = savedInstanceState.getIntArray("bScores1");
            bScores2 = savedInstanceState.getIntArray("bScores2");

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
        savedInstanceState.putBoolean("inPlay",inPlay);
        savedInstanceState.putString("teamName1",teamName1);
        savedInstanceState.putString("teamName2",teamName2);
        savedInstanceState.putString("difficulty",difficulty);
        savedInstanceState.putStringArray("wordList",wordList);

        // Ever-Changing "Current" Variables
        savedInstanceState.putInt("currRound",currRound);
        savedInstanceState.putInt("currPP",currPP);
        savedInstanceState.putBoolean("isPartnerB",isPartnerB);
        savedInstanceState.putBoolean("isTeam2",isTeam2);
        savedInstanceState.putInt("currScore1",currScore1);
        savedInstanceState.putInt("currScore2",currScore2);
        savedInstanceState.putInt("currSkipCountA",currSkipCountA);
        savedInstanceState.putInt("currSkipCountB",currSkipCountB);
        savedInstanceState.putBoolean("wordTransition",wordTransition);
        savedInstanceState.putBoolean("previousCorrect", previousCorrect);

        // Results Variables to be Passed to the Winner Screen
        savedInstanceState.putStringArray("aWords",aWords);
        savedInstanceState.putStringArray("bWords",bWords);
        savedInstanceState.putIntArray("aScores1",aScores1);
        savedInstanceState.putIntArray("aScores2",aScores2);
        savedInstanceState.putIntArray("bScores1",bScores1);
        savedInstanceState.putIntArray("bScores2",bScores2);

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
        scoreView.setText(currScore1 + ":" + currScore2);
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
                currScore1 += currPP;
            } else {
                currScore2 += currPP;
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
            if (currScore1 > currScore2)
                winnerIntent.putExtra("winnerTeamName", teamName1);
            else if (currScore2 > currScore1)
                winnerIntent.putExtra("winnerTeamName", teamName2);

            // Attach the team names and necessary scoring values to the Winner Screen intent
            winnerIntent.putExtra("aScores1", aScores1);
            winnerIntent.putExtra("aScores2", aScores2);
            winnerIntent.putExtra("bScores1", bScores1);
            winnerIntent.putExtra("bScores2", bScores2);
            winnerIntent.putExtra("aWords", aWords);
            winnerIntent.putExtra("bWords", bWords);
            winnerIntent.putExtra("score1", currScore1);
            winnerIntent.putExtra("score2", currScore2);
            winnerIntent.putExtra("teamName1", teamName1);
            winnerIntent.putExtra("teamName2", teamName2);
            winnerIntent.putExtra("difficulty", difficulty);

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
        Intent restartIntent = new Intent(this, BeginActivity.class);

        restartIntent.putExtra("teamName1", teamName1);
        restartIntent.putExtra("teamName2", teamName2);
        restartIntent.putExtra("difficulty", difficulty);

        startActivity(restartIntent);
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
