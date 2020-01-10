package dev.handcraftedsoftware.hint;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.transition.Explode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.TextViewCompat;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import java.util.Arrays;
import java.util.Locale;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;



/**
 * Main activity which is displayed during gameplay.
 * The display shows the word to be guessed, the current score, and the number of the round.
 * By Connor Reeder
 */
public class TutorialActivity extends AppCompatActivity {

    private static final String TAG = "TurnActivity";
    private static final int NUM_ROUNDS = 6;


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
    private ProgressBar loadingIcon;

    // Values Constant for the Entirety of one Game
    //private boolean inPlay;
    private String teamName1;
    private String teamName2;

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

    // CountDown used for the game timer
    private CountDownTimer countDownTimer;
    private long countDownTimeRemaining;
    private ObjectAnimator coverAlphaAnimator;




    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }

        setContentView(R.layout.activity_turn);

        boolean firstRun = true;
        if (firstRun) {
            showTutorial();
        }


        Intent parentIntent = getIntent();

        // Setup Display
        roundView = findViewById(R.id.roundText);
        roundView.setTextSize(25);
        scoreView = findViewById(R.id.scoreText);
        ppSpinnerView = findViewById(R.id.ppSpinner);
        partnerLetterView = findViewById(R.id.partnerLetterText);
        teamNameView = findViewById(R.id.teamName);
        viewPager = findViewById(R.id.pager);
        acceptWordButton = findViewById(R.id.acceptWordButton);
        continueButton = findViewById(R.id.continueButton);
        messageView = findViewById(R.id.messageView);
        timerView = findViewById(R.id.timerView);
        wordHolder = findViewById(R.id.wordHolder);
        wordCover = findViewById(R.id.wordCover);
        loadingIcon = findViewById(R.id.progressBar);
        layout = findViewById(R.id.activity_turn);


        // Init Game Values
        teamName1 = getString(R.string.team1);
        teamName2 = getString(R.string.team2);
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

        // Show brief description of what the game is about

        // ------Display overview
        // Show Round number
        // Show Score
        // Show Possible Points to be won
        // Show word holder
        // Show word cover and mention touch to reveal
        // Show player letter
        // Show Current playing team name

        // ------Individual turn
        // wordHolder -> this is the word you must guess
        // wordHolder -> You and your opponent may chose to swype
        // wordHolder -> only 5 skips per game
        // acceptButton -> Once you've agreed, click to accept
        // timerView -> Timer appears, time to give hint and guess
        // buttonBar -> upon guessing, mark correct or incorrect
        // timerView -> If you don't finish before the time is up, its automatically wrong!

        // ------If you're wrong
        // ppSpinner -> Spins/decreases
        // messageView -> Pass phone to the opponent next to you
        // that person clicks continue

        // ------If you're right
        // score -> updates
        // ppSpinner -> resets
        // messageView -> Pass phone across to the other 2 players



        loadingIcon = findViewById(R.id.progressBar);
        loadingIcon.setVisibility(View.INVISIBLE);
        findViewById(R.id.successButton).setVisibility(View.VISIBLE);
        findViewById(R.id.failureButton).setVisibility(View.VISIBLE);
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



        //promptForContinue(getString(R.string.pass_phone_next));
        //promptForContinue(getString(R.string.pass_phone_across));
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
        int value = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            value = value | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(value);
    }


    // State transition to the actual guessing/playing stage
    private void startPlaying() {
        gameState = GameState.PLAYING;

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
                newSet.constrainHeight(R.id.wordHolder, getResources().getDimensionPixelSize(R.dimen.word_height));
                newSet.constrainWidth(R.id.wordHolder, ConstraintLayout.LayoutParams.MATCH_PARENT);
                newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.timerView, ConstraintSet.BOTTOM,0);
                newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
                newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.buttonRow, ConstraintSet.TOP,0);

                Log.d(TAG, "wordHolder.getHeight()1:" + wordHolder.getHeight());
                TextViewCompat.setAutoSizeTextTypeWithDefaults(wordHolder, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

                TransitionManager.beginDelayedTransition(layout, transition);
                wordHolder.setGravity(Gravity.CENTER);
                newSet.applyTo(layout);
                Log.d(TAG, "wordHolder.getHeight()2:" + wordHolder.getHeight());
            }
        });

        // Initialize the timer clock
        timerView.setVisibility(View.VISIBLE);
        timerView.setText(String.format(Locale.getDefault(),"%02d",Math.round(countDownTimeRemaining / 1000)));
        countDownTimer.start();
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
     */
    private void onContinue() {
        Log.v(TAG, "We have continued in this state: " + gameState);
        messageView.setVisibility(View.INVISIBLE);
        continueButton.setVisibility(View.INVISIBLE);

        if (gameState == GameState.TEAM_TRANSITION) {
            // Next Team guesses
            startPlaying();
        } else if (gameState == GameState.WORD_TRANSITION) {
            // The other two players now start giving hints and a new word is approved

//            layout.setBackgroundColor(Color.WHITE);

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
            newSet.constrainHeight(R.id.wordHolder, getResources().getDimensionPixelSize(R.dimen.word_height));
            newSet.constrainWidth(R.id.wordHolder, ConstraintLayout.LayoutParams.MATCH_PARENT);
            newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.topBar, ConstraintSet.BOTTOM,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.buttonRow, ConstraintSet.TOP,0);

            TextViewCompat.setAutoSizeTextTypeWithDefaults(wordHolder, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

            TransitionManager.beginDelayedTransition(layout, transition);
            wordHolder.setGravity(Gravity.CENTER);
            newSet.applyTo(layout);
            loadingIcon.setVisibility(View.INVISIBLE);
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
//        if (success) {
//            layout.setBackgroundColor(Color.GREEN);
//
//        } else {
//            layout.setBackgroundColor(Color.RED);
//        }
        gameState = GameState.WORD_TRANSITION;
    }



    private void showTutorial() {
        View targetView1 = findViewById(R.id.ppSpinner);
        View targetView2 = findViewById(R.id.roundText);
//        new MaterialShowcaseView.Builder(this)
//                .setTarget(targetView)
//                .setContentText("This is my Connor content!")
//                .setTitleText("Connor Title!")
//                .setDismissText("Connor Done")
//                .setDelay(1000)
//                .singleUse("showcase1")
//                .show();

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);

        MaterialShowcaseSequence showcaseSequence = new MaterialShowcaseSequence(this);
        showcaseSequence.setConfig(config);

        showcaseSequence.addSequenceItem(targetView1,"My content","dismis this garbage!");
        showcaseSequence.addSequenceItem(targetView2,"My content","dismis this garbage!");
        showcaseSequence.start();

    }
}
