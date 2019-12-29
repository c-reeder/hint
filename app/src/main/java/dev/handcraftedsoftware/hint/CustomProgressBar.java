package dev.handcraftedsoftware.hint;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * A custom perpetual progress bar to show while waiting for the words to arrive.
 * By Connor Reeder
 */

public class CustomProgressBar extends ProgressBar {
    public CustomProgressBar(Context context) {
        super(context);
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.progressBarStyleLarge);
    }
}
