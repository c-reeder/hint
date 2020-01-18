package dev.handcraftedsoftware.hint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A non-UI fragment that persists between configuration changes in order to
 * manage the downloading of the words for the game.
 * @author Connor Reeder
 * 9/26/17.
 *
 */

public class DownloadFragment extends Fragment {
    private static final String TAG = "DownloadFragment";

    public interface OnDownloadCompleteListener {
        void onDownloadComplete(String result);
    }

    private OnDownloadCompleteListener listenerActivity;
    private JSONTask task;

//    private AsyncTask.Status TESTING_STATUS = AsyncTask.Status.PENDING;


    @SuppressLint("StaticFieldLeak")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

//        Bundle arguments = getArguments();
//        assert arguments != null;
        String language = sharedPreferences.getString(GK.LANGUAGE,GV.ENGLISH);
        String difficulty = sharedPreferences.getString(GK.DIFFICULTY, GV.EASY);
//        String language = arguments.getString(GK.LANGUAGE);
//        String difficulty = arguments.getString(GK.DIFFICULTY);

        // Check Network Status and if connected perform GET request to acquire word list from server
        // There should be 22 words. 2 Words * 6 Rounds + 5 Word-Skips * 2 Teams = 22 Words

        // For testing purposes
//        listenerActivity.onDownloadComplete("[\"Pond\",\"uncomfortable\",\"dude\",\"mascot\",\"cargo\",\"telephone booth\",\"albatross\",\"wheat\",\"paper clips\",\"photograph\",\"car dealership\",\"wipe\",\"snatch\",\"winter\",\"ratchet\",\"passport\",\"tiptoe\",\"lemon\",\"seat\",\"disc jockey\",\"succeed\",\"treatment\"]");
//        TESTING_STATUS = AsyncTask.Status.FINISHED;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listenerActivity = (OnDownloadCompleteListener) context;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    boolean isComplete() {
        // For testing purposes
//        return TESTING_STATUS == AsyncTask.Status.FINISHED;
        return task.getStatus() == AsyncTask.Status.FINISHED;
    }

}
