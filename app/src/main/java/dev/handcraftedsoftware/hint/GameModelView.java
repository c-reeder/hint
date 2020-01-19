package dev.handcraftedsoftware.hint;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONException;

import static dev.handcraftedsoftware.hint.GameActivity.NUM_ROUNDS;

public class GameModelView extends AndroidViewModel implements OneDirectionViewPager.SwipeController {

    // Values Constant for the Entirety of one Game
    //private boolean inPlay;
    private MutableLiveData<String> teamName1;
    private MutableLiveData<String> teamName2;
    private MutableLiveData<String> difficulty;
    private MutableLiveData<String> language;
    private MutableLiveData<String[]> wordList;

    // Ever-Changing "Current" Variables
    private MutableLiveData<Integer> currRound;
    private MutableLiveData<Integer> currPP;
    private MutableLiveData<Boolean> isPartnerB;
    private MutableLiveData<Boolean> isTeam2;
    private MutableLiveData<Integer[]> totalScores;
    private MutableLiveData<Integer> currSkipCountA;
    private MutableLiveData<Integer> currSkipCountB;
    private MutableLiveData<Boolean> previousCorrect;
    private MutableLiveData<GameState> gameState;
    private MutableLiveData<Integer> wordIdx;
    private MutableLiveData<Boolean> isWordHidden;

    // Results Variables to be Passed to the Winner Screen
    private MutableLiveData<String[]> aWords;
    private MutableLiveData<String[]> bWords;
    private MutableLiveData<Integer[]> aScores1;
    private MutableLiveData<Integer[]> aScores2;
    private MutableLiveData<Integer[]> bScores1;
    private MutableLiveData<Integer[]> bScores2;

    private MutableLiveData<Long> countDownTimeRemaining;

    SharedPreferences sharedPreferences;
    JSONTask jsonTask;

    private final static String TAG = "GameModelView";

    public GameModelView(@NonNull Application application) {
        super(application);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        language = new MutableLiveData<String>(sharedPreferences.getString(GK.LANGUAGE, GV.ENGLISH));
        difficulty = new MutableLiveData<String>(sharedPreferences.getString(GK.DIFFICULTY,GV.EASY));
        aWords = new MutableLiveData<String[]>(new String[NUM_ROUNDS]);
        bWords = new MutableLiveData<String[]>(new String[NUM_ROUNDS]);
        aScores1 = new MutableLiveData<Integer[]>(new Integer[NUM_ROUNDS]);
        aScores2 = new MutableLiveData<Integer[]>(new Integer[NUM_ROUNDS]);
        bScores1 = new MutableLiveData<Integer[]>(new Integer[NUM_ROUNDS]);
        bScores2 = new MutableLiveData<Integer[]>(new Integer[NUM_ROUNDS]);
        previousCorrect = new MutableLiveData<Boolean>();
        init();
    }

    @SuppressLint("StaticFieldLeak")
    private void init() {

        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            //Define behavior to occur upon receiving the JSON word data
            jsonTask = new JSONTask() {
                @Override
                protected void onPostExecute(String result) {
                    onDownloadComplete(result);
                }
            };
            String requestURL = dev.handcraftedsoftware.hint.BuildConfig.url + "/words/" + language.getValue() + "/" + difficulty.getValue();
            jsonTask.execute(requestURL);
            Log.v(TAG,"request URL: " + requestURL);
        } else {
            Log.e(TAG, "Not connected to network");
        }
    }

    private void onDownloadComplete(String result) {
        Log.v(TAG, "onDownloadComplete");
        try {
            String[] newList = new String[22];
            JSONArray response = new JSONArray(result);
            for (int i = 0; i < response.length(); i++) {
                newList[i] = response.getString(i);
            }
            wordList.setValue(newList);
            if (response.length() != 22) throw new AssertionError("DID NOT GET 22 WORDS!!!");
            gameState.setValue(GameState.WORD_APPROVAL);
            //Game has now begun
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Contents of Response: ");
            Log.e(TAG, result);
            gameState.setValue(GameState.DOWNLOAD_ERROR);

        }
    }

    public MutableLiveData<String> getTeamName1() {
        if (teamName1 == null) {
            teamName1 = new MutableLiveData<String>(sharedPreferences.getString(GK.TEAM_NAME_1, getApplication().getString(R.string.team1)));
        }
        return teamName1;
    }

    public MutableLiveData<String> getTeamName2() {
        if (teamName2 == null) {
            teamName2 = new MutableLiveData<String>(sharedPreferences.getString(GK.TEAM_NAME_2, getApplication().getString(R.string.team2)));
        }
        return teamName2;
    }

    public MutableLiveData<String> getDifficulty() {
        return difficulty;
    }

    public MutableLiveData<String> getLanguage() {
        return language;
    }

    public MutableLiveData<String[]> getWordList() {
        if (wordList == null) {
            wordList = new MutableLiveData<String[]>();
        }
        return wordList;
    }

    public MutableLiveData<Integer> getCurrRound() {
        if (currRound == null) {
            currRound = new MutableLiveData<Integer>(1);
        }
        return currRound;
    }

    public void incCurrRound() {
        currRound.setValue(currRound.getValue() + 1);
    }

    public MutableLiveData<Integer> getCurrPP() {
        if (currPP == null) {
            currPP = new MutableLiveData<Integer>(10);
        }
        return currPP;
    }

    public void decCurrPP() {
        currPP.setValue(currPP.getValue() - 1);
    }

    public MutableLiveData<Boolean> getIsPartnerB() {
        if (isPartnerB == null) {
            isPartnerB = new MutableLiveData<Boolean>(false);
        }
        return isPartnerB;
    }

    public MutableLiveData<Boolean> getIsTeam2() {
        if (isTeam2 == null) {
            isTeam2 = new MutableLiveData<Boolean>(false);
        }
        return isTeam2;
    }

    public void setIsTeam2(boolean isNowTeam2) {
        isTeam2.setValue(isNowTeam2);
    }

    public MutableLiveData<Integer[]> getTotalScores() {
        if (totalScores == null) {
            totalScores = new MutableLiveData<Integer[]>(new Integer[]{0,0});
        }
        return totalScores;
    }

    public MutableLiveData<Integer> getCurrSkipCountA() {
        if (currSkipCountA == null) {
            currSkipCountA = new MutableLiveData<Integer>(0);
        }
        return currSkipCountA;
    }
    public void incCurrSkipCountA() {
        currSkipCountA.setValue(currSkipCountA.getValue() + 1);
    }
    public void incCurrSkipCountB() {
        currSkipCountB.setValue(currSkipCountB.getValue() + 1);
    }

    public MutableLiveData<Integer> getCurrSkipCountB() {
        if (currSkipCountB == null) {
            currSkipCountB = new MutableLiveData<Integer>(0);
        }
        return currSkipCountB;
    }

    public MutableLiveData<Boolean> getPreviousCorrect() {
        return previousCorrect;
    }

    public MutableLiveData<GameState> getGameState() {
        if (gameState == null) {
            gameState = new MutableLiveData<GameState>(GameState.AWAITING_WORDS);
        }
        return gameState;
    }

    public void setGameState(GameState newGameState) {
        gameState.setValue(newGameState);
    }

    public MutableLiveData<Integer> getWordIdx() {
        if (wordIdx == null) {
            wordIdx = new MutableLiveData<Integer>(0);
        }
        return wordIdx;
    }

    public void setWordIdx(int newWordIdx) {
        wordIdx.setValue(newWordIdx);
    }

    public void incWordIdx() {
        wordIdx.setValue(wordIdx.getValue() + 1);
    }

    public MutableLiveData<Boolean> getIsWordHidden() {
        if (isWordHidden == null) {
            isWordHidden = new MutableLiveData<Boolean>(false);
        }
        return isWordHidden;
    }

    public void setIsWordHidden(boolean isNowHidden) {
        isWordHidden.setValue(isNowHidden);
    }

    public MutableLiveData<String[]> getaWords() {
        return aWords;
    }
    public void setaWordsElem(int pos, String val) {
        aWords.getValue()[pos] = val;
        aWords.setValue(aWords.getValue());
    }

    public MutableLiveData<String[]> getbWords() {
        return bWords;
    }

    public void setbWordsElem(int pos, String val) {
        bWords.getValue()[pos] = val;
        bWords.setValue(bWords.getValue());
    }

    public MutableLiveData<Integer[]> getaScores1() {
        return aScores1;
    }

    public MutableLiveData<Integer[]> getaScores2() {
        return aScores2;
    }

    public MutableLiveData<Integer[]> getbScores1() {
        return bScores1;
    }


    public MutableLiveData<Integer[]> getbScores2() {
        return bScores2;
    }

    public MutableLiveData<Long> getCountDownTimeRemaining() {
        if (countDownTimeRemaining == null) {
            countDownTimeRemaining = new MutableLiveData<Long>(31000L);
        }
        return countDownTimeRemaining;
    }
    public void setCountDownTimeRemaining(long millis) {
        countDownTimeRemaining.setValue(millis);

    }

    public void resetCountDownTimeRemaining() {
        countDownTimeRemaining.setValue(31000L);
    }

    public void scoreCorrectAnswer() {
        // Score Addition Logic
        if (!isTeam2.getValue()) {
            totalScores.getValue()[0] += currPP.getValue();
            totalScores.setValue(totalScores.getValue());
        } else {
            totalScores.getValue()[1] += currPP.getValue();
            totalScores.setValue(totalScores.getValue());
        }

        previousCorrect.setValue(true);
        gameState.setValue(GameState.WORD_TRANSITION);
        currPP.setValue(10);

        // Increment the round number if both sets of opposing players has played
        if (isPartnerB.getValue())
            incCurrRound();

        // Alternate which team begins each round
        isTeam2.setValue((currRound.getValue() % 2) == 0);
        // Flip back and forth between pairs of opposing players
        isPartnerB.setValue(!isPartnerB.getValue());
    }

    public void scoreIncorrectAnswer() {
        currPP.setValue(10);

    }


    /**
     * Called every time is word is completed
     * Updates the result variables based on who successfully guessed the word and how many points
     *  they earned.
     */
    public void storeResult(String completedWord) {
        if (isPartnerB.getValue()) {
            bWords.getValue()[currRound.getValue() - 1] = completedWord;
            bWords.setValue(bWords.getValue());
            if (isTeam2.getValue()) {
                bScores1.getValue()[currRound.getValue() - 1] = 0;
                bScores2.getValue()[currRound.getValue() - 1] = currPP.getValue();
            } else {
                bScores1.getValue()[currRound.getValue() - 1] = currPP.getValue();
                bScores2.getValue()[currRound.getValue() - 1] = 0;
            }
            bScores1.setValue(bScores1.getValue());
            bScores2.setValue(bScores2.getValue());
        } else {
            aWords.getValue()[currRound.getValue() - 1] = completedWord;
            aWords.setValue(aWords.getValue());
            if (isTeam2.getValue()) {
                aScores1.getValue()[currRound.getValue() - 1] = 0;
                aScores2.getValue()[currRound.getValue() - 1] = currPP.getValue();
            } else {
                aScores1.getValue()[currRound.getValue() - 1] = currPP.getValue();
                aScores2.getValue()[currRound.getValue() - 1] = 0;
            }
            aScores1.setValue(aScores1.getValue());
            aScores2.setValue(aScores2.getValue());
        }
    }

    /**
     * Flip back and forth between pairs of opposing players
     */
    public void flipPartnerLetter() {
        isPartnerB.setValue(!isPartnerB.getValue());
    }

    public void switchTeams() {
        if (gameState.getValue() == GameState.PLAYING) {
            isTeam2.setValue(!isTeam2.getValue());
            gameState.setValue(GameState.TEAM_TRANSITION);
        }
    }

    public boolean isTeam1ScoreGreater() {
        return totalScores.getValue()[0] > totalScores.getValue()[1];
    }

    public boolean isTeam2ScoreGreater() {
        return totalScores.getValue()[1] > totalScores.getValue()[0];
    }


    /**
     * Callback method from the OneDirectionViewPager interface
     * @return whether or not to permit the word-swiper to swipe at the moment
     */
    @Override
    public boolean canSwipe() {
        if (gameState.getValue() != GameState.WORD_APPROVAL) {
            return false;
        }
        // Returns whether or not the current word can be skipped.
        if (isPartnerB.getValue()) {
            return (currPP.getValue() == 10) && currSkipCountB.getValue() < 5;
        } else {
            return (currPP.getValue() == 10) && currSkipCountA.getValue() < 5;
        }
    }


    /**
     * Callback Method implementing the OneDirectionViewPager which is called upon a swipe being performed.
     * In this case we are using it to update the counts of how many times each set of opposing players has skipped a word
     * @param newIndex the index of the OneDirectionViewPager after being swiped.
     */
    @Override
    public void onSwiped(int newIndex) {
        Log.v(TAG, "onSwiped: " + wordIdx.getValue() + "->" + newIndex);
        wordIdx.setValue(newIndex);
        if (isPartnerB.getValue()) {
            incCurrSkipCountB();
        } else {
            incCurrSkipCountA();
        }
    }
}
