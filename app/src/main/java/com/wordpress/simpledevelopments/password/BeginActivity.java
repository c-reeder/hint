package com.wordpress.simpledevelopments.password;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class BeginActivity extends AppCompatActivity {

    private static final String TAG = "TurnActivity";

    private String teamName1;
    private String teamName2;
    private EditText nameText1;
    private EditText nameText2;
    private RadioGroup diffGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
        Intent parentIntent = getIntent();
        nameText1 = (EditText) findViewById(R.id.team1NameBox);
        nameText2 = (EditText) findViewById(R.id.team2NameBox);
        diffGroup = (RadioGroup) findViewById(R.id.diffGroup);

        if (parentIntent.getStringExtra("teamName1") != null) {
            teamName1 = parentIntent.getStringExtra("teamName1");
            nameText1.setText(teamName1);
        } else {
            Log.d(TAG, "teamName1 not passed correctly!");
        }

        if (parentIntent.getStringExtra("teamName2") != null) {
            teamName2 = parentIntent.getStringExtra("teamName2");
            nameText2.setText(teamName2);
        } else {
            Log.d(TAG, "teamName2 not passed correctly!");
        }

        if (parentIntent.getStringExtra("difficulty") != null) {
            String difficulty = parentIntent.getStringExtra("difficulty");
            if (difficulty.equals("easy"))
                diffGroup.check(R.id.easyButton);
            else if (difficulty.equals("medium"))
                diffGroup.check(R.id.mediumButton);
            else if (difficulty.equals("hard"))
                diffGroup.check(R.id.hardButton);
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        //setToFullScreen();
    }
    public void begin(View view) {
        Intent intent = new Intent(this, TurnActivity.class);
        intent.putExtra("teamName1", nameText1.getText().toString());
        intent.putExtra("teamName2", nameText2.getText().toString());
        RadioButton selected = (RadioButton) findViewById(diffGroup.getCheckedRadioButtonId());
        intent.putExtra("difficulty", selected.getText().toString().toLowerCase());
        startActivity(intent);
    }
}
