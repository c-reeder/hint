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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
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

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Locale;

import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.target.Target;


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
    private LinearLayout buttonRow;
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

    private Bundle extrasToForward;


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



        Intent parentIntent = getIntent();
        extrasToForward = parentIntent.getExtras();

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
        buttonRow = findViewById(R.id.buttonRow);
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

        // Start with all buttons hidden

        // ------Display overview
        // Show Round number
        // Show Score
        // Show Possible Points to be won
        // Show word holder
        // Reveal wordCover
        // Show word cover and mention touch to reveal
        // Show player letter
        // Show Current playing team name

        // Show accept Button
        // Hide wordCover

        // ------Individual turn
        // wordHolder -> this is the word you must guess
        // wordHolder -> You and your opponent may chose to swype
        // wordHolder -> only 5 skips per game
        // acceptButton -> Once you've agreed, click to accept

        // Show timer and buttonBar
        // Hide acceptbutton

        // timerView -> Timer appears, time to give hint and guess
        // buttonBar -> upon guessing, mark correct or incorrect
        // timerView -> If you don't finish before the time is up, its automatically wrong!

        // Show messageView with pass next to you message
        // Show continueButton

        // ------If you're wrong
        // ppSpinner -> Spins/decreases
        // messageView -> Pass phone to the opponent next to you
        // that person clicks continue

        // Show messageView with pass across message

        // ------If you're right
        // score -> updates
        // ppSpinner -> resets
        // messageView -> Pass phone across to the other 2 players
        // that person clicks continue



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

        boolean firstRun = true;
        if (firstRun) {
            showTutorial();
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

    private MaterialShowcaseView centerSCV(MaterialShowcaseView showcaseView) {
        TextView title = showcaseView.findViewById(R.id.tv_title);
        TextView content = showcaseView.findViewById(R.id.tv_content);
        TextView dismiss = showcaseView.findViewById(R.id.tv_dismiss);
        showcaseView.setGravity(Gravity.CENTER);
        title.setGravity(Gravity.CENTER);
        content.setGravity(Gravity.CENTER);
        dismiss.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        dismiss.setGravity(Gravity.CENTER);
        return showcaseView;
    }


    private void showTutorial() {
        acceptWordButton.setVisibility(View.INVISIBLE);
        continueButton.setVisibility(View.INVISIBLE);
        buttonRow.setVisibility(View.INVISIBLE);
        wordHolder.setVisibility(View.VISIBLE);
        wordHolder.setText(R.string.current_word);

        MaterialShowcaseView[] introSlides = new MaterialShowcaseView[3];
        MaterialShowcaseView[] overviewSlides = new MaterialShowcaseView[7];
        MaterialShowcaseView[] turnSlides = new MaterialShowcaseView[14];

        // Intro Information Slides
        introSlides[0] = new MaterialShowcaseView.Builder(this)
                .setTarget(new View(this))
                .setContentText("The object of the game is to manage to get your partner to guess an assigned word using only a single word Hint!   You may not converse with your partner about the word. You may only say your single word HINT. You can say it with special voice inflection if you want to, but that is all.")
                .setTitleText("Objective (1/3)")
                .setDismissText("Next")
                .build();

        introSlides[1] = new MaterialShowcaseView.Builder(this)
                .setTarget(new View(this))
                .setTitleText("Objective (2/3)")
                .setContentText("There are 2 teams of two people. Each team has a person A and a person B. The 2 A's sit side by side, facing their B partners.")
                .setDismissText("Next")
                .build();

        introSlides[2] = new MaterialShowcaseView.Builder(this)
                .setTarget(new View(this))
                .setTitleText("Objective (3/3)")
                .setContentText("You and the same-lettered opponent next to you will alternate trying to get your partners to guess the same word. The one who acheives it gets the points!")
                .setDismissText("Next")
                .build();

        // General breakdown of the screen
        overviewSlides[0] = new MaterialShowcaseView.Builder(this)
                .setTarget(roundView)
                .setTitleText("Round number")
                .setContentText("Here you can see what round you are currently in. There are 6 total. In each round the A's have a chance to give Hints and the B's as well!")
                .setDismissText("Next")
                .build();

        overviewSlides[1] = new MaterialShowcaseView.Builder(this)
                .setTarget(scoreView)
                .setTitleText("Current Score")
                .setContentText("Here you can see what the current score is!")
                .setDismissText("Next")
                .build();

        overviewSlides[2] = new MaterialShowcaseView.Builder(this)
                .setTarget(ppSpinnerView)
                .setTitleText("Point Spinner")
                .setContentText("This spinner tells you how many points your team will get if your partner guesses the word. With each attempt (of either opponent), the spinner decreases in value!")
                .setDismissText("Next")
                .build();

        overviewSlides[3] = new MaterialShowcaseView.Builder(this)
                .setTarget(wordHolder)
                .withRectangleShape()
                .setTitleText("Current Word")
                .setContentText("This is the word your partner must guess.")
                .setDismissText("Next")
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        wordCover.setVisibility(View.VISIBLE);
                        wordCover.setAlpha(1f);
                    }
                })
                .build();

        overviewSlides[4] = new MaterialShowcaseView.Builder(this)
                .setTarget(wordCover)
                .setTitleText("Word Cover")
                .setContentText("Only you and the opponent sitting next to you may see the word. So this cover appears to hide he word. Simply press and hold to reveal the word.")
                .setDismissText("Next")
                .withRectangleShape()
                .build();

        overviewSlides[5] = new MaterialShowcaseView.Builder(this)
                .setTarget(partnerLetterView)
                .setTitleText("Current Players")
                .setContentText("Which 2 are currently giving hints to get their partners to guess the word. (the A's or the B's).")
                .setDismissText("Next")
                .build();

        overviewSlides[6] = new MaterialShowcaseView.Builder(this)
                .setTarget(teamNameView)
                .setTitleText("Who's turn is it?")
                .setContentText("The team trying to get the points at the moment.")
                .setDismissText("Next")
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        acceptWordButton.setVisibility(View.VISIBLE);
                        wordCover.setVisibility(View.INVISIBLE);
                    }
                })
                .build();



        turnSlides[0] = new MaterialShowcaseView.Builder(this)
                .setTarget(wordHolder)
                .setTitleText("Is it even guessable??")
                .setContentText("Occasionally an impossible word may pop up. Before beginning the round, you and the opponent next to you must silently agree that the word is guessable.")
                .withRectangleShape()
                .setDismissText("Next")
                .build();

        turnSlides[1] = new MaterialShowcaseView.Builder(this)
                .setTarget(wordHolder)
                .setTitleText("Not guessable!")
                .setContentText("If you and your opponent decide the word is not guessable simply swipe it to the left.")
                .withRectangleShape()
                .setDismissText("Next")
                .build();

        turnSlides[2] = new MaterialShowcaseView.Builder(this)
                .setTarget(wordHolder)
                .setTitleText("Swipe Limit")
                .setContentText("Feel free to swipe, but be aware that you and your opponent only have 5 skips per game!")
                .setDismissText("Next")
                .withRectangleShape()
                .build();

        turnSlides[3] = new MaterialShowcaseView.Builder(this)
                .setTarget(acceptWordButton)
                .withRectangleShape()
                .setTitleText("Accept Button")
                .setContentText("Once the two of you decide the word is guessable, you begin the round by clicking here!")
                .setDismissText("Next")
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        timerView.setVisibility(View.VISIBLE);
                        acceptWordButton.setVisibility(View.INVISIBLE);
                        buttonRow.setVisibility(View.VISIBLE);
                    }
                })
                .build();


        turnSlides[4] = new MaterialShowcaseView.Builder(this)
                .setTarget(timerView)
                .setTitleText("Timer")
                .setContentText("You have 30 seconds in total to give your one word hint and for your partner to guess.")
                .setDismissText("Next")
                .build();

        turnSlides[5] = new MaterialShowcaseView.Builder(this)
                .setTarget(buttonRow)
                .withRectangleShape()
                .setTitleText("Mark Your Answer")
                .setContentText("Once your partner has guessed, immediately mark here whether the answer was correct or not.")
                .setDismissText("Next")
                .build();

        turnSlides[6] = new MaterialShowcaseView.Builder(this)
                .setTarget(timerView)
                .setTitleText("Time Limit")
                .setContentText("If you don't finish before time is up, it's automatically wrong!")
                .setDismissText("Next")
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        timerView.setVisibility(View.INVISIBLE);
                        promptForContinue(getString(R.string.pass_phone_next));
                    }
                })
                .build();

        turnSlides[7] = new MaterialShowcaseView.Builder(this)
                .setTarget(ppSpinnerView)
                .setTitleText("If you're wrong...(1/3)")
                .setContentText("The possible points to be won decreases")
                .setDismissText("Next")
                .build();

        turnSlides[8] = new MaterialShowcaseView.Builder(this)
                .setTarget(messageView)
                .withRectangleShape()
                .setTitleText("If you're wrong...(2/3)")
                .setContentText("Now pass the phone to the other person (next to you)")
                .setDismissText("Next")
                .build();

        turnSlides[9] = new MaterialShowcaseView.Builder(this)
                .setTarget(continueButton)
                .withRectangleShape()
                .setTitleText("If you're wrong...(3/3)")
                .setContentText("That person clicks continue and takes his/her turn.")
                .setDismissText("Next")
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        promptForContinue(getString(R.string.pass_phone_across));
                    }
                })
                .build();

        turnSlides[10] = new MaterialShowcaseView.Builder(this)
                .setTarget(scoreView)
                .setTitleText("If you're right...(1/4)")
                .setContentText("Your team receives the current value of the winnable points spinner")
                .setDismissText("Next")
                .build();

        turnSlides[11] = new MaterialShowcaseView.Builder(this)
                .setTarget(ppSpinnerView)
                .setTitleText("If you're right...(2/4)")
                .setContentText("The winnable points reset for the next turn.")
                .setDismissText("Next")
                .build();

        turnSlides[12] = new MaterialShowcaseView.Builder(this)
                .setTarget(messageView)
                .withRectangleShape()
                .setTitleText("If you're right...(3/4)")
                .setContentText("Now the other two people give hints (from the A's to the B's or vice versa). ")
                .setDismissText("Next")
                .build();

        turnSlides[13] = new MaterialShowcaseView.Builder(this)
                .setTarget(continueButton)
                .withRectangleShape()
                .setTitleText("If you're right...(4/4)")
                .setContentText("The other side clicks continue and starts the next round")
                .setDismissText("Next")
                .build();

        MaterialShowcaseView finalSlide = new MaterialShowcaseView.Builder(this)
                .setTarget(new View(this))
                .setTitleText("Tutorial Complete")
                .setContentText("You are ready to play the game!")
                .setDismissText("Begin Game!!!")
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        Intent intent = new Intent(getApplicationContext(), TurnActivity.class);
                        intent.putExtras(extrasToForward);
                        startActivity(intent);
                        finish();
                    }
                })
                .build();

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        config.setRenderOverNavigationBar(true);

        MaterialShowcaseSequence showcaseSequence = new MaterialShowcaseSequence(this);
        showcaseSequence.setConfig(config);

        for (int i = 0; i < 3; i++) {
            showcaseSequence.addSequenceItem(centerSCV(introSlides[i]));
        }
        for (int i = 0; i < 7; i++) {
            showcaseSequence.addSequenceItem(overviewSlides[i]);
        }
        for (int i = 0; i < 14; i++) {
            showcaseSequence.addSequenceItem(turnSlides[i]);
        }
        showcaseSequence.addSequenceItem(centerSCV(finalSlide));
        showcaseSequence.start();

    }
}
