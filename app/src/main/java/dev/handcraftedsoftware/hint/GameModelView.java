package dev.handcraftedsoftware.hint;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static dev.handcraftedsoftware.hint.GameActivity.NUM_ROUNDS;

public class GameModelView extends AndroidViewModel {

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
    private MutableLiveData<Integer> totalScore1;
    private MutableLiveData<Integer> totalScore2;
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

    SharedPreferences sharedPreferences;

    public GameModelView(@NonNull Application application) {
        super(application);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
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
        if (difficulty == null) {
            difficulty = new MutableLiveData<String>(sharedPreferences.getString(GK.DIFFICULTY,GV.EASY));
        }
        return difficulty;
    }

    public MutableLiveData<String> getLanguage() {
        if (language == null) {
            language = new MutableLiveData<String>(sharedPreferences.getString(GK.LANGUAGE, GV.ENGLISH));
        }
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

    public MutableLiveData<Integer> getTotalScore1() {
        if (totalScore1 == null) {
            totalScore1 = new MutableLiveData<Integer>(0);
        }
        return totalScore1;
    }

    public MutableLiveData<Integer> getTotalScore2() {
        if (totalScore2 == null) {
            totalScore2 = new MutableLiveData<Integer>(0);
        }
        return totalScore2;
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
        if (previousCorrect == null) {
            previousCorrect = new MutableLiveData<Boolean>();
        }
        return previousCorrect;
    }

    public MutableLiveData<GameState> getGameState() {
        if (gameState == null) {
            gameState = new MutableLiveData<GameState>(GameState.AWAITING_WORDS);
        }
        return gameState;
    }

    public MutableLiveData<Integer> getWordIdx() {
        if (wordIdx == null) {
            wordIdx = new MutableLiveData<Integer>(0);
        }
        return wordIdx;
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

    public MutableLiveData<String[]> getaWords() {
        if (aWords == null) {
            aWords = new MutableLiveData<String[]>(new String[NUM_ROUNDS]);
        }
        return aWords;
    }
    public void setaWordsElem(int pos, String val) {
        aWords.getValue()[pos] = val;
        aWords.setValue(aWords.getValue());
    }

    public MutableLiveData<String[]> getbWords() {
        if (bWords == null) {
            bWords = new MutableLiveData<String[]>(new String[NUM_ROUNDS]);
        }
        return bWords;
    }

    public void setbWordsElem(int pos, String val) {
        bWords.getValue()[pos] = val;
        bWords.setValue(bWords.getValue());
    }

    public MutableLiveData<Integer[]> getaScores1() {
        if (aScores1 == null) {
            aScores1 = new MutableLiveData<Integer[]>(new Integer[NUM_ROUNDS]);
        }
        return aScores1;
    }
    public void setaScores1Elem(int pos, int val) {
        aScores1.getValue()[pos] = val;
        aScores1.setValue(aScores1.getValue());
    }

    public MutableLiveData<Integer[]> getaScores2() {
        if (aScores2 == null) {
            aScores2 = new MutableLiveData<Integer[]>(new Integer[NUM_ROUNDS]);
        }
        return aScores2;
    }

    public void setaScores2Elem(int pos, int val) {
        aScores2.getValue()[pos] = val;
        aScores2.setValue(aScores2.getValue());
    }

    public MutableLiveData<Integer[]> getbScores1() {
        if (bScores1 == null) {
            bScores1 = new MutableLiveData<Integer[]>(new Integer[NUM_ROUNDS]);
        }
        return bScores1;
    }

    public void setbScores1Elem(int pos, int val) {
        bScores1.getValue()[pos] = val;
        bScores1.setValue(bScores1.getValue());
    }

    public MutableLiveData<Integer[]> getbScores2() {
        if (bScores2 == null) {
            bScores2 = new MutableLiveData<Integer[]>(new Integer[NUM_ROUNDS]);
        }
        return bScores2;
    }

    public void setbScores2Elem(int pos, int val) {
        bScores2.getValue()[pos] = val;
        bScores2.setValue(bScores2.getValue());
    }
}
