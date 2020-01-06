package dev.handcraftedsoftware.hint;

import android.graphics.Typeface;
import android.os.Bundle;

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
        collapsingToolbarLayout.setTitle("Game Instructions");
        collapsingToolbarLayout.setCollapsedTitleTypeface(blendaTypeface);
        collapsingToolbarLayout.setExpandedTitleTypeface(blendaTypeface);

    }
}
