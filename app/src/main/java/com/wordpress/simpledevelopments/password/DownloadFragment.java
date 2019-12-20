package com.wordpress.simpledevelopments.password;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;

/**
 * A non-UI fragment that persists between configuration changes in order to
 * manage the downloading of the words for the game.
 * Created by connor on 9/26/17.
 */

public class DownloadFragment extends Fragment {
    private static final String TAG = "DownloadFragment";

    public interface OnDownloadCompleteListener {
        public void onDownloadComplete(String result);
    }

    OnDownloadCompleteListener listenerActivity;
    JSONTask task;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle arguments = getArguments();
        String language = arguments.getString(GK.LANGUAGE);
        String difficulty = arguments.getString(GK.DIFFICULTY);

        // Check Network Status and if connected perform GET request to acquire word list from server
        // There should be 22 words. 2 Words * 6 Rounds + 5 Word-Skips * 2 Teams = 22 Words
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            // Define behavior to occur upon receiving the JSON word data
            task = new JSONTask() {
                @Override
                protected void onPostExecute(String result) {
                    listenerActivity.onDownloadComplete(result);
                }
            };
                task.execute("https://wordvault-heroku-18.herokuapp.com/passwords/" + language + "/" + difficulty);
//            task.execute("http://192.168.50.24:5000/passwords/" + language + "/" + difficulty);
                Log.v(TAG, "URL: " + "https://wordvault-heroku-18.herokuapp.com/passwords/" + language + "/" + difficulty);
//            Log.v(TAG, "URL: " + "http://192.168.50.24:5000/passwords/" + language + "/" + difficulty);
        } else {
            Log.e(TAG, "Not connected to network");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listenerActivity = (OnDownloadCompleteListener) context;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isComplete() {
        return task.getStatus() == AsyncTask.Status.FINISHED;
    }
}
