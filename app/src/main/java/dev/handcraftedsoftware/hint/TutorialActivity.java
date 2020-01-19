package dev.handcraftedsoftware.hint;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.transition.Explode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.core.widget.NestedScrollView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.Transition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.TextViewCompat;

import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


/**
 * Main activity which is displayed during gameplay.
 * The display shows the word to be guessed, the current score, and the number of the round.
 * By Connor Reeder
 */
public class TutorialActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
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

    private int wordHeight;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }

        // Force Portrait Orientation mode for tutorial to ensure that all ToolTip elements
        // fit within the screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_game);


        wordHeight = getResources().getDimensionPixelSize(R.dimen.word_height);

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
        viewPager.setVisibility(View.INVISIBLE);
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

        showTutorial();


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

    private void promptForContinue(String message) {
        Log.v(TAG, "Here is where we would prompt for continue!");
        messageView.setText(message);
        messageView.setVisibility(View.VISIBLE);
        continueButton.setVisibility(View.VISIBLE);
    }


    private MaterialShowcaseView centerSCV(MaterialShowcaseView showcaseView) {
        TextView title = showcaseView.findViewById(R.id.tv_title);
        TextView content = showcaseView.findViewById(R.id.tv_content);
        TextView dismiss = showcaseView.findViewById(R.id.tv_dismiss);
        showcaseView.setGravity(Gravity.CENTER);
        title.setGravity(Gravity.CENTER);
        content.setAlpha(1f);
        dismiss.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        dismiss.setGravity(Gravity.CENTER);
        return showcaseView;
    }
    private MaterialShowcaseView regularSCV(MaterialShowcaseView showcaseView) {
        TextView content = showcaseView.findViewById(R.id.tv_content);
        content.setAlpha(1f);
        return showcaseView;
    }


    private void showTutorial() {
        acceptWordButton.setVisibility(View.INVISIBLE);
        continueButton.setVisibility(View.INVISIBLE);
        buttonRow.setVisibility(View.INVISIBLE);
        wordHolder.setVisibility(View.VISIBLE);
        wordHolder.setText(R.string.current_word);

        ConstraintSet newSet = new ConstraintSet();
        newSet.clear(R.id.wordHolder);
        newSet.constrainHeight(R.id.wordHolder, getResources().getDimensionPixelSize(R.dimen.word_height));
        newSet.constrainWidth(R.id.wordHolder, ConstraintLayout.LayoutParams.MATCH_PARENT);
        newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.topBar, ConstraintSet.BOTTOM,0);
        newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
        newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
        newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.teamNameBar, ConstraintSet.TOP,0);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(wordHolder, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        wordHolder.setGravity(Gravity.CENTER);
        newSet.applyTo(layout);

        MaterialShowcaseView[] introSlides = new MaterialShowcaseView[3];
        MaterialShowcaseView[] overviewSlides = new MaterialShowcaseView[7];
        MaterialShowcaseView[] turnSlides = new MaterialShowcaseView[14];

        // Intro Information Slides
        introSlides[0] = new MaterialShowcaseView.Builder(this)
                .setTarget(new View(this))
                .setContentText(R.string.objective_1_of_3_cont)
                .setTitleText(R.string.objective_1_of_3_title)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();
        // Add some extra padding to the top (For smaller devices)
        NestedScrollView view = introSlides[0].findViewById(R.id.content_box);
        int padding = getResources().getDimensionPixelSize(R.dimen.showcase_view_padding);
        int extraPadding = getResources().getDimensionPixelSize(R.dimen.showcase_view_extra_padding);
        view.setPadding(padding,extraPadding,padding,padding);

        introSlides[1] = new MaterialShowcaseView.Builder(this)
                .setTarget(new View(this))
                .setTitleText(R.string.objective_2_of_3_title)
                .setContentText(R.string.objective_2_of_3_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        introSlides[2] = new MaterialShowcaseView.Builder(this)
                .setTarget(new View(this))
                .setTitleText(R.string.objective_3_of_3_title)
                .setContentText(R.string.objective_3_of_3_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        // General breakdown of the screen
        overviewSlides[0] = new MaterialShowcaseView.Builder(this)
                .setTarget(roundView)
                .setTitleText(R.string.round_number)
                .setContentText(R.string.round_number_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        overviewSlides[1] = new MaterialShowcaseView.Builder(this)
                .setTarget(scoreView)
                .setTitleText(R.string.current_score)
                .setContentText(R.string.current_score_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        overviewSlides[2] = new MaterialShowcaseView.Builder(this)
                .setTarget(ppSpinnerView)
                .setTitleText(R.string.point_spinner_title)
                .setContentText(R.string.point_spinner_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        overviewSlides[3] = new MaterialShowcaseView.Builder(this)
                .setTarget(wordHolder)
                .withRectangleShape()
                .setTitleText(R.string.word_to_guess)
                .setContentText(R.string.word_to_guess_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
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
                .setTitleText(R.string.word_cover_title)
                .setContentText(R.string.word_cover_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .withRectangleShape()
                .build();

        overviewSlides[5] = new MaterialShowcaseView.Builder(this)
                .setTarget(partnerLetterView)
                .setTitleText(R.string.current_players)
                .setContentText(R.string.current_players_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();
        // Add some extra padding to the top (For smaller devices)
        view = overviewSlides[5].findViewById(R.id.content_box);
        view.setPadding(padding,extraPadding,padding,padding);

        overviewSlides[6] = new MaterialShowcaseView.Builder(this)
                .setTarget(teamNameView)
                .setTitleText(R.string.whos_turn_title)
                .setContentText(R.string.whos_turn_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
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
        // Add some extra padding to the top (For smaller devices)
        view = overviewSlides[6].findViewById(R.id.content_box);
        view.setPadding(padding,extraPadding,padding,padding);



        turnSlides[0] = new MaterialShowcaseView.Builder(this)
                .setTarget(wordHolder)
                .setTitleText(R.string.is_it_guessable)
                .setContentText(R.string.is_it_guessable_cont)
                .withRectangleShape()
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        turnSlides[1] = new MaterialShowcaseView.Builder(this)
                .setTarget(wordHolder)
                .setTitleText(R.string.not_guessable)
                .setContentText(R.string.not_guessable_cont)
                .withRectangleShape()
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        turnSlides[2] = new MaterialShowcaseView.Builder(this)
                .setTarget(wordHolder)
                .setTitleText(R.string.swipe_limit_title)
                .setContentText(R.string.swipe_limit_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .withRectangleShape()
                .build();

        turnSlides[3] = new MaterialShowcaseView.Builder(this)
                .setTarget(acceptWordButton)
                .withRectangleShape()
                .setTitleText(R.string.accept_button_title)
                .setContentText(R.string.accept_button_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {


                        ConstraintSet newSet = new ConstraintSet();
                        newSet.clear(R.id.wordHolder);
                        newSet.constrainHeight(R.id.wordHolder, wordHeight);
                        newSet.constrainWidth(R.id.wordHolder, ConstraintLayout.LayoutParams.MATCH_PARENT);
                        newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.timerView, ConstraintSet.BOTTOM,0);
                        newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                        newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
                        newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.buttonRow, ConstraintSet.TOP,0);

                        Log.d(TAG, "wordHolder.getHeight()1:" + wordHolder.getHeight());
                        TextViewCompat.setAutoSizeTextTypeWithDefaults(wordHolder, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

                        Transition transition = new AutoTransition();
                        TransitionManager.beginDelayedTransition(layout, transition);
                        wordHolder.setGravity(Gravity.CENTER);
                        timerView.setVisibility(View.VISIBLE);
                        acceptWordButton.setVisibility(View.INVISIBLE);
                        buttonRow.setVisibility(View.VISIBLE);
                        newSet.applyTo(layout);
                    }
                })
                .build();
        // Add some extra padding to the top (For smaller devices)
        view = turnSlides[3].findViewById(R.id.content_box);
        view.setPadding(padding,extraPadding,padding,padding);


        turnSlides[4] = new MaterialShowcaseView.Builder(this)
                .setTarget(timerView)
                .setTitleText(R.string.timer_title)
                .setContentText(R.string.timer_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        turnSlides[5] = new MaterialShowcaseView.Builder(this)
                .setTarget(buttonRow)
                .withRectangleShape()
                .setTitleText(R.string.mark_answer_title)
                .setContentText(R.string.mark_answer_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();
        // Add some extra padding to the top (For smaller devices)
        view = turnSlides[5].findViewById(R.id.content_box);
        view.setPadding(padding,extraPadding,padding,padding);

        turnSlides[6] = new MaterialShowcaseView.Builder(this)
                .setTarget(timerView)
                .setTitleText(R.string.time_limit_title)
                .setContentText(R.string.time_limit_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        ConstraintSet newSet = new ConstraintSet();
                        newSet.clear(R.id.wordHolder);
                        newSet.constrainHeight(R.id.wordHolder, wordHeight);
                        newSet.constrainWidth(R.id.wordHolder, ConstraintLayout.LayoutParams.MATCH_PARENT);
                        newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.messageView, ConstraintSet.BOTTOM,0);
                        newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                        newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
                        newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.buttonRow, ConstraintSet.TOP,0);

                        Log.d(TAG, "wordHolder.getHeight()1:" + wordHolder.getHeight());
                        TextViewCompat.setAutoSizeTextTypeWithDefaults(wordHolder, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

                        Transition transition = new AutoTransition();
                        TransitionManager.beginDelayedTransition(layout, transition);
                        promptForContinue(getString(R.string.pass_phone_next));
                        timerView.setVisibility(View.INVISIBLE);
                        newSet.applyTo(layout);
                    }
                })
                .build();

        turnSlides[7] = new MaterialShowcaseView.Builder(this)
                .setTarget(ppSpinnerView)
                .setTitleText(R.string.if_wrong_1_of_3_title)
                .setContentText(R.string.if_wrong_1_of_3_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        turnSlides[8] = new MaterialShowcaseView.Builder(this)
                .setTarget(messageView)
                .withRectangleShape()
                .setTitleText(R.string.if_wrong_2_of_3_title)
                .setContentText(R.string.if_wrong_2_of_3_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        turnSlides[9] = new MaterialShowcaseView.Builder(this)
                .setTarget(continueButton)
                .withRectangleShape()
                .setTitleText(R.string.if_wrong_3_of_3_title)
                .setContentText(R.string.if_wrong_3_of_3_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        Transition transition = new AutoTransition();
                        TransitionManager.beginDelayedTransition(layout, transition);
                        promptForContinue(getString(R.string.pass_phone_across));
                    }
                })
                .build();
        // Add some extra padding to the top (For smaller devices)
        view = turnSlides[9].findViewById(R.id.content_box);
        view.setPadding(padding,extraPadding,padding,padding);

        turnSlides[10] = new MaterialShowcaseView.Builder(this)
                .setTarget(scoreView)
                .setTitleText(R.string.if_right_1_of_4_title)
                .setContentText(R.string.if_right_1_of_4_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        turnSlides[11] = new MaterialShowcaseView.Builder(this)
                .setTarget(ppSpinnerView)
                .setTitleText(R.string.if_right_2_of_4_title)
                .setContentText(R.string.if_right_2_of_4_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        turnSlides[12] = new MaterialShowcaseView.Builder(this)
                .setTarget(messageView)
                .withRectangleShape()
                .setTitleText(R.string.if_right_3_of_4_title)
                .setContentText(R.string.if_right_3_of_4_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();

        turnSlides[13] = new MaterialShowcaseView.Builder(this)
                .setTarget(continueButton)
                .withRectangleShape()
                .setTitleText(R.string.if_right_4_of_4_title)
                .setContentText(R.string.if_right_4_of_4_cont)
                .setDismissText(R.string.next)
                .setSkipText(R.string.skip_tutorial)
                .build();
        // Add some extra padding to the top (For smaller devices)
        view = turnSlides[13].findViewById(R.id.content_box);
        view.setPadding(padding,extraPadding,padding,padding);


        int finalDismissMsg;
        if (isFirstTime()) {
            // First time tutorial
            finalDismissMsg = R.string.begin_game_dismiss;
        } else {
            // Re-watching the tutorial
            finalDismissMsg = R.string.done;
        }

        MaterialShowcaseView finalSlide = new MaterialShowcaseView.Builder(this)
                .setTarget(new View(this))
                .setTitleText(R.string.tutorial_complete_title)
                .setContentText(R.string.tutorial_complete_cont)
                .setDismissText(finalDismissMsg)
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        if (isFirstTime()) {
                            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                            intent.putExtras(extrasToForward);
                            startActivity(intent);
                            finish();
                        } else {
                           finish();
                        }
                    }
                })
                .build();

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        config.setRenderOverNavigationBar(true);

        MaterialShowcaseSequence showcaseSequence = new CustomShowcaseSequence(this);
        showcaseSequence.setConfig(config);

        for (int i = 0; i < 3; i++) {
            showcaseSequence.addSequenceItem(centerSCV(introSlides[i]));
        }
        for (int i = 0; i < 7; i++) {
            showcaseSequence.addSequenceItem(regularSCV(overviewSlides[i]));
        }
        for (int i = 0; i < 14; i++) {
            showcaseSequence.addSequenceItem(regularSCV(turnSlides[i]));
        }
        showcaseSequence.addSequenceItem(centerSCV(finalSlide));
        showcaseSequence.start();

    }

    /**
     * Checks to see if this tutorial instance is the first-run tutorial
     * E.g. if this was initiated from the BeginActivity and should
     * forward to the GameActivity to start the game when it completes.
     * @return whether or not this is the first-time tutorial
     */
    private boolean isFirstTime() {
        return getParent() instanceof BeginActivity;
    }

    /**
     * Subclass of the MaterialShowcaseSequence in order to add in the onSkipped functionality
     * This is necessary in order to finish() the TutorialActivity when it is cancelled
     * part-way through the slides
     */
    private class CustomShowcaseSequence extends MaterialShowcaseSequence {
        public CustomShowcaseSequence(Activity activity) {
            super(activity);
        }

        public CustomShowcaseSequence(Activity activity, String sequenceID) {
            super(activity, sequenceID);
        }

        @Override
        public void onShowcaseDetached(MaterialShowcaseView showcaseView, boolean wasDismissed, boolean wasSkipped) {
            super.onShowcaseDetached(showcaseView, wasDismissed, wasSkipped);
            if (wasSkipped) {
                // If its the first-time tutorial...continue on to the actual game
                if (isFirstTime()) {
                    Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                    intent.putExtras(extrasToForward);
                    startActivity(intent);
                    finish();
                } else {
                    // Otherwise just close the TutorialActivity to return to where it was started from
                    finish();
                }
            }
        }
    }
}
