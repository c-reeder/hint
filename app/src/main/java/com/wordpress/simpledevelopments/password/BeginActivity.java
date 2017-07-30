package com.wordpress.simpledevelopments.password;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class BeginActivity extends AppCompatActivity {

    private static final String TAG = "TurnActivity";

    private String teamName1;
    private String teamName2;
    private EditText nameText1;
    private EditText nameText2;
    private RadioGroup diffGroup;
    private RadioGroup langGroup;
    private ImageButton helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
        Intent parentIntent = getIntent();
        nameText1 = (EditText) findViewById(R.id.team1NameBox);
        nameText2 = (EditText) findViewById(R.id.team2NameBox);
        diffGroup = (RadioGroup) findViewById(R.id.diffGroup);
        langGroup = (RadioGroup) findViewById(R.id.langGroup);
        helpButton = (ImageButton) findViewById(R.id.helpButton);

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
                startActivity(intent);
            }
        });
        
        nameText1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d(TAG, "onFocusChange: edit text 1 " + b + " -----------------------------");
                if (!b)
                    setToFullScreen();
            }
        });

        nameText2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d(TAG, "onFocusChange: edit text 2 " + b + " -----------------------------");
                if (!b)
                    setToFullScreen();
            }
        });

        if (parentIntent.getStringExtra(GV.TEAM_NAME_1) != null) {
            teamName1 = parentIntent.getStringExtra(GV.TEAM_NAME_1);
            nameText1.setText(teamName1);
        } else {
            Log.d(TAG, "teamName1 not passed correctly!");
        }

        if (parentIntent.getStringExtra(GV.TEAM_NAME_2) != null) {
            teamName2 = parentIntent.getStringExtra(GV.TEAM_NAME_2);
            nameText2.setText(teamName2);
        } else {
            Log.d(TAG, "teamName2 not passed correctly!");
        }

        if (parentIntent.getStringExtra(GV.DIFFICULTY) != null) {
            String difficulty = parentIntent.getStringExtra(GV.DIFFICULTY);
            if (difficulty.equals("easy"))
                diffGroup.check(R.id.easyButton);
            else if (difficulty.equals("medium"))
                diffGroup.check(R.id.mediumButton);
            else if (difficulty.equals("hard"))
                diffGroup.check(R.id.hardButton);
        }

        if (parentIntent.getStringExtra(GV.LANGUAGE) != null) {
            String difficulty = parentIntent.getStringExtra(GV.LANGUAGE);
            if (difficulty.equals("english"))
                diffGroup.check(R.id.easyButton);
            else if (difficulty.equals("español"))
                diffGroup.check(R.id.mediumButton);
        }

        //Ensure that we do not lose fullscreen mode upon entering text
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    setToFullScreen();
                }
            }
        });
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
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public void begin(View view) {
        Intent intent = new Intent(this, TurnActivity.class);
        intent.putExtra(GV.TEAM_NAME_1, nameText1.getText().toString());
        intent.putExtra(GV.TEAM_NAME_2, nameText2.getText().toString());
        RadioButton selectedDiff = (RadioButton) findViewById(diffGroup.getCheckedRadioButtonId());
        RadioButton selectedLang = (RadioButton) findViewById(langGroup.getCheckedRadioButtonId());
        if (selectedDiff.getId() == R.id.easyButton) {
            intent.putExtra(GV.DIFFICULTY, "easy");
        } else if (selectedDiff.getId() == R.id.mediumButton) {
            intent.putExtra(GV.DIFFICULTY, "medium");
        } else if (selectedDiff.getId() == R.id.hardButton) {
            intent.putExtra(GV.DIFFICULTY, "hard");
        }
        intent.putExtra(GV.LANGUAGE, selectedLang.getText().toString().toLowerCase());
        startActivity(intent);
    }
}
