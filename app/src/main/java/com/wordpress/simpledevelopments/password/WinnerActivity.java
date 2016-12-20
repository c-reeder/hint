package com.wordpress.simpledevelopments.password;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class WinnerActivity extends AppCompatActivity {

    private static final String TAG = "WinnerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);
        Intent parentIntent = getIntent();
        Log.d(TAG, parentIntent.getStringExtra("winnerTeamName"));
        TextView winnerView = (TextView) findViewById(R.id.winnerText);

        if (parentIntent.getStringExtra("winnerTeamName") != null) {
            winnerView.setText(parentIntent.getStringExtra("winnerTeamName"));
        } else {
            winnerView.setText("It's a Tie!!!");
        }

    }
}
