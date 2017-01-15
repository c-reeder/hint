package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Created by connor on 1/14/17.
 */

public class CustomProgressBar extends ProgressBar {
    public CustomProgressBar(Context context) {
        super(context);
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.progressBarStyleLarge);
    }
}
