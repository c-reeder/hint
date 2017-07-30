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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class TurnActivity extends AppCompatActivity implements OneDirectionViewPager.SwipeController, View.OnTouchListener, MenuFragment.MenuActionsHandler, TextPagerAdapter.OnReadyListener, TimerPie.TimerListener {

    private static final String TAG = "TurnActivity";
    public static final int NUM_ROUNDS = 6;

    private enum GameState {
        AWAITING_WORDS,
        WORD_APPROVAL,
        PLAYING,
        TEAM_TRANSITION,
        WORD_TRANSITION,
        GAME_OVER
    }

    // Components of the Display
    private TextView roundView;
    private TextView scoreView;
    private TenSpinner ppSpinnerView;
    private TextView partnerLetterView;
    private TextView teamNameView;
    private OneDirectionViewPager viewPager;
    private Button acceptWordButton;
    private Button continueButton;
    private TextView messageView;
    private TimerPie timerPie;
    //Word-Swiper Functionality
    private TextPagerAdapter adapter;
    private GestureDetector gestureDetector;

    // Values Constant for the Entirety of one Game
    //private boolean inPlay;
    private String teamName1;
    private String teamName2;
    private String difficulty;
    private String language;
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
    //private boolean wordTransition;
    private boolean previousCorrect;
    //private boolean wordAccepted;
    private GameState gameState;

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
        acceptWordButton = (Button) findViewById(R.id.acceptWordButton);
        continueButton = (Button) findViewById(R.id.continueButton);
        messageView = (TextView) findViewById(R.id.messageView);
        timerPie = (TimerPie) findViewById(R.id.timerPie);
        timerPie.setTimerListener(this);

        //Setup Tap-Action on the Word-Swyper which is used to exit the word-transition state
        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                // Single tap used to release the game from word-transition mode
//                if (wordTransition) {
//                    nextWord();
//                    acceptWordButton.setVisibility(View.VISIBLE);
//                    wordTransition = false;
//                    return true;
//                } else {
//                    return false;
//                }

//                if (gameState == GameState.WORD_TRANSITION) {
//                    nextWord();
//                    acceptWordButton.setVisibility(View.VISIBLE);
//                    approveNextWord();
//                    return true;
//                } else {
//                    return false;
//                }
                return false;
            }
        });
        viewPager.setOnTouchListener(this);

        // If the game is started for the first time
        if (savedInstanceState == null) {
            // Init Game Values
            teamName1 = parentIntent.getStringExtra(GV.TEAM_NAME_1);
            teamName2 = parentIntent.getStringExtra(GV.TEAM_NAME_2);
            difficulty = parentIntent.getStringExtra(GV.DIFFICULTY);
            language = parentIntent.getStringExtra(GV.LANGUAGE);
            currRound = 1;
            currPP = 10;
            isPartnerB = false;
            isTeam2 = false;
            totalScore1 = 0;
            totalScore2 = 0;
            currSkipCountA = 0;
            currSkipCountB = 0;
            //wordTransition = false;
            //wordAccepted = false;
            gameState = GameState.AWAITING_WORDS;

            // Init Results Variables
            aWords = new String[NUM_ROUNDS];
            bWords = new String[NUM_ROUNDS];
            aScores1 = new int[NUM_ROUNDS];
            aScores2 = new int[NUM_ROUNDS];
            bScores1 = new int[NUM_ROUNDS];
            bScores2 = new int[NUM_ROUNDS];

            // Check Network Status and if connected perform GET request to acquire word list from server
            // There should be 22 words. 2 Words * 6 Rounds + 5 Word-Skips * 2 Teams = 22 Words
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                // Define behavior to occur upon receiving the JSON word data
                JSONTask task = new JSONTask() {
                    @Override
                    protected void onPostExecute(String result) {
                        try {
                            wordList = new String[22];
                            JSONArray response = new JSONArray(result);
                            for (int i = 0; i < response.length(); i++) {
                                wordList[i] = response.getString(i);
                            }
                            if (response.length() != 22) throw new AssertionError("DID NOT GET 22 WORDS!!!");
                            //Hide Loading Icon now that Data has been received
                            ProgressBar loadingIcon = (ProgressBar) findViewById(R.id.progressBar);
                            loadingIcon.setVisibility(View.GONE);
                            initWords();
                            updateDisplay();
                            //inPlay = true;
                            approveNextWord();
                            //Game has now begun
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                            Log.e(TAG, "Contents of Response: ");
                            Log.e(TAG, result);
                        }
                    }
                };
                task.execute("https://wordvault.herokuapp.com/passwords/" + language + "/" + difficulty);
                Log.v(TAG, "URL: " + "https://wordvault.herokuapp.com/passwords/" + language + "/" + difficulty);
            } else {
                Log.e(TAG, "Not connected to network");
            }
        } else { //if savedInstanceState != null  -----> We are RE-starting our activity

            // Values Constant for the Entirety of one Game
            //inPlay = savedInstanceState.getBoolean(GV.IN_PLAY);
            teamName1 = savedInstanceState.getString(GV.TEAM_NAME_1);
            teamName2 = savedInstanceState.getString(GV.TEAM_NAME_2);
            difficulty = savedInstanceState.getString(GV.DIFFICULTY);
            language = savedInstanceState.getString(GV.LANGUAGE);
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
            //wordTransition = savedInstanceState.getBoolean(GV.WORD_TRANSITION);
            previousCorrect = savedInstanceState.getBoolean(GV.PREVIOUS_CORRECT);
            //wordAccepted = savedInstanceState.getBoolean(GV.WORD_ACCEPTED);
            gameState = (GameState) savedInstanceState.getSerializable(GV.GAME_STATE);

            Log.v(TAG, "Game state on restart: " + gameState);

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
            if (gameState == GameState.TEAM_TRANSITION) {
                promptForContinue(getString(R.string.pass_phone_next));
            } else if (gameState == GameState.WORD_TRANSITION) {
                promptForContinue(getString(R.string.pass_phone_across));
            }
            // Game has been successfully restarted
        }
    }

    private void approveNextWord() {
        gameState = GameState.WORD_APPROVAL;
        acceptWordButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Values Constant for the Entirety of one Game
        //savedInstanceState.putBoolean(GV.IN_PLAY,inPlay);
        savedInstanceState.putString(GV.TEAM_NAME_1,teamName1);
        savedInstanceState.putString(GV.TEAM_NAME_2,teamName2);
        savedInstanceState.putString(GV.DIFFICULTY,difficulty);
        savedInstanceState.putString(GV.LANGUAGE,language);
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
        //savedInstanceState.putBoolean(GV.WORD_TRANSITION,wordTransition);
        savedInstanceState.putBoolean(GV.PREVIOUS_CORRECT, previousCorrect);
        //savedInstanceState.putBoolean(GV.WORD_ACCEPTED, wordAccepted);
        savedInstanceState.putSerializable(GV.GAME_STATE, gameState);

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
     * Callback Method implementing the OneDirectionViewPager which is called upon a swipe being performed.
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
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
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

    public void onAcceptWord(View view) {
        assertEquals(gameState, GameState.WORD_APPROVAL);
        Log.v(TAG, "Word Accepted");
        //wordAccepted = true;
        acceptWordButton.setVisibility(View.GONE);
        startPlaying();
    }

    private void startPlaying() {
        gameState = GameState.PLAYING;
        timerPie.setVisibility(View.VISIBLE);
        timerPie.startTimer();
    }

    /**
     * The method called when a guess has been made
     * @param view The button pressed to signal that a guess has been made
     */
    public void guessMade(View view) {
        // Do nothing if a guess has already been made but we have not advanced
        // or the game is not currently in play
        // or the word has not been accepted
//        if (wordTransition || !inPlay || !wordAccepted)
//            return;

        if (gameState != GameState.PLAYING) {
            Log.e(TAG, "Guess made while not playing!");
            return;
        }

        // Get rid of the timer and reset it for next time we use it.
        timerPie.setVisibility(View.GONE);
        timerPie.resetTimer();

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
                transitionToNextTeam();
            }
        }

        // Check if end of game
        if (currRound > NUM_ROUNDS) {
            // Game is Over
            //inPlay = false;
            gameState = GameState.GAME_OVER;

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
            winnerIntent.putExtra(GV.LANGUAGE, language);

            //Launch Winner Activity
            startActivityForResult(winnerIntent,0);
        } else { // If not the end of the game
            updateDisplay();
            if (gameState == GameState.TEAM_TRANSITION) {
                promptForContinue(getString(R.string.pass_phone_next));
            } else if (gameState == GameState.WORD_TRANSITION) {
                promptForContinue(getString(R.string.pass_phone_across));
            }
        }
    }

    private void transitionToNextTeam() {
        gameState = GameState.TEAM_TRANSITION;
    }

    private void promptForContinue(String message) {
        Log.v(TAG, "Here is where we would prompt for continue!");
        messageView.setText(message);
        messageView.setVisibility(View.VISIBLE);
        continueButton.setVisibility(View.VISIBLE);
    }

    public void onContinue(View view) {
        Log.v(TAG, "We have continued in this state: " + gameState);
        assertTrue(gameState == GameState.TEAM_TRANSITION || gameState == GameState.WORD_TRANSITION);
        messageView.setVisibility(View.GONE);
        continueButton.setVisibility(View.GONE);

        if (gameState == GameState.TEAM_TRANSITION) {
            // Next Team guesses
            startPlaying();
        } else if (gameState == GameState.WORD_TRANSITION) {
            // The other two players now start giving hints and a new word is approved
            nextWord();
            //acceptWordButton.setVisibility(View.VISIBLE);
            approveNextWord();
        }

    }

        /**
         * Setup so that we cannot return to TurnActivity from WinnerActivity
         * @param requestCode
         * @param resultCode
         * @param data
         */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
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
        //wordTransition = true;
        //wordAccepted = false;
        gameState = GameState.WORD_TRANSITION;
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
//        Log.d(TAG, "canSwipe: " + !wordTransition);
//        if (wordTransition) {
//            return false;
//        }
        boolean inApprovalState = (gameState == GameState.WORD_APPROVAL);
        Log.v(TAG, "canSwipe: " + inApprovalState);
        if (!inApprovalState) {
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
        if (gameState == GameState.AWAITING_WORDS) {
            Log.d(TAG, "Trying to pause in Awaiting Words Mode");
            return;
        }

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
//        if (wordTransition) {
//            if (previousCorrect)
//                adapter.getCurrentView().setBackgroundColor(Color.GREEN);
//            else
//                adapter.getCurrentView().setBackgroundColor(Color.RED);
//        }
        if (gameState == GameState.WORD_TRANSITION) {
            if (previousCorrect)
                adapter.getCurrentView().setBackgroundColor(Color.GREEN);
            else
                adapter.getCurrentView().setBackgroundColor(Color.RED);
        }
    }

    /**
     *
     * Called when the TimerPie finishes its countdown
     *
     */
    @Override
    public void onTimerComplete() {
        Log.v(TAG, "Timer Complete!");
        // Simulate a Incorrect Button press
        Button incorrectButton = (Button) findViewById(R.id.failureButton);
        guessMade(incorrectButton);
    }
}
