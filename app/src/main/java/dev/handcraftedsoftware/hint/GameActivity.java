package dev.handcraftedsoftware.hint;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.Transition;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;


/**
 * Main activity which is displayed during gameplay.
 * The display shows the word to be guessed, the current score, and the number of the round.
 * By Connor Reeder
 */
public class GameActivity extends AppCompatActivity implements View.OnTouchListener, MenuFragment.MenuActionsHandler {

    private static final String TAG = "GameActivity";
    public static final int NUM_ROUNDS = 6;


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
    private Button successButton;
    private Button failureButton;

    //Word-Swiper Functionality
    private TextPagerAdapter adapter;
    private ProgressBar loadingIcon;

    // Game variables removed here --> See GameModelView
    private GameModelView gameModelView;

    private ObjectAnimator coverAlphaAnimator;

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

        gameModelView = ViewModelProviders.of(this).get(GameModelView.class);

        wordHeight = getResources().getDimensionPixelSize(R.dimen.word_height);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        Log.d(TAG, "dpHeight: " + dpHeight);
        Log.d(TAG, "dpWidth: " + dpWidth);

        // Set portrait only on small devices
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp < 350) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_game);

//        Intent parentIntent = getIntent();

        // Setup Display
        roundView = findViewById(R.id.roundText);
        roundView.setTextSize(25);
        scoreView = findViewById(R.id.scoreText);
        ppSpinnerView = findViewById(R.id.ppSpinner);
        partnerLetterView = findViewById(R.id.partnerLetterText);
        teamNameView = findViewById(R.id.teamName);
        viewPager = findViewById(R.id.pager);
        viewPager.setSwipeController(gameModelView);
        acceptWordButton = findViewById(R.id.acceptWordButton);
        continueButton = findViewById(R.id.continueButton);
        messageView = findViewById(R.id.messageView);
        timerView = findViewById(R.id.timerView);
        wordHolder = findViewById(R.id.wordHolder);
        wordCover = findViewById(R.id.wordCover);
        loadingIcon = findViewById(R.id.progressBar);
        layout = findViewById(R.id.activity_turn);
        successButton = findViewById(R.id.successButton);
        failureButton = findViewById(R.id.failureButton);
        viewPager.setOnTouchListener(this);

        coverAlphaAnimator = new ObjectAnimator();
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (gameModelView.getGameState().getValue() == GameState.PLAYING || gameModelView.getGameState().getValue() == GameState.TEAM_TRANSITION) {
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        Log.d(TAG, "layout ACTION_DOWN");
                        gameModelView.setIsWordHidden(false);
//                        isWordHidden = false;
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
                        gameModelView.setIsWordHidden(true);
//                        isWordHidden = true;
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

        acceptWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameModelView.setGameState(GameState.PLAYING);
            }
        });
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onContinue();
            }
        });
        findViewById(R.id.pauseButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseGame();
            }
        });
        findViewById(R.id.successButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guessMade(view);
            }
        });
        findViewById(R.id.failureButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guessMade(view);
            }
        });


        // Add observers!
        gameModelView.getGameState().observe(this, new Observer<GameState>() {
            @Override
            public void onChanged(GameState newGameState) {

                if (newGameState == GameState.AWAITING_WORDS) {

                } else if (newGameState == GameState.WORD_APPROVAL) {

                    loadingIcon = findViewById(R.id.progressBar);
                    loadingIcon.setVisibility(View.INVISIBLE);
                    successButton.setVisibility(View.VISIBLE);
                    failureButton.setVisibility(View.VISIBLE);
                    acceptWordButton.setVisibility(View.VISIBLE);

                } else if (newGameState == GameState.PLAYING) {
                    // Initialize the CountDownTimer from where it was before we stopped
                    timerView.setVisibility(View.VISIBLE);
                    messageView.setVisibility(View.INVISIBLE);
                    loadingIcon.setVisibility(View.INVISIBLE);
                    successButton.setVisibility(View.VISIBLE);
                    failureButton.setVisibility(View.VISIBLE);
                    acceptWordButton.setVisibility(View.INVISIBLE);
                    viewPager.setVisibility(View.INVISIBLE);
                    wordHolder.setVisibility(View.VISIBLE);

                    wordHolder.setText(gameModelView.getCurrentWord());

                    Transition transition = new AutoTransition();
                    transition.addListener(new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(@NonNull Transition transition) {
                            gameModelView.setIsWordHidden(true);
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
                    newSet.constrainHeight(R.id.wordHolder, wordHeight);
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



                    if (gameModelView.getIsWordHidden().getValue())
                        wordCover.setAlpha(1f);

//                    // Correctly restore position of the WordHolder vertically between the timerView and the buttonrow
//                    ConstraintSet newSet = new ConstraintSet();
//                    wordHolder.setText(gameModelView.getWordList().getValue()[gameModelView.getWordIdx().getValue()]);
//                    viewPager.setVisibility(View.INVISIBLE);
//                    newSet.clear(R.id.wordHolder);
//                    newSet.constrainHeight(R.id.wordHolder, wordHeight);
//                    newSet.constrainWidth(R.id.wordHolder, ConstraintLayout.LayoutParams.MATCH_PARENT);
//                    newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.timerView, ConstraintSet.BOTTOM,0);
//                    newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
//                    newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
//                    newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.buttonRow, ConstraintSet.TOP,0);
//                    TextViewCompat.setAutoSizeTextTypeWithDefaults(wordHolder, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
//                    wordHolder.setGravity(Gravity.CENTER);
//                    newSet.applyTo(layout);
//                    timerView.setText(String.format(Locale.getDefault(),"%02d",Math.round(gameModelView.getCountDownTimeRemaining().getValue() / 1000)));
//                    timerView.setVisibility(View.VISIBLE);
                } else if (newGameState == GameState.TEAM_TRANSITION) {

                    timerView.setVisibility(View.INVISIBLE);
                    loadingIcon.setVisibility(View.INVISIBLE);

                    promptForContinue(getString(R.string.pass_phone_next));
                    timerView.setVisibility(View.INVISIBLE);

                    if (gameModelView.getIsWordHidden().getValue())
                        wordCover.setAlpha(1f);


                    wordHolder.setText(gameModelView.getWordList().getValue()[gameModelView.getWordIdx().getValue()]);
                    viewPager.setVisibility(View.INVISIBLE);


                    // Position wordHolder between the continueButton and the messageView
                    ConstraintSet newSet = new ConstraintSet();
                    newSet.clear(R.id.wordHolder);
                    newSet.constrainHeight(R.id.wordHolder, wordHeight);
                    newSet.constrainWidth(R.id.wordHolder, ConstraintLayout.LayoutParams.MATCH_PARENT);
                    newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.messageView, ConstraintSet.BOTTOM,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.continueButton, ConstraintSet.TOP,0);
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(wordHolder, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                    wordHolder.setGravity(Gravity.CENTER);
                    newSet.applyTo(layout);
                } else if (newGameState == GameState.WORD_TRANSITION) {
                    promptForContinue(getString(R.string.pass_phone_across));
                    timerView.setVisibility(View.INVISIBLE);
                    loadingIcon.setVisibility(View.INVISIBLE);

                    Log.v(TAG, "Restarting in Word Transition!");
                    wordHolder.setText(gameModelView.getWordList().getValue()[gameModelView.getWordIdx().getValue()]);
                    viewPager.setVisibility(View.INVISIBLE);

                    // Position wordHolder between the continueButton and the messageView
                    ConstraintSet newSet = new ConstraintSet();
                    newSet.clear(R.id.wordHolder);
                    newSet.constrainHeight(R.id.wordHolder, wordHeight);
                    newSet.constrainWidth(R.id.wordHolder, ConstraintLayout.LayoutParams.MATCH_PARENT);
                    newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.messageView, ConstraintSet.BOTTOM,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
                    newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.continueButton, ConstraintSet.TOP,0);
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(wordHolder, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                    wordHolder.setGravity(Gravity.CENTER);
                    newSet.applyTo(layout);
                } else if (newGameState == GameState.GAME_OVER) {
                } else if (newGameState == GameState.DOWNLOAD_ERROR){
                    makeSnackBar();
                } else{
                    Log.e(TAG, "Transitioned into unknown game state!");
                }
            }
        });
        gameModelView.getCurrRound().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer newRound) {
                roundView.setText(String.format("%c%s",'#',Integer.toString(newRound)));

                // Alternate which team begins each round
                gameModelView.setIsTeam2((newRound % 2) == 0);
            }
        });
        gameModelView.getTotalScores().observe(this, new Observer<Integer[]>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(Integer[] newScores) {
                scoreView.setText(Integer.toString(newScores[0]) + ':' + newScores[1]);
            }
        });
        gameModelView.getWordList().observe(this, new Observer<String[]>() {
            @Override
            public void onChanged(String[] updatedWordList) {
                // Initialize the Word Viewpager (for swiping/skipping through words)
                // This method sets up the word-slider by creating the adapter for the word list
                adapter = new TextPagerAdapter(GameActivity.this, updatedWordList);
                viewPager.setAdapter(adapter);
            }
        });
        gameModelView.getIsPartnerB().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isNowPartnerB) {
                if(!isNowPartnerB)
                    partnerLetterView.setText("A");
                else
                    partnerLetterView.setText("B");
            }
        });
        gameModelView.getIsTeam2().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isNowTeam2) {
                if(!isNowTeam2)
                    teamNameView.setText(gameModelView.getTeamName1().getValue());
                else
                    teamNameView.setText(gameModelView.getTeamName2().getValue());
            }
        });
        gameModelView.getCurrPP().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer newPP) {
                ppSpinnerView.setSpinnerIdx(newPP);
            }
        });
        gameModelView.getTicker().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long millis) {
                if (millis < 0) {
                    onCountDownCompleted();
                } else {
                    timerView.setText(String.format("%02d",Math.round(millis / 1000)));
                }
            }
        });



        // If the game is started for the first time
        if (savedInstanceState == null) {
        } else { //if savedInstanceState != null  -----> We are RE-starting our activity
            Log.d(TAG, "Restart");
            Log.v(TAG, "Game state on restart: " + gameModelView.getGameState().getValue());
        }

        // Setup add banner
        if (BuildConfig.FLAVOR.equals("free")) {
            FrameLayout frameLayout = findViewById(R.id.adFrame);
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });

            RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
                    .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                    .build();
            MobileAds.setRequestConfiguration(requestConfiguration);
            AdView adView = (AdView) frameLayout.getChildAt(0);
            Bundle extras = new Bundle(); extras.putString("max_ad_content_rating", "G");
            AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
            adView.loadAd(adRequest);
        } else {
            Log.d(TAG, "not free");
        }
    }

    private void makeSnackBar() {
        Snackbar snackbar = Snackbar
                .make(layout, R.string.download_error, 5000);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                finish();
            }
        });
        View snackbarView = snackbar.getView();
        int snackbarTextId = com.google.android.material.R.id.snackbar_text;
        TextView textView = snackbarView.findViewById(snackbarTextId);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        textView.setMaxLines(4);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setGravity(Gravity.CENTER);
        } else {
            textView.setGravity(Gravity.CENTER);
        }
        snackbar.show();
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

    }

    /**
     * The method called when a guess has been made
     * @param view The button pressed to signal that a guess has been made
     */
    private void guessMade(View view) {
        if (gameModelView.getGameState().getValue() != GameState.PLAYING) {
            Log.e(TAG, "Guess made while not playing!");
            return;
        }


        // Reset the timer for next time we use it.
        gameModelView.resetCountDownTimer();

        TextView currentView = adapter.getCurrentView().findViewById(R.id.singleTextView);
        String currWord = currentView.getText().toString();
        // If the guess was correct
        if (view.getId() == R.id.successButton) {

            gameModelView.storeResult(currWord);
            gameModelView.scoreCorrectAnswer();

            coverAlphaAnimator.cancel();
            coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",1f,0f);
            coverAlphaAnimator.setDuration(500);
            coverAlphaAnimator.start();

            ppSpinnerView.resetSpinner();

            // If the guess was incorrect
        } else if (view.getId() == R.id.failureButton) {

            gameModelView.decCurrPP();
            // The word was NEVER guessed and there are not more tries left
            if (gameModelView.getCurrPP().getValue() == 10) {

                gameModelView.storeResult(currWord);
//                gameModelView.scoreIncorrectAnswer();

                coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",1f,0f);
                coverAlphaAnimator.setDuration(500);
                coverAlphaAnimator.start();

                // Increment the round number if both sets of opposing players have played
                if (gameModelView.getIsPartnerB().getValue())
                    gameModelView.incCurrRound();

                gameModelView.flipPartnerLetter();

            } else {// The word has not yet been correctly guessed but there are still chances left
                gameModelView.switchTeams();
            }

            // Decrement current possible points

        }

        // Check if end of game
        if (gameModelView.getCurrRound().getValue() > NUM_ROUNDS) {
            // Game is Over
            gameModelView.setGameState(GameState.GAME_OVER);
            startWinnerScreen();

        } else { // If not the end of the game
            if (gameModelView.getGameState().getValue() == GameState.TEAM_TRANSITION) {
                promptForContinue(getString(R.string.pass_phone_next));
            } else if (gameModelView.getGameState().getValue() == GameState.WORD_TRANSITION) {
                promptForContinue(getString(R.string.pass_phone_across));
            }

            ConstraintSet newSet = new ConstraintSet();
            newSet.clear(R.id.wordHolder);
            newSet.constrainHeight(R.id.wordHolder, wordHeight);
            newSet.constrainWidth(R.id.wordHolder, ConstraintLayout.LayoutParams.MATCH_PARENT);
            newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.messageView, ConstraintSet.BOTTOM,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
            newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.continueButton, ConstraintSet.TOP,0);

            TextViewCompat.setAutoSizeTextTypeWithDefaults(wordHolder, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

            TransitionManager.beginDelayedTransition(layout);
            wordHolder.setGravity(Gravity.CENTER);
            newSet.applyTo(layout);
            loadingIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void startWinnerScreen() {

        // Create Winner Screen intent
        Intent winnerIntent = new Intent(this, WinnerActivity.class);

        // Determine who the winner is based off the final scores
        if (gameModelView.isTeam1ScoreGreater())
            winnerIntent.putExtra(GK.WINNER_TEAM_NAME, gameModelView.getTeamName1().getValue());
        else if (gameModelView.isTeam2ScoreGreater())
            winnerIntent.putExtra(GK.WINNER_TEAM_NAME, gameModelView.getTeamName2().getValue());

        // Attach the team names and necessary scoring values to the Winner Screen intent
        winnerIntent.putExtra(GK.A_SCORES_1, gameModelView.getaScores1().getValue());
        winnerIntent.putExtra(GK.A_SCORES_2, gameModelView.getaScores2().getValue());
        winnerIntent.putExtra(GK.B_SCORES_1, gameModelView.getbScores1().getValue());
        winnerIntent.putExtra(GK.B_SCORES_2, gameModelView.getbScores2().getValue());
        winnerIntent.putExtra(GK.A_WORDS, gameModelView.getaWords().getValue());
        winnerIntent.putExtra(GK.B_WORDS, gameModelView.getbWords().getValue());
        winnerIntent.putExtra(GK.TOTAL_SCORE_1, gameModelView.getTotalScores().getValue()[0]);
        winnerIntent.putExtra(GK.TOTAL_SCORE_2, gameModelView.getTotalScores().getValue()[1]);
        winnerIntent.putExtra(GK.TEAM_NAME_1, gameModelView.getTeamName1().getValue());
        winnerIntent.putExtra(GK.TEAM_NAME_2, gameModelView.getTeamName2().getValue());
        winnerIntent.putExtra(GK.DIFFICULTY, gameModelView.getDifficulty().getValue());
        winnerIntent.putExtra(GK.LANGUAGE, gameModelView.getLanguage().getValue());

        // Print Intent Goodies
        Log.d(TAG, "aScores1: " + Arrays.toString(gameModelView.getaScores1().getValue()));
        Log.d(TAG, "aScores2: " + Arrays.toString(gameModelView.getaScores2().getValue()));
        Log.d(TAG, "bScores1: " + Arrays.toString(gameModelView.getbScores1().getValue()));
        Log.d(TAG, "bScores2: " + Arrays.toString(gameModelView.getbScores2().getValue()));
        Log.d(TAG, "aWords: " + Arrays.toString(gameModelView.getaWords().getValue()));
        Log.d(TAG, "bWords: " + Arrays.toString(gameModelView.getbWords().getValue()));
        Log.d(TAG, "totalScore1: " + gameModelView.getTotalScores().getValue()[0]);
        Log.d(TAG, "totalScore2: " + gameModelView.getTotalScores().getValue()[1]);
        Log.d(TAG, "teamName1: " + gameModelView.getTeamName1().getValue());
        Log.d(TAG, "teamName2: " + gameModelView.getTeamName2().getValue());
        Log.d(TAG, "difficulty: " + gameModelView.getDifficulty().getValue());
        Log.d(TAG, "language: " + gameModelView.getLanguage().getValue());

        //Launch Winner Activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(winnerIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(winnerIntent);
        }
        finish();
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
        Log.v(TAG, "We have continued in this state: " + gameModelView.getGameState().getValue());
        messageView.setVisibility(View.INVISIBLE);
        continueButton.setVisibility(View.INVISIBLE);

        if (gameModelView.getGameState().getValue() == GameState.TEAM_TRANSITION) {
            // Next Team guesses
            gameModelView.setGameState(GameState.PLAYING);
        } else if (gameModelView.getGameState().getValue() == GameState.WORD_TRANSITION) {
            // The other two players now start giving hints and a new word is approved

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

                    // Advances the word-swiper to the next word
                    Log.v(TAG, "nextWord");
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                    gameModelView.incWordIdx();
                    gameModelView.setGameState(GameState.WORD_APPROVAL);
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
            newSet.constrainHeight(R.id.wordHolder, wordHeight);
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
     * onTouch method used for detecting a tap on the word-swiper
     * @param view the view that the onTouch event was fired from
     * @param motionEvent the motion event that occurred on the touched view
     * @return whether or not the touch was received
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    /**
     * Called when the pause button is pressed.
     * In reality this only gives you the ability to return to the start screen
     */
    private void pauseGame() {
        if (gameModelView.getGameState().getValue() == GameState.AWAITING_WORDS) {
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
    private void onCountDownCompleted() {
        Log.v(TAG, "Timer Complete!");
        // Simulate a Incorrect Button press
        guessMade(failureButton);
    }
}
