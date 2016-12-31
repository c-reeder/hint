package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class TurnActivity extends AppCompatActivity implements OneDirectionViewPager.SwipeController, View.OnTouchListener {

    private static final String TAG = "TurnActivity";

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

    // Values Constant for the Entirety of one Game
    private boolean inPlay;
    private String teamName1;
    private String teamName2;
    private List<String> wordList;

    // Components of the Display
    private TextView roundView;
    private TextView scoreView;
    private TextView ppView;
    private TextView partnerLetterView;
    private TextView teamNameView;
    OneDirectionViewPager viewPager;
    TextPagerAdapter adapter;
    private GestureDetector gestureDetector;

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
        scoreView = (TextView) findViewById(R.id.scoreText);
        ppView = (TextView) findViewById(R.id.possPointsText);
        partnerLetterView = (TextView) findViewById(R.id.partnerLetterText);
        teamNameView = (TextView) findViewById(R.id.teamName);
        viewPager = (OneDirectionViewPager) findViewById(R.id.pager);
        viewPager.setSwipeController(this);

        // Init Game Values
        teamName1 = parentIntent.getStringExtra("teamName1");
        teamName2 = parentIntent.getStringExtra("teamName2");
        currRound = 1;
        currPP = 10;
        //currWord = "Magnificent";
        isPartnerB = false;
        isTeam2 = false;
        currScore1 = 0;
        currScore2 = 0;
        currSkipCountA = 0;
        currSkipCountB = 0;
        wordTransition = false;
        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                Log.d(TAG, "onSingleTapConfirmed");
                if (wordTransition) {
                    Log.d(TAG, "wordTransition");
                    nextWord();
                    wordTransition = false;
                    return true;
                } else {
                    Log.d(TAG, "Not wordTransition");
                    return false;
                }
            }
        });
        viewPager.setOnTouchListener(this);

        // Init Results Variables
        aWords = new String[5];
        bWords = new String[5];
        aScores1 = new int[5];
        aScores2 = new int[5];
        bScores1 = new int[5];
        bScores2 = new int[5];

        Log.d(TAG, "Getting Words!");
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            JSONTask task = new JSONTask() {
                @Override
                protected void onPostExecute(String result) {
                    try {
                        //Change later to statically sized array once server is updated
                        wordList = new ArrayList<>();
                        JSONArray response = new JSONArray(result);
                        for (int i = 0; i < response.length(); i++) {
                            //Log.v(TAG, "WORD: " + response.getString(i));
                            wordList.add(response.getString(i));
                        }
                        if (wordList.size() != 20) {
                            Log.e(TAG, "DID NOT GET 20 WORDS!!!");
                        }
                        Log.d(TAG, "Got " + wordList.size() + " words!");
                        ProgressBar loadingIcon = (ProgressBar) findViewById(R.id.progressBar);
                        loadingIcon.setVisibility(View.GONE);
                        initWords();
                        updateDisplay();
                        inPlay = true;
                        Log.d(TAG, "Beginning Game!");
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                    //Log.d(TAG, "RESULT: " + result);
                }
            };

            //task.execute("https://www.thegamegal.com/wordgenerator/generator.php?game=2&category=6");
            task.execute("https://wordvault.herokuapp.com/passwords");
        } else {
            Log.e(TAG, "Not connected to network");
        }


    }
    /**
     * Initialize the Word Viewpager (for swiping/skipping through words
     */
    public void initWords() {
        adapter = new TextPagerAdapter(this, wordList);
        viewPager.setAdapter(adapter);
        Log.d(TAG, "Words Initialized! " + viewPager.getCurrentItem());
    }
    @Override
    public void onSwiped(int newIndex) {
        Log.d(TAG, "onSwiped, newIndex: " + newIndex + ", " + viewPager.getCurrentItem());
        if (isPartnerB) {
            currSkipCountB++;
            Log.d(TAG, "Partner B swiped, new SkipCount is: " + currSkipCountB);
        } else {
            currSkipCountA++;
            Log.d(TAG, "Partner A swiped, new SkipCount is: " + currSkipCountA);
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
    private void updateDisplay() {
        roundView.setText("Round #" + currRound);
        scoreView.setText(currScore1 + ":" + currScore2);
        ppView.setText("" + currPP);
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
     * The method called when the guess has been made
     * @param view The button pressed to signal a guess has been made
     */
    public void guessMade(View view) {
        if (wordTransition || !inPlay)
            return;
        if (view.getId() == R.id.successButton) {
            Log.d(TAG, "Correct!");
            storeResult();

            // Score Addition Logic
            if (!isTeam2) {
                currScore1 += currPP;
            } else {
                currScore2 += currPP;
            }

            // Next Turn Logic
            //-------CHANGE WORD HERE----------
            transitionToNextWord(true);
            currPP = 10;
            if (isPartnerB)
                currRound++;
            isTeam2 = ((currRound % 2) == 0);//false;
            if (isTeam2)
                Log.d(TAG, "Now Team 2's Turn!: " + currRound);
            else
                Log.d(TAG, "Now Team 1's Turn!: " + currRound);
            isPartnerB = !isPartnerB;
        } else if (view.getId() == R.id.failureButton) {
            Log.d(TAG, "Failure!");
            storeResult();
            currPP--;

            // Next Turn Logic
            if (currPP < 1) {
                // if the word was not guessed AT ALL
                //-------CHANGE WORD HERE----------
                transitionToNextWord(false);
                currPP = 10;
                if (isPartnerB)
                    currRound++;
                isTeam2 = ((currRound % 2) == 0);//false;
                if (isTeam2)
                    Log.d(TAG, "Now Team 2's Turn!: " + currRound);
                else
                    Log.d(TAG, "Now Team 1's Turn!: " + currRound);
                isPartnerB = !isPartnerB;
            } else {
                isTeam2 = !isTeam2;
            }
        }

        // Check if end of game
        if (currRound > 5) {
            inPlay = false;
            //Launch Winner Activity
            Intent winnerIntent = new Intent(this, WinnerActivity.class);

            if (currScore1 > currScore2)
                winnerIntent.putExtra("winnerTeamName", teamName1);
            else if (currScore2 > currScore1)
                winnerIntent.putExtra("winnerTeamName", teamName2);

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

            startActivity(winnerIntent);
        } else
            updateDisplay();
    }

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


    private void transitionToNextWord(boolean success) {
        View currentView = adapter.getCurrentView();
        TextView textView = (TextView) currentView.findViewById(R.id.singleTextView);
        Log.d(TAG, "Word Complete: " + viewPager.getCurrentItem() + ", " + textView.getText().toString());
        if (success)
            currentView.setBackgroundColor(Color.GREEN);
        else
            currentView.setBackgroundColor(Color.RED);
        wordTransition = true;

    }

    private void nextWord() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

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
    private void successDialog() {
        AlertDialog.Builder aDBuilder = new AlertDialog.Builder(this);
        aDBuilder.setMessage("This is my message!");
        aDBuilder.setPositiveButton("yes",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(getApplicationContext(),"You clicked yes button",Toast.LENGTH_LONG).show();
                        }
            });
        aDBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"You clicked no button",Toast.LENGTH_LONG).show();
                }
            });
        AlertDialog alertDialog = aDBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        return false;
    }
}
