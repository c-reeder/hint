package com.wordpress.simpledevelopments.password;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Asynchronous task that gets JSON data and returns it as a JSONArray
 * Connor Reeder
 */

public class JSONTask extends AsyncTask<String, Void, String> {
    private final String TAG = "JSONTask";

    @Override
    protected String doInBackground(String... urls) {
        InputStream is = null;
        String result;
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();
            is = new BufferedInputStream(connection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String recvd;
            while ((recvd = reader.readLine()) != null)
                builder.append(recvd);
            result = builder.toString();

        } catch (Exception e) {
            e.printStackTrace();
            result = "";
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

}
