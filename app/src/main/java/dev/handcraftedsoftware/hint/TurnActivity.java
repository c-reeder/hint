package dev.handcraftedsoftware.hint;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import androidx.transition.Transition;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import dev.handcraftedsoftware.hint.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;


/**
 * Main activity which is displayed during gameplay.
 * The display shows the word to be guessed, the current score, and the number of the round.
 * By Connor Reeder
 */
public class TurnActivity extends AppCompatActivity implements OneDirectionViewPager.SwipeController, View.OnTouchListener, MenuFragment.MenuActionsHandler, DownloadFragment.OnDownloadCompleteListener {

    private static final String TAG = "TurnActivity";
    public static final int NUM_ROUNDS = 6;

    // Enum representing different states of the Game
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
    private TextView timerView;
    private TextView wordHolder;
    private View wordCover;
    private ConstraintLayout layout;

    //Word-Swiper Functionality
    private TextPagerAdapter adapter;
    ProgressBar loadingIcon;

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
    private int wordIdx;
    private boolean isWordHidden;

    // Results Variables to be Passed to the Winner Screen
    private String[] aWords;
    private String[] bWords;
    private int[] aScores1;
    private int[] aScores2;
    private int[] bScores1;
    private int[] bScores2;

    private DownloadFragment downloadFragment;

    // CountDown used for the game timer
    private CountDownTimer countDownTimer;
    private long countDownTimeRemaining;
    private ObjectAnimator coverAlphaAnimator;



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
        roundView = findViewById(R.id.roundText);
        roundView.setTextSize(25);
        scoreView = findViewById(R.id.scoreText);
        ppSpinnerView = findViewById(R.id.ppSpinner);
        partnerLetterView = findViewById(R.id.partnerLetterText);
        teamNameView = findViewById(R.id.teamName);
        viewPager = findViewById(R.id.pager);
        viewPager.setSwipeController(this);
        acceptWordButton = findViewById(R.id.acceptWordButton);
        continueButton = findViewById(R.id.continueButton);
        messageView = findViewById(R.id.messageView);
        timerView = findViewById(R.id.timerView);
        wordHolder = findViewById(R.id.wordHolder);
        wordCover = findViewById(R.id.wordCover);
        loadingIcon = findViewById(R.id.progressBar);
        layout = findViewById(R.id.activity_turn);
        viewPager.setOnTouchListener(this);

        coverAlphaAnimator = new ObjectAnimator();
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (gameState == GameState.PLAYING || gameState == GameState.TEAM_TRANSITION) {
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        Log.d(TAG, "layout ACTION_DOWN");
                        isWordHidden = false;
                        //coverAlphaAnimator.cancel();
                        if (coverAlphaAnimator.isRunning()) {
                            coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",(float)coverAlphaAnimator.getAnimatedValue(),0f);
                        } else {
                            coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",1f,0f);
                        }
                        coverAlphaAnimator.setDuration(500);
                        coverAlphaAnimator.start();
                        return true;
                    } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                        Log.d(TAG, "layout ACTION_UP");
                        isWordHidden = true;
                        //coverAlphaAnimator.cancel();
                        if (coverAlphaAnimator.isRunning()) {
                            coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",(float)coverAlphaAnimator.getAnimatedValue(),1f);
                        } else {
                            coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",0f,1f);
                        }
                        coverAlphaAnimator.setDuration(500);
                        coverAlphaAnimator.start();
                        return true;
                    }
                }
                return false;
            }
        });



        // If the game is started for the first time
        if (savedInstanceState == null) {
//            Log.d(TAG, "From scratch");
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
            wordIdx = 0;
            isWordHidden = false;

            // Init Results Variables
            aWords = new String[NUM_ROUNDS];
            bWords = new String[NUM_ROUNDS];
            aScores1 = new int[NUM_ROUNDS];
            aScores2 = new int[NUM_ROUNDS];
            bScores1 = new int[NUM_ROUNDS];
            bScores2 = new int[NUM_ROUNDS];

            // Bundle the information sent to the Download Fragment
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
//            Log.d(TAG, "Restart");

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
            wordIdx = savedInstanceState.getInt(GK.WORD_IDX);
            countDownTimeRemaining = savedInstanceState.getLong(GK.TIME_REMAINING);
            isWordHidden = savedInstanceState.getBoolean(GK.WORD_HIDDEN);

            Log.v(TAG, "Game state on restart: " + gameState);

            // Results Variables to be Passed to the Winner Screen
            aWords = savedInstanceState.getStringArray(GK.A_WORDS);
            bWords = savedInstanceState.getStringArray(GK.B_WORDS);
            aScores1 = savedInstanceState.getIntArray(GK.A_SCORES_1);
            aScores2 = savedInstanceState.getIntArray(GK.A_SCORES_2);
            bScores1 = savedInstanceState.getIntArray(GK.B_SCORES_1);
            bScores2 = savedInstanceState.getIntArray(GK.B_SCORES_2);

            // Recover Download Fragment
            FragmentManager fm = getFragmentManager();
            downloadFragment = (DownloadFragment) fm.findFragmentByTag(GK.DOWNLOAD_FRAGMENT);

            if (downloadFragment == null) {
                Log.e(TAG, "Download Fragment doesn't exist!");
            }

            if (downloadFragment.isComplete()) {
                // Hide the loading icon IMMEDIATELY since we are only re-starting the activity and have already obtained our word data
                loadingIcon.setVisibility(View.INVISIBLE);
                initWords();
                ppSpinnerView.setSpinner(currPP);
                updateDisplay();
                if (gameState == GameState.TEAM_TRANSITION) {
                    promptForContinue(getString(R.string.pass_phone_next));

                    if (isWordHidden)
                        wordCover.setAlpha(1f);


                    wordHolder.setText(wordList[wordIdx]);
                    viewPager.setVisibility(View.INVISIBLE);


                    // Position wordHolder between the continueButton and the messageView
                    ConstraintSet newSet = new ConstraintSet();
                    newSet.clear(R.id.wordHolder);
                    newSet.constrainHeight(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
                    newSet.constrainWidth(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
                    newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.messageView, ConstraintSet.BOTTOM,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.continueButton, ConstraintSet.TOP,0);
                    newSet.applyTo(layout);
                } else if (gameState == GameState.WORD_TRANSITION) {
                    promptForContinue(getString(R.string.pass_phone_across));
                    if (previousCorrect) {
                        layout.setBackgroundColor(Color.GREEN);

                    } else {
                        layout.setBackgroundColor(Color.RED);
                    }
                    Log.v(TAG, "Restarting in Word Transition!");
                    wordHolder.setText(wordList[wordIdx]);
                    viewPager.setVisibility(View.INVISIBLE);

                    // Position wordHolder between the continueButton and the messageView
                    ConstraintSet newSet = new ConstraintSet();
                    newSet.clear(R.id.wordHolder);
                    newSet.constrainHeight(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
                    newSet.constrainWidth(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
                    newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.messageView, ConstraintSet.BOTTOM,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.continueButton, ConstraintSet.TOP,0);
                    newSet.applyTo(layout);
                } else if (gameState == GameState.WORD_APPROVAL) {
                    acceptWordButton.setVisibility(View.VISIBLE);
                } else if (gameState == GameState.PLAYING) {

                    // Initialize the CountDownTimer from where it was before we stopped
                    countDownTimer = new CountDownTimer(countDownTimeRemaining,1000) {
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onTick(long millisUntilFinished) {
                            countDownTimeRemaining = millisUntilFinished;
                            timerView.setText(String.format("%02d",Math.round(millisUntilFinished / 1000)));
                        }

                        @Override
                        public void onFinish() {
                            onCountDownCompleted();
                        }
                    };
                    countDownTimer.start();

                    if (isWordHidden)
                        wordCover.setAlpha(1f);

                    // Correctly restore position of the WordHolder vertically between the timerView and the buttonrow
                    ConstraintSet newSet = new ConstraintSet();
                    wordHolder.setText(wordList[wordIdx]);
                    viewPager.setVisibility(View.INVISIBLE);
                    newSet.clear(R.id.wordHolder);
                    newSet.constrainHeight(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
                    newSet.constrainWidth(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
                    newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.timerView, ConstraintSet.BOTTOM,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.buttonRow, ConstraintSet.TOP,0);
                    newSet.applyTo(layout);
                    timerView.setText(String.format("%02d",Math.round(countDownTimeRemaining / 1000)));
                    timerView.setVisibility(View.VISIBLE);
                }
            } else {
                Log.d(TAG, "Activity Restarted but words not ready yet!");
            }


            // Game has been successfully restarted
        }
    }

    // State transition into Approval Mode
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
        savedInstanceState.putSerializable(GK.WORD_IDX, wordIdx);
        savedInstanceState.putLong(GK.TIME_REMAINING, countDownTimeRemaining);
        savedInstanceState.putBoolean(GK.WORD_HIDDEN, isWordHidden);

        // Results Variables to be Passed to the Winner Screen
        savedInstanceState.putStringArray(GK.A_WORDS,aWords);
        savedInstanceState.putStringArray(GK.B_WORDS,bWords);
        savedInstanceState.putIntArray(GK.A_SCORES_1,aScores1);
        savedInstanceState.putIntArray(GK.A_SCORES_2,aScores2);
        savedInstanceState.putIntArray(GK.B_SCORES_1,bScores1);
        savedInstanceState.putIntArray(GK.B_SCORES_2,bScores2);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /**
     * Initialize the Word Viewpager (for swiping/skipping through words)
     * This method sets up the word-slider by creating the adapter for the word list
     */
    public void initWords() {
        adapter = new TextPagerAdapter(this, wordList);
        viewPager.setAdapter(adapter);
    }

    /**
     * Called by DownloadFragment when finished downloading words.
     */
    @Override
    public void onDownloadComplete(String result) {
        Log.v(TAG, "onDownloadComplete");
        try {
            wordList = new String[22];
            JSONArray response = new JSONArray(result);
            for (int i = 0; i < response.length(); i++) {
                wordList[i] = response.getString(i);
            }
            if (response.length() != 22) throw new AssertionError("DID NOT GET 22 WORDS!!!");
            //Hide Loading Icon now that Data has been received
            loadingIcon = findViewById(R.id.progressBar);
            loadingIcon.setVisibility(View.INVISIBLE);
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
        Log.v(TAG, "onSwiped: " + wordIdx + "->" + newIndex);
        wordIdx = newIndex;
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
    @SuppressLint("SetTextI18n")
    private void updateDisplay() {
        roundView.setText(String.format("%c%s",'#',Integer.toString(currRound)));
        scoreView.setText(Integer.toString(totalScore1) + ':' + totalScore2);
        if(!isPartnerB)
            partnerLetterView.setText("A");
        else
            partnerLetterView.setText("B");
        if(!isTeam2)
            teamNameView.setText(teamName1);
        else
            teamNameView.setText(teamName2);
    }

    // State transition called when the AcceptButton is pressed
    public void onAcceptWord(View view) {
        Log.v(TAG, "Word Accepted");
        acceptWordButton.setVisibility(View.INVISIBLE);
        startPlaying();
    }

    // State transition to the actual guessing/playing stage
    private void startPlaying() {
        gameState = GameState.PLAYING;

        wordHolder.setText(wordList[viewPager.getCurrentItem()]);
        viewPager.setVisibility(View.INVISIBLE);
        wordHolder.setVisibility(View.VISIBLE);

        layout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                layout.removeOnLayoutChangeListener(this);



                Transition transition = new AutoTransition();
                transition.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(@NonNull Transition transition) {
                        isWordHidden = true;
                    }

                    @Override
                    public void onTransitionEnd(@NonNull Transition transition) {
                        if (wordCover.getAlpha() != 1f) {
                            coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",0f,1f);
                            coverAlphaAnimator.setDuration(500);
                            coverAlphaAnimator.setStartDelay(1000);
                            coverAlphaAnimator.start();
                        }
                    }

                    @Override
                    public void onTransitionCancel(@NonNull Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(@NonNull Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(@NonNull Transition transition) {

                    }
                });

                // Animate WordHolder from Center Position to Low Position (beneath the timerView)
                ConstraintSet newSet = new ConstraintSet();
                newSet.clear(R.id.wordHolder);
                newSet.constrainHeight(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
                newSet.constrainWidth(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
                newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.timerView, ConstraintSet.BOTTOM,0);
                newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
                newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.buttonRow, ConstraintSet.TOP,0);
                TransitionManager.beginDelayedTransition(layout, transition);
                newSet.applyTo(layout);
            }
        });

        // Initialize the timer clock
        timerView.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(31000,1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                countDownTimeRemaining = millisUntilFinished;
                timerView.setText(String.format("%02d",Math.round(millisUntilFinished / 1000)));
            }

            @Override
            public void onFinish() {
                onCountDownCompleted();
            }
        };
        timerView.setText(String.format("%02d",Math.round(countDownTimeRemaining / 1000)));
        countDownTimer.start();
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
        timerView.setVisibility(View.INVISIBLE);
        countDownTimer.cancel();

        // If the guess was correct
        if (view.getId() == R.id.successButton) {
            storeResult();


            coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",1f,0f);
            coverAlphaAnimator.setDuration(500);
            coverAlphaAnimator.start();

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


                coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",1f,0f);
                coverAlphaAnimator.setDuration(500);
                coverAlphaAnimator.start();

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

            // Print Intent Goodies
            Log.d(TAG, "aScores1: " + Arrays.toString(aScores1));
            Log.d(TAG, "aScores2: " + Arrays.toString(aScores2));
            Log.d(TAG, "bScores1: " + Arrays.toString(bScores1));
            Log.d(TAG, "bScores2: " + Arrays.toString(bScores2));
            Log.d(TAG, "aWords: " + Arrays.toString(aWords));
            Log.d(TAG, "bWords: " + Arrays.toString(bWords));
            Log.d(TAG, "totalScore1: " + totalScore1);
            Log.d(TAG, "totalScore2 " + totalScore2);
            Log.d(TAG, "teamName1: " + teamName1);
            Log.d(TAG, "teamName2: " + teamName2);
            Log.d(TAG, "difficulty: " + difficulty);
            Log.d(TAG, "language: " + language);

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

            ConstraintSet newSet = new ConstraintSet();
            newSet.clear(R.id.wordHolder);
            newSet.constrainHeight(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
            newSet.constrainWidth(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
            newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.messageView, ConstraintSet.BOTTOM,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.continueButton, ConstraintSet.TOP,0);
            TransitionManager.beginDelayedTransition(layout);
            newSet.applyTo(layout);
            loadingIcon.setVisibility(View.INVISIBLE);
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

    //

    /**
     * Called upon pressing the continue button.
     * @param view the continue button
     */
    public void onContinue(View view) {
        Log.v(TAG, "We have continued in this state: " + gameState);
        messageView.setVisibility(View.INVISIBLE);
        continueButton.setVisibility(View.INVISIBLE);

        if (gameState == GameState.TEAM_TRANSITION) {
            // Next Team guesses
            startPlaying();
        } else if (gameState == GameState.WORD_TRANSITION) {
            // The other two players now start giving hints and a new word is approved

            layout.setBackgroundColor(Color.WHITE);

            Transition transition = new AutoTransition();
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(@NonNull Transition transition) {

                }

                @Override
                public void onTransitionEnd(@NonNull Transition transition) {
                    TextView wH = findViewById(R.id.wordHolder);
                    wH.setVisibility(View.INVISIBLE);
                    viewPager.setVisibility(View.VISIBLE);

                    nextWord();
                    approveNextWord();
                }

                @Override
                public void onTransitionCancel(@NonNull Transition transition) {

                }

                @Override
                public void onTransitionPause(@NonNull Transition transition) {

                }

                @Override
                public void onTransitionResume(@NonNull Transition transition) {

                }
            });

            // Animation WordHolder from Low Position to Normal Position
            ConstraintSet newSet = new ConstraintSet();
            newSet.clear(R.id.wordHolder);
            newSet.constrainHeight(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
            newSet.constrainWidth(R.id.wordHolder, ConstraintSet.WRAP_CONTENT);
            newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.topBar, ConstraintSet.BOTTOM,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.buttonRow, ConstraintSet.TOP,0);
            TransitionManager.beginDelayedTransition(layout, transition);
            newSet.applyTo(layout);
            loadingIcon.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * Helper method to be called every time is word is completed
     * Updates the result variables based on who successfully guessed the word and how many points
     *  they earned.
     */
    private void storeResult() {
        TextView currentView = adapter.getCurrentView().findViewById(R.id.singleTextView);
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
        if (success) {
            layout.setBackgroundColor(Color.GREEN);

        } else {
            layout.setBackgroundColor(Color.RED);
        }
        gameState = GameState.WORD_TRANSITION;
    }

    /**
     * Advances the word-swiper to the next word
     */
    private void nextWord() {
        Log.v(TAG, "nextWord");
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        wordIdx++;
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

    /**
     * Called when the pause button is pressed.
     * In reality this only gives you the ability to return to the start screen
     * @param view
     */
    public void pauseGame(View view) {
        if (gameState == GameState.AWAITING_WORDS) {
            Log.v(TAG, "Trying to pause in Awaiting Words Mode");
            return;
        }

        MenuFragment menuFragment = new MenuFragment();

        TextView wordText = viewPager.findViewById(R.id.singleTextView);
        wordText.setVisibility(View.INVISIBLE);
        menuFragment.show(getSupportFragmentManager(), "MENU_FRAGMENT");
    }

    @Override
    public void restartGame() {
        supportFinishAfterTransition();
    }

    @Override
    public void resumeGame() {
        TextView wordText = viewPager.findViewById(R.id.singleTextView);
        wordText.setVisibility(View.VISIBLE);
    }

    /**
     *
     * Called when the CountdownTimer finishes its countdown
     *
     */
    public void onCountDownCompleted() {
        Log.v(TAG, "Timer Complete!");
        // Simulate a Incorrect Button press
        Button incorrectButton = findViewById(R.id.failureButton);
        guessMade(incorrectButton);
    }
}
