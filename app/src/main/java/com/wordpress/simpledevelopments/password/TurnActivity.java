package com.wordpress.simpledevelopments.password;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Main activity which is displayed during gameplay.
 * The display shows the word to be guessed, the current score, and the number of the round.
 * By Connor Reeder
 */
public class TurnActivity extends AppCompatActivity implements OneDirectionViewPager.SwipeController, View.OnTouchListener, MenuFragment.MenuActionsHandler, TextPagerAdapter.OnReadyListener, TimerPie.TimerListener, DownloadFragment.OnDownloadCompleteListener {

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
    private boolean previousCorrect;
    private GameState gameState;

    // Results Variables to be Passed to the Winner Screen
    private String[] aWords;
    private String[] bWords;
    private int[] aScores1;
    private int[] aScores2;
    private int[] bScores1;
    private int[] bScores2;

    private DownloadFragment downloadFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }

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
        viewPager.setOnTouchListener(this);

        // If the game is started for the first time
        if (savedInstanceState == null) {
            // Init Game Values
            teamName1 = parentIntent.getStringExtra(GK.TEAM_NAME_1);
            teamName2 = parentIntent.getStringExtra(GK.TEAM_NAME_2);
            difficulty = parentIntent.getStringExtra(GK.DIFFICULTY);
            language = parentIntent.getStringExtra(GK.LANGUAGE);
            currRound = 1;
            currPP = 10;
            isPartnerB = false;
            isTeam2 = false;
            totalScore1 = 0;
            totalScore2 = 0;
            currSkipCountA = 0;
            currSkipCountB = 0;
            gameState = GameState.AWAITING_WORDS;

            // Init Results Variables
            aWords = new String[NUM_ROUNDS];
            bWords = new String[NUM_ROUNDS];
            aScores1 = new int[NUM_ROUNDS];
            aScores2 = new int[NUM_ROUNDS];
            bScores1 = new int[NUM_ROUNDS];
            bScores2 = new int[NUM_ROUNDS];

            // Create Fragment Here
            Bundle fragmentBundle = new Bundle();
            fragmentBundle.putString(GK.LANGUAGE, language);
            fragmentBundle.putString(GK.DIFFICULTY, difficulty);

            // Create DownloadFragment and start it.
            FragmentManager fm = getFragmentManager();
            downloadFragment = (DownloadFragment) fm.findFragmentByTag(GK.DOWNLOAD_FRAGMENT);

            if (downloadFragment != null) {
                Log.e(TAG, "Download Fragment already exists!");
            } else {
                downloadFragment = new DownloadFragment();
                downloadFragment.setArguments(fragmentBundle);
                fm.beginTransaction().add(downloadFragment, GK.DOWNLOAD_FRAGMENT).commit();
            }

        } else { //if savedInstanceState != null  -----> We are RE-starting our activity

            // Values Constant for the Entirety of one Game
            teamName1 = savedInstanceState.getString(GK.TEAM_NAME_1);
            teamName2 = savedInstanceState.getString(GK.TEAM_NAME_2);
            difficulty = savedInstanceState.getString(GK.DIFFICULTY);
            language = savedInstanceState.getString(GK.LANGUAGE);
            wordList = savedInstanceState.getStringArray(GK.WORD_LIST);

            // Ever-Changing "Current" Variables
            currRound = savedInstanceState.getInt(GK.CURR_ROUND);
            currPP = savedInstanceState.getInt(GK.CURR_PP);
            isPartnerB = savedInstanceState.getBoolean(GK.IS_PARTNER_B);
            isTeam2 = savedInstanceState.getBoolean(GK.IS_TEAM_2);
            totalScore1 = savedInstanceState.getInt(GK.CURR_SCORE_1);
            totalScore2 = savedInstanceState.getInt(GK.CURR_SCORE_2);
            currSkipCountA = savedInstanceState.getInt(GK.CURR_SKIP_COUNT_A);
            currSkipCountB = savedInstanceState.getInt(GK.CURR_SKIP_COUNT_B);
            previousCorrect = savedInstanceState.getBoolean(GK.PREVIOUS_CORRECT);
            gameState = (GameState) savedInstanceState.getSerializable(GK.GAME_STATE);

            Log.v(TAG, "Game state on restart: " + gameState);

            // Results Variables to be Passed to the Winner Screen
            aWords = savedInstanceState.getStringArray(GK.A_WORDS);
            bWords = savedInstanceState.getStringArray(GK.B_WORDS);
            aScores1 = savedInstanceState.getIntArray(GK.A_SCORES_1);
            aScores2 = savedInstanceState.getIntArray(GK.A_SCORES_2);
            bScores1 = savedInstanceState.getIntArray(GK.B_SCORES_1);
            bScores2 = savedInstanceState.getIntArray(GK.B_SCORES_2);


            FragmentManager fm = getFragmentManager();
            downloadFragment = (DownloadFragment) fm.findFragmentByTag(GK.DOWNLOAD_FRAGMENT);

            if (downloadFragment == null) {
                Log.e(TAG, "Download Fragment doesn't exist!");
            }

            if (downloadFragment.isComplete()) {
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
                } else if (gameState == GameState.WORD_APPROVAL) {
                    acceptWordButton.setVisibility(View.VISIBLE);
                }
            } else {
                Log.d(TAG, "Activity Restarted but works not ready yet!");
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
        savedInstanceState.putString(GK.TEAM_NAME_1,teamName1);
        savedInstanceState.putString(GK.TEAM_NAME_2,teamName2);
        savedInstanceState.putString(GK.DIFFICULTY,difficulty);
        savedInstanceState.putString(GK.LANGUAGE,language);
        savedInstanceState.putStringArray(GK.WORD_LIST,wordList);

        // Ever-Changing "Current" Variables
        savedInstanceState.putInt(GK.CURR_ROUND,currRound);
        savedInstanceState.putInt(GK.CURR_PP,currPP);
        savedInstanceState.putBoolean(GK.IS_PARTNER_B,isPartnerB);
        savedInstanceState.putBoolean(GK.IS_TEAM_2,isTeam2);
        savedInstanceState.putInt(GK.CURR_SCORE_1, totalScore1);
        savedInstanceState.putInt(GK.CURR_SCORE_2, totalScore2);
        savedInstanceState.putInt(GK.CURR_SKIP_COUNT_A,currSkipCountA);
        savedInstanceState.putInt(GK.CURR_SKIP_COUNT_B,currSkipCountB);
        savedInstanceState.putBoolean(GK.PREVIOUS_CORRECT, previousCorrect);
        savedInstanceState.putSerializable(GK.GAME_STATE, gameState);

        // Results Variables to be Passed to the Winner Screen
        savedInstanceState.putStringArray(GK.A_WORDS,aWords);
        savedInstanceState.putStringArray(GK.B_WORDS,bWords);
        savedInstanceState.putIntArray(GK.A_SCORES_1,aScores1);
        savedInstanceState.putIntArray(GK.A_SCORES_2,aScores2);
        savedInstanceState.putIntArray(GK.B_SCORES_1,bScores1);
        savedInstanceState.putIntArray(GK.B_SCORES_2,bScores2);

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
     * Called by DownloadFragment when finished downloading words.
     */
    @Override
    public void onDownloadComplete(String result) {
        Log.d(TAG, "onDownloadComplete");
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
            approveNextWord();
            //Game has now begun
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Contents of Response: ");
            Log.e(TAG, result);
        }
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
        roundView.setText(String.format("%c%s",'#',Integer.toString(currRound)));
        scoreView.setText(Integer.toString(totalScore1) + ':' + Integer.toString(totalScore2));
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
            gameState = GameState.GAME_OVER;

            // Create Winner Screen intent
            Intent winnerIntent = new Intent(this, WinnerActivity.class);

            // Determine who the winner is based off the final scores
            if (totalScore1 > totalScore2)
                winnerIntent.putExtra(GK.WINNER_TEAM_NAME, teamName1);
            else if (totalScore2 > totalScore1)
                winnerIntent.putExtra(GK.WINNER_TEAM_NAME, teamName2);

            // Attach the team names and necessary scoring values to the Winner Screen intent
            winnerIntent.putExtra(GK.A_SCORES_1, aScores1);
            winnerIntent.putExtra(GK.A_SCORES_2, aScores2);
            winnerIntent.putExtra(GK.B_SCORES_1, bScores1);
            winnerIntent.putExtra(GK.B_SCORES_2, bScores2);
            winnerIntent.putExtra(GK.A_WORDS, aWords);
            winnerIntent.putExtra(GK.B_WORDS, bWords);
            winnerIntent.putExtra(GK.TOTAL_SCORE_1, totalScore1);
            winnerIntent.putExtra(GK.TOTAL_SCORE_2, totalScore2);
            winnerIntent.putExtra(GK.TEAM_NAME_1, teamName1);
            winnerIntent.putExtra(GK.TEAM_NAME_2, teamName2);
            winnerIntent.putExtra(GK.DIFFICULTY, difficulty);
            winnerIntent.putExtra(GK.LANGUAGE, language);

            //Launch Winner Activity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivity(winnerIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            } else {
                startActivity(winnerIntent);
            }
            finish();
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
            approveNextWord();
        }

    }

        /**
         * Setup so that we cannot return to TurnActivity from WinnerActivity
         * @param requestCode The integer request code originally supplied to startActivityForResult(),
         *                    allowing you to identify who this result came from.
         * @param resultCode The integer result code returned by the child activity through its setResult().
         * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
         */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        finish();
//    }

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
     * Once the game is in this mode, the background color of the word-swiper indicates whether the word was successfully guessed or not
     * and the screen must be tapped for the game to advance.
     * This mode is used when handing the phone to the other set of opposing players
     * @param success Whether or not the guess made was correct.
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
        boolean inApprovalState = (gameState == GameState.WORD_APPROVAL);
        Log.v(TAG, "canSwipe: " + inApprovalState);
        if (!inApprovalState) {
            return false;
        }
        // Returns whether or not the current word can be skipped.
        if (isPartnerB) {
            return (currPP == 10) && currSkipCountB < 5;
        } else {
            return (currPP == 10) && currSkipCountA < 5;
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
        supportFinishAfterTransition();
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
