package dev.handcraftedsoftware.hint;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


/**
 * Initial Activity upon opening the app.
 * Allows the user to choose team names and difficulty for the game.
 * @author Connor Reeder
 */
public class BeginActivity extends AppCompatActivity {

    private static final String TAG = "BeginActivity";

    private EditText nameText1;
    private EditText nameText2;
    private RadioGroup diffGroup;
    private RadioGroup langGroup;
    private Button beginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }

        setContentView(R.layout.activity_begin);
        Intent parentIntent = getIntent();
        nameText1 = findViewById(R.id.team1NameBox);
        nameText2 = findViewById(R.id.team2NameBox);
        diffGroup = findViewById(R.id.diffGroup);
        langGroup = findViewById(R.id.langGroup);
        Button helpButton = findViewById(R.id.helpButton);
        beginButton = findViewById(R.id.beginButton);

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
                startActivity(intent);
            }
        });
        beginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                begin(beginButton);
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

        if (savedInstanceState == null) {
            if (parentIntent.getStringExtra(GK.TEAM_NAME_1) != null) {
                nameText1.setText(parentIntent.getStringExtra(GK.TEAM_NAME_1));
            } else {
                Log.d(TAG, "teamName1 not passed correctly!");
            }

            if (parentIntent.getStringExtra(GK.TEAM_NAME_2) != null) {
                nameText2.setText(parentIntent.getStringExtra(GK.TEAM_NAME_2));
            } else {
                Log.d(TAG, "teamName2 not passed correctly!");
            }

            if (parentIntent.getStringExtra(GK.DIFFICULTY) != null) {
                String difficulty = parentIntent.getStringExtra(GK.DIFFICULTY);
                switch (difficulty) {
                    case GV.EASY:
                        diffGroup.check(R.id.easyButton);
                        break;
                    case GV.MEDIUM:
                        diffGroup.check(R.id.mediumButton);
                        break;
                    case GV.HARD:
                        diffGroup.check(R.id.hardButton);
                        break;
                }
            }

            if (parentIntent.getStringExtra(GK.LANGUAGE) != null) {
                String lang = parentIntent.getStringExtra(GK.LANGUAGE);
                if (lang.equals(GV.ENGLISH))
                    langGroup.check(R.id.englishButton);
                else if (lang.equals(GV.SPANISH))
                    langGroup.check(R.id.spanishButton);
            }
        } else {
            nameText1.setText(savedInstanceState.getString(GK.TEAM_NAME_1));
            nameText2.setText(savedInstanceState.getString(GK.TEAM_NAME_2));

            String difficulty = savedInstanceState.getString(GK.DIFFICULTY);
            assert difficulty != null;
            switch (difficulty) {
                case GV.EASY:
                    diffGroup.check(R.id.easyButton);
                    break;
                case GV.MEDIUM:
                    diffGroup.check(R.id.mediumButton);
                    break;
                case GV.HARD:
                    diffGroup.check(R.id.hardButton);
                    break;
            }

            String lang = savedInstanceState.getString(GK.LANGUAGE);
            if (lang != null) {
                if (lang.equals(GV.ENGLISH))
                    langGroup.check(R.id.englishButton);
                else if (lang.equals(GV.SPANISH))
                    langGroup.check(R.id.spanishButton);
            } else {
                langGroup.check(R.id.englishButton);
            }

        }

        if (BuildConfig.FLAVOR.equals("free")) {
            FrameLayout frameLayout = findViewById(R.id.adFrame);
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });
            AdView adView = (AdView) frameLayout.getChildAt(0);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            Log.d(TAG, "not free");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString(GK.TEAM_NAME_1, nameText1.getText().toString());
        savedInstanceState.putString(GK.TEAM_NAME_2, nameText2.getText().toString());

        switch (diffGroup.getCheckedRadioButtonId()) {
            case R.id.easyButton:
                savedInstanceState.putString(GK.DIFFICULTY, GV.EASY);
                break;
            case R.id.mediumButton:
                savedInstanceState.putString(GK.DIFFICULTY, GV.MEDIUM);
                break;
            case R.id.hardButton:
                savedInstanceState.putString(GK.DIFFICULTY, GV.HARD);
                break;
        }

        if (langGroup.getCheckedRadioButtonId() == R.id.englishButton)
            savedInstanceState.putString(GK.LANGUAGE, GV.ENGLISH);
        else if (langGroup.getCheckedRadioButtonId() == R.id.spanishButton)
            savedInstanceState.putString(GK.LANGUAGE, GV.SPANISH);

        super.onSaveInstanceState(savedInstanceState);
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
        int value = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            value = value | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(value);
    }


    private void begin(View view) {
        Intent intent = new Intent(this, TurnActivity.class);
        intent.putExtra(GK.TEAM_NAME_1, nameText1.getText().toString());
        intent.putExtra(GK.TEAM_NAME_2, nameText2.getText().toString());
        RadioButton selectedDiff = findViewById(diffGroup.getCheckedRadioButtonId());
        RadioButton selectedLang = findViewById(langGroup.getCheckedRadioButtonId());
        if (selectedDiff.getId() == R.id.easyButton) {
            intent.putExtra(GK.DIFFICULTY, GV.EASY);
        } else if (selectedDiff.getId() == R.id.mediumButton) {
            intent.putExtra(GK.DIFFICULTY, GV.MEDIUM);
        } else if (selectedDiff.getId() == R.id.hardButton) {
            intent.putExtra(GK.DIFFICULTY, GV.HARD);
        }
        if (selectedLang.getId() == R.id.englishButton) {
            intent.putExtra(GK.LANGUAGE, GV.ENGLISH);
        } else if (selectedLang.getId() == R.id.spanishButton) {
            intent.putExtra(GK.LANGUAGE, GV.SPANISH);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(intent);
        }
    }
}
