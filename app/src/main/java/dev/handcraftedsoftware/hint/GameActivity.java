package dev.handcraftedsoftware.hint;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.Transition;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.Locale;

import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;


/**
 * Main activity which is displayed during gameplay.
 * The display shows the word to be guessed, the current score, and the number of the round.
 * By Connor Reeder
 */
public class GameActivity extends AppCompatActivity implements OneDirectionViewPager.SwipeController, View.OnTouchListener, MenuFragment.MenuActionsHandler, DownloadFragment.OnDownloadCompleteListener {

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

    //Word-Swiper Functionality
    private TextPagerAdapter adapter;
    private ProgressBar loadingIcon;

    // Game variables removed here --> See GameModelView
    private GameModelView gameModelView;

    // CountDown used for the game timer
    private CountDownTimer countDownTimer;
    private long countDownTimeRemaining;
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
                if (gameModelView.getGameState().getValue() == GameState.PLAYING || gameModelView.getGameState().getValue() == GameState.TEAM_TRANSITION) {
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        Log.d(TAG, "layout ACTION_DOWN");
                        gameModelView.getIsWordHidden().setValue(false);
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
                        gameModelView.getIsWordHidden().setValue(false);
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
                onAcceptWord();
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

        // If the game is started for the first time
        DownloadFragment downloadFragment;
        if (savedInstanceState == null) {
//            Log.d(TAG, "From scratch");
            // Init Game Values
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//            teamName1 = sharedPreferences.getString(GK.TEAM_NAME_1, getString(R.string.team1));
//            teamName2 = sharedPreferences.getString(GK.TEAM_NAME_2, getString(R.string.team2));
//            teamName1 = parentIntent.getStringExtra(GK.TEAM_NAME_1);
//            teamName2 = parentIntent.getStringExtra(GK.TEAM_NAME_2);
//            if (teamName1.equals("")) {
//                teamName1 = getString(R.string.team1);
//            }
//            if (teamName2.equals("")) {
//                teamName2 = getString(R.string.team2);
//            }
//            difficulty = parentIntent.getStringExtra(GK.DIFFICULTY);
//            difficulty = sharedPreferences.getString(GK.DIFFICULTY, GV.EASY);
//            language = parentIntent.getStringExtra(GK.LANGUAGE);
//            language = sharedPreferences.getString(GK.LANGUAGE, GV.ENGLISH);
//            currRound = 1;
//            currPP = 10;
//            isPartnerB = false;
//            isTeam2 = false;
//            totalScore1 = 0;
//            totalScore2 = 0;
//            currSkipCountA = 0;
//            currSkipCountB = 0;
//            gameState = GameState.AWAITING_WORDS;
//            wordIdx = 0;
//            isWordHidden = false;

            // Init Results Variables
//            aWords = new String[NUM_ROUNDS];
//            bWords = new String[NUM_ROUNDS];
//            aScores1 = new int[NUM_ROUNDS];
//            aScores2 = new int[NUM_ROUNDS];
//            bScores1 = new int[NUM_ROUNDS];
//            bScores2 = new int[NUM_ROUNDS];

            // Bundle the information sent to the Download Fragment
//            Bundle fragmentBundle = new Bundle();
//            fragmentBundle.putString(GK.LANGUAGE, language);
//            fragmentBundle.putString(GK.DIFFICULTY, difficulty);

            // Create DownloadFragment and start it.
            FragmentManager fm = getSupportFragmentManager();
            downloadFragment = (DownloadFragment) fm.findFragmentByTag(GK.DOWNLOAD_FRAGMENT);

            if (downloadFragment != null) {
                Log.e(TAG, "Download Fragment already exists!");
            } else {
                downloadFragment = new DownloadFragment();
//                downloadFragment.setArguments(fragmentBundle);
                fm.beginTransaction().add(downloadFragment, GK.DOWNLOAD_FRAGMENT).commit();
            }

        } else { //if savedInstanceState != null  -----> We are RE-starting our activity
//            Log.d(TAG, "Restart");

            // Values Constant for the Entirety of one Game
//            teamName1 = savedInstanceState.getString(GK.TEAM_NAME_1);
//            teamName2 = savedInstanceState.getString(GK.TEAM_NAME_2);
//            difficulty = savedInstanceState.getString(GK.DIFFICULTY);
//            language = savedInstanceState.getString(GK.LANGUAGE);
//            wordList = savedInstanceState.getStringArray(GK.WORD_LIST);

            // Ever-Changing "Current" Variables
//            currRound = savedInstanceState.getInt(GK.CURR_ROUND);
//            currPP = savedInstanceState.getInt(GK.CURR_PP);
//            isPartnerB = savedInstanceState.getBoolean(GK.IS_PARTNER_B);
//            isTeam2 = savedInstanceState.getBoolean(GK.IS_TEAM_2);
//            totalScore1 = savedInstanceState.getInt(GK.CURR_SCORE_1);
//            totalScore2 = savedInstanceState.getInt(GK.CURR_SCORE_2);
//            currSkipCountA = savedInstanceState.getInt(GK.CURR_SKIP_COUNT_A);
//            currSkipCountB = savedInstanceState.getInt(GK.CURR_SKIP_COUNT_B);
//            previousCorrect = savedInstanceState.getBoolean(GK.PREVIOUS_CORRECT);
//            gameState = (GameState) savedInstanceState.getSerializable(GK.GAME_STATE);
//            wordIdx = savedInstanceState.getInt(GK.WORD_IDX);
            countDownTimeRemaining = savedInstanceState.getLong(GK.TIME_REMAINING);
//            isWordHidden = savedInstanceState.getBoolean(GK.WORD_HIDDEN);

            Log.v(TAG, "Game state on restart: " + gameModelView.getGameState().getValue());

            // Results Variables to be Passed to the Winner Screen
//            aWords = savedInstanceState.getStringArray(GK.A_WORDS);
//            bWords = savedInstanceState.getStringArray(GK.B_WORDS);
//            aScores1 = savedInstanceState.getIntArray(GK.A_SCORES_1);
//            aScores2 = savedInstanceState.getIntArray(GK.A_SCORES_2);
//            bScores1 = savedInstanceState.getIntArray(GK.B_SCORES_1);
//            bScores2 = savedInstanceState.getIntArray(GK.B_SCORES_2);

            // Recover Download Fragment
            FragmentManager fm = getSupportFragmentManager();
            downloadFragment = (DownloadFragment) fm.findFragmentByTag(GK.DOWNLOAD_FRAGMENT);

            if (downloadFragment == null) {
                Log.e(TAG, "Download Fragment doesn't exist!");
            } else {
                if (downloadFragment.isComplete()) {
                    // Hide the loading icon IMMEDIATELY since we are only re-starting the activity and have already obtained our word data
                    loadingIcon.setVisibility(View.INVISIBLE);
                    findViewById(R.id.successButton).setVisibility(View.VISIBLE);
                    findViewById(R.id.failureButton).setVisibility(View.VISIBLE);
                    initWords();
                    ppSpinnerView.setSpinner(gameModelView.getCurrPP().getValue());
                    updateDisplay();
                    if (gameModelView.getGameState().getValue() == GameState.TEAM_TRANSITION) {
                        promptForContinue(getString(R.string.pass_phone_next));

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
                    } else if (gameModelView.getGameState().getValue() == GameState.WORD_TRANSITION) {
                        promptForContinue(getString(R.string.pass_phone_across));

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
                    } else if (gameModelView.getGameState().getValue() == GameState.WORD_APPROVAL) {
                        acceptWordButton.setVisibility(View.VISIBLE);
                    } else if (gameModelView.getGameState().getValue() == GameState.PLAYING) {

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

                        if (gameModelView.getIsWordHidden().getValue())
                            wordCover.setAlpha(1f);

                        // Correctly restore position of the WordHolder vertically between the timerView and the buttonrow
                        ConstraintSet newSet = new ConstraintSet();
                        wordHolder.setText(gameModelView.getWordList().getValue()[gameModelView.getWordIdx().getValue()]);
                        viewPager.setVisibility(View.INVISIBLE);
                        newSet.clear(R.id.wordHolder);
                        newSet.constrainHeight(R.id.wordHolder, wordHeight);
                        newSet.constrainWidth(R.id.wordHolder, ConstraintLayout.LayoutParams.MATCH_PARENT);
                        newSet.connect(R.id.wordHolder, ConstraintSet.TOP,R.id.timerView, ConstraintSet.BOTTOM,0);
                        newSet.connect(R.id.wordHolder, ConstraintSet.LEFT,ConstraintSet.PARENT_ID, ConstraintSet.LEFT,0);
                        newSet.connect(R.id.wordHolder, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,0);
                        newSet.connect(R.id.wordHolder, ConstraintSet.BOTTOM,R.id.buttonRow, ConstraintSet.TOP,0);
                        TextViewCompat.setAutoSizeTextTypeWithDefaults(wordHolder, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                        wordHolder.setGravity(Gravity.CENTER);
                        newSet.applyTo(layout);
                        timerView.setText(String.format(Locale.getDefault(),"%02d",Math.round(countDownTimeRemaining / 1000)));
                        timerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(TAG, "Activity Restarted but words not ready yet!");
                }
            }
            // Game has been successfully restarted
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

    // State transition into Approval Mode
    private void approveNextWord() {
        gameModelView.getGameState().setValue(GameState.WORD_APPROVAL);
//        gameState = GameState.WORD_APPROVAL;
        acceptWordButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
//        // Values Constant for the Entirety of one Game
//        savedInstanceState.putString(GK.TEAM_NAME_1,teamName1);
//        savedInstanceState.putString(GK.TEAM_NAME_2,teamName2);
//        savedInstanceState.putString(GK.DIFFICULTY,difficulty);
//        savedInstanceState.putString(GK.LANGUAGE,language);
//        savedInstanceState.putStringArray(GK.WORD_LIST,wordList);

//        // Ever-Changing "Current" Variables
//        savedInstanceState.putInt(GK.CURR_ROUND,currRound);
//        savedInstanceState.putInt(GK.CURR_PP,currPP);
//        savedInstanceState.putBoolean(GK.IS_PARTNER_B,isPartnerB);
//        savedInstanceState.putBoolean(GK.IS_TEAM_2,isTeam2);
//        savedInstanceState.putInt(GK.CURR_SCORE_1, totalScore1);
//        savedInstanceState.putInt(GK.CURR_SCORE_2, totalScore2);
//        savedInstanceState.putInt(GK.CURR_SKIP_COUNT_A,currSkipCountA);
//        savedInstanceState.putInt(GK.CURR_SKIP_COUNT_B,currSkipCountB);
//        savedInstanceState.putBoolean(GK.PREVIOUS_CORRECT, previousCorrect);
//        savedInstanceState.putSerializable(GK.GAME_STATE, gameState);
//        savedInstanceState.putInt(GK.WORD_IDX, wordIdx);
        savedInstanceState.putLong(GK.TIME_REMAINING, countDownTimeRemaining);
//        savedInstanceState.putBoolean(GK.WORD_HIDDEN, isWordHidden);
//
//        // Results Variables to be Passed to the Winner Screen
//        savedInstanceState.putStringArray(GK.A_WORDS,aWords);
//        savedInstanceState.putStringArray(GK.B_WORDS,bWords);
//        savedInstanceState.putIntArray(GK.A_SCORES_1,aScores1);
//        savedInstanceState.putIntArray(GK.A_SCORES_2,aScores2);
//        savedInstanceState.putIntArray(GK.B_SCORES_1,bScores1);
//        savedInstanceState.putIntArray(GK.B_SCORES_2,bScores2);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Initialize the Word Viewpager (for swiping/skipping through words)
     * This method sets up the word-slider by creating the adapter for the word list
     */
    private void initWords() {
        adapter = new TextPagerAdapter(this, gameModelView.getWordList().getValue());
        viewPager.setAdapter(adapter);
    }

    /**
     * Called by DownloadFragment when finished downloading words.
     */
    @Override
    public void onDownloadComplete(String result) {
        Log.v(TAG, "onDownloadComplete");
        try {
//            wordList = new String[22];
            String[] newList = new String[22];
            JSONArray response = new JSONArray(result);
            for (int i = 0; i < response.length(); i++) {
                newList[i] = response.getString(i);
            }
            gameModelView.getWordList().setValue(newList);
            if (response.length() != 22) throw new AssertionError("DID NOT GET 22 WORDS!!!");
            //Hide Loading Icon now that Data has been received
            loadingIcon = findViewById(R.id.progressBar);
            loadingIcon.setVisibility(View.INVISIBLE);
            findViewById(R.id.successButton).setVisibility(View.VISIBLE);
            findViewById(R.id.failureButton).setVisibility(View.VISIBLE);
            initWords();
            updateDisplay();
            approveNextWord();
            //Game has now begun
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Contents of Response: ");
            Log.e(TAG, result);
            makeSnackBar();

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

    /**
     * Callback Method implementing the OneDirectionViewPager which is called upon a swipe being performed.
     * In this case we are using it to update the counts of how many times each set of opposing players has skipped a word
     * @param newIndex the index of the OneDirectionViewPager after being swiped.
     */
    @Override
    public void onSwiped(int newIndex) {
        Log.v(TAG, "onSwiped: " + gameModelView.getWordIdx().getValue() + "->" + newIndex);
        gameModelView.getWordIdx().setValue(newIndex);
        if (gameModelView.getIsPartnerB().getValue()) {
            gameModelView.incCurrSkipCountB();
//            currSkipCountB++;
        } else {
            gameModelView.incCurrSkipCountA();
//            currSkipCountA++;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DownloadFragment downloadFragment = (DownloadFragment) getSupportFragmentManager().findFragmentByTag(GK.DOWNLOAD_FRAGMENT);
        if (downloadFragment != null && downloadFragment.isComplete()) {
            loadingIcon.setVisibility(View.INVISIBLE);
        }
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

    /**
     * Helper method that updates the various components of the view with the current values of the
     * variables that back them.
     */
    @SuppressLint("SetTextI18n")
    private void updateDisplay() {
        roundView.setText(String.format("%c%s",'#',Integer.toString(gameModelView.getCurrRound().getValue())));
        scoreView.setText(Integer.toString(gameModelView.getTotalScore1().getValue()) + ':' + gameModelView.getTotalScore2().getValue());
        if(!gameModelView.getIsPartnerB().getValue())
            partnerLetterView.setText("A");
        else
            partnerLetterView.setText("B");
        if(!gameModelView.getIsTeam2().getValue())
            teamNameView.setText(gameModelView.getTeamName1().getValue());
        else
            teamNameView.setText(gameModelView.getTeamName2().getValue());
    }

    // State transition called when the AcceptButton is pressed
    private void onAcceptWord() {
        Log.v(TAG, "Word Accepted");
        acceptWordButton.setVisibility(View.INVISIBLE);
        startPlaying();
    }

    // State transition to the actual guessing/playing stage
    private void startPlaying() {
        gameModelView.getGameState().setValue(GameState.PLAYING);
//        gameState = GameState.PLAYING;

        wordHolder.setText(gameModelView.getWordList().getValue()[viewPager.getCurrentItem()]);
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
                        gameModelView.getIsWordHidden().setValue(true);
//                        isWordHidden = true;
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
        timerView.setText(String.format(Locale.getDefault(),"%02d",Math.round(countDownTimeRemaining / 1000)));
        countDownTimer.start();
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


        // Get rid of the timer and reset it for next time we use it.
        timerView.setVisibility(View.INVISIBLE);
        countDownTimer.cancel();

        // If the guess was correct
        if (view.getId() == R.id.successButton) {
            storeResult();

            coverAlphaAnimator.cancel();
            coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",1f,0f);
            coverAlphaAnimator.setDuration(500);
            coverAlphaAnimator.start();

            // Score Addition Logic
            if (!gameModelView.getIsTeam2().getValue()) {
                gameModelView.getTotalScore1().setValue(
                        gameModelView.getTotalScore1().getValue() +
                                gameModelView.getCurrPP().getValue()
                );
//                totalScore1 += currPP;
            } else {
                gameModelView.getTotalScore2().setValue(
                        gameModelView.getTotalScore2().getValue() +
                                gameModelView.getCurrPP().getValue()
                );
//                totalScore2 += currPP;
            }

            // Next Turn Logic
            //-------CHANGE WORD HERE----------
            ppSpinnerView.resetSpinner();
            transitionToNextWord(true);
//            currPP = 10;
            gameModelView.getCurrPP().setValue(10);

            // Increment the round number if both sets of opposing players has played
            if (gameModelView.getIsPartnerB().getValue())
                gameModelView.incCurrRound();
//                currRound++;
            // Alternate which team begins each round
            gameModelView.getIsTeam2().setValue((gameModelView.getCurrRound().getValue() % 2) == 0);
//            isTeam2 = ((currRound % 2) == 0);
            // Flip back and forth between pairs of opposing players
            gameModelView.getIsPartnerB().setValue(!gameModelView.getIsPartnerB().getValue());
//            isPartnerB = !isPartnerB;

            // If the guess was incorrect
        } else if (view.getId() == R.id.failureButton) {
            storeResult();
            // Decrement current possible points
//            currPP--;
            gameModelView.decCurrPP();

            // The word was NEVER guessed and there are not more tries left
            // Next Turn Logic
            if (gameModelView.getCurrPP().getValue() < 1) {


                coverAlphaAnimator = ObjectAnimator.ofFloat(wordCover,"alpha",1f,0f);
                coverAlphaAnimator.setDuration(500);
                coverAlphaAnimator.start();

                // if the word was not guessed AT ALL
                //-------CHANGE WORD HERE----------
                ppSpinnerView.resetSpinner();
                transitionToNextWord(false);
//                currPP = 10;
                gameModelView.getCurrPP().setValue(10);

                // Increment the round number if both sets of opposing players has played
                if (gameModelView.getIsPartnerB().getValue())
                    gameModelView.incCurrRound();
//                    currRound++;
                // Alternate which team begins each round
//                isTeam2 = ((currRound % 2) == 0);
                gameModelView.getIsTeam2().setValue((gameModelView.getCurrRound().getValue() % 2) == 0);
                // Flip back and forth between pairs of opposing players
//                isPartnerB = !isPartnerB;
                gameModelView.getIsPartnerB().setValue(!gameModelView.getIsPartnerB().getValue());

            } else {// The word has not yet been correctly guessed but there are still chances left
                // Switch teams and decrement possible points
//                isTeam2 = !isTeam2;
                gameModelView.getIsTeam2().setValue(!gameModelView.getIsTeam2().getValue());
                ppSpinnerView.spinToNext();
                transitionToNextTeam();
            }
        }

        // Check if end of game
        if (gameModelView.getCurrRound().getValue() > NUM_ROUNDS) {
            // Game is Over
//            gameState = GameState.GAME_OVER;
            gameModelView.getGameState().setValue(GameState.GAME_OVER);

            // Create Winner Screen intent
            Intent winnerIntent = new Intent(this, WinnerActivity.class);

            // Determine who the winner is based off the final scores
            if (gameModelView.getTotalScore1().getValue() > gameModelView.getTotalScore2().getValue())
                winnerIntent.putExtra(GK.WINNER_TEAM_NAME, gameModelView.getTeamName1().getValue());
            else if (gameModelView.getTotalScore2().getValue() > gameModelView.getTotalScore1().getValue())
                winnerIntent.putExtra(GK.WINNER_TEAM_NAME, gameModelView.getTeamName2().getValue());

            // Attach the team names and necessary scoring values to the Winner Screen intent
            winnerIntent.putExtra(GK.A_SCORES_1, gameModelView.getaScores1().getValue());
            winnerIntent.putExtra(GK.A_SCORES_2, gameModelView.getaScores2().getValue());
            winnerIntent.putExtra(GK.B_SCORES_1, gameModelView.getbScores1().getValue());
            winnerIntent.putExtra(GK.B_SCORES_2, gameModelView.getbScores2().getValue());
            winnerIntent.putExtra(GK.A_WORDS, gameModelView.getaWords().getValue());
            winnerIntent.putExtra(GK.B_WORDS, gameModelView.getbWords().getValue());
            winnerIntent.putExtra(GK.TOTAL_SCORE_1, gameModelView.getTotalScore1().getValue());
            winnerIntent.putExtra(GK.TOTAL_SCORE_2, gameModelView.getTotalScore2().getValue());
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
            Log.d(TAG, "totalScore1: " + gameModelView.getTotalScore1().getValue());
            Log.d(TAG, "totalScore2 " + gameModelView.getTotalScore2().getValue());
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
        } else { // If not the end of the game
            updateDisplay();
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

    private void transitionToNextTeam() {
        gameModelView.getGameState().setValue(GameState.TEAM_TRANSITION);
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
            startPlaying();
        } else if (gameModelView.getGameState().getValue() == GameState.WORD_TRANSITION) {
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
     * Helper method to be called every time is word is completed
     * Updates the result variables based on who successfully guessed the word and how many points
     *  they earned.
     */
    private void storeResult() {
        TextView currentView = adapter.getCurrentView().findViewById(R.id.singleTextView);
        String currWord = currentView.getText().toString();
        if (gameModelView.getIsPartnerB().getValue()) {
            gameModelView.setbWordsElem(gameModelView.getCurrRound().getValue() - 1, currWord);
            if (gameModelView.getIsTeam2().getValue()) {
//                bScores1[currRound - 1] = 0;
//                bScores2[currRound - 1] = currPP;
                gameModelView.setbScores1Elem(gameModelView.getCurrRound().getValue() - 1,0);
                gameModelView.setbScores2Elem(gameModelView.getCurrRound().getValue() - 1, gameModelView.getCurrPP().getValue());
            } else {
//                bScores1[currRound - 1] = currPP;
//                bScores2[currRound - 1] = 0;
                gameModelView.setbScores1Elem(gameModelView.getCurrRound().getValue() - 1,gameModelView.getCurrPP().getValue());
                gameModelView.setbScores2Elem(gameModelView.getCurrRound().getValue() - 1, 0);
            }
        } else {
//            aWords[currRound - 1] = currWord;
            gameModelView.setaWordsElem(gameModelView.getCurrRound().getValue() - 1, currWord);
            if (gameModelView.getIsTeam2().getValue()) {
//                aScores1[currRound - 1] = 0;
//                aScores2[currRound - 1] = currPP;
                gameModelView.setaScores1Elem(gameModelView.getCurrRound().getValue() - 1, 0);
                gameModelView.setaScores2Elem(gameModelView.getCurrRound().getValue() - 1, gameModelView.getCurrPP().getValue());
            } else {
//                aScores1[currRound - 1] = currPP;
//                aScores2[currRound - 1] = 0;
                gameModelView.setaScores1Elem(gameModelView.getCurrRound().getValue() - 1, gameModelView.getCurrPP().getValue());
                gameModelView.setaScores2Elem(gameModelView.getCurrRound().getValue() - 1, 0);
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
//        previousCorrect = success;
        gameModelView.getPreviousCorrect().setValue(success);
//        if (success) {
//            layout.setBackgroundColor(Color.GREEN);
//
//        } else {
//            layout.setBackgroundColor(Color.RED);
//        }
//        gameState = GameState.WORD_TRANSITION;
        gameModelView.getGameState().setValue(GameState.WORD_TRANSITION);
    }

    /**
     * Advances the word-swiper to the next word
     */
    private void nextWord() {
        Log.v(TAG, "nextWord");
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
//        wordIdx++;
        gameModelView.incWordIdx();
    }

    /**
     * Callback method from the OneDirectionViewPager interface
     * @return whether or not to permit the word-swiper to swipe at the moment
     */
    @Override
    public boolean canSwipe() {
        boolean inApprovalState = (gameModelView.getGameState().getValue() == GameState.WORD_APPROVAL);
        Log.v(TAG, "canSwipe: " + inApprovalState);
        if (!inApprovalState) {
            return false;
        }
        // Returns whether or not the current word can be skipped.
        if (gameModelView.getIsPartnerB().getValue()) {
            return (gameModelView.getCurrPP().getValue() == 10) && gameModelView.getCurrSkipCountB().getValue() < 5;
        } else {
            return (gameModelView.getCurrPP().getValue() == 10) && gameModelView.getCurrSkipCountA().getValue() < 5;
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
        Button incorrectButton = findViewById(R.id.failureButton);
        guessMade(incorrectButton);
    }
}
