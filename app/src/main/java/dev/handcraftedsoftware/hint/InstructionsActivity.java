package dev.handcraftedsoftware.hint;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

/**
 * InstructionsActivity is the screen which shows the instructions for the game.
 * @author Connor Reeder
 */
public class InstructionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Typeface blendaTypeface = ResourcesCompat.getFont(this, R.font.blenda_script);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitle(getString(R.string.game_instructions));
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.InstructionsAppBarTextAppearanceCollapsed);
        collapsingToolbarLayout.setCollapsedTitleTypeface(blendaTypeface);

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.InstructionsAppBarTextAppearanceExpanded);
        collapsingToolbarLayout.setExpandedTitleTypeface(blendaTypeface);

        Button tutorialButton = findViewById(R.id.tutorialButton);
        tutorialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InstructionsActivity.this, TutorialActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(InstructionsActivity.this).toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

    }
}
