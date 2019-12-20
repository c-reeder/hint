package com.wordpress.simpledevelopments.password;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * OneDirectionViewPager
 * By Connor Reeder
 */

public class OneDirectionViewPager extends ViewPager {

    private static final String TAG = "OneDirectionViewPager";

    private GestureDetector gestureDetector;
    private SwipeController swipeController;

    public OneDirectionViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
           @Override
           public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
               float delta = e2.getX() - e1.getX();
               if (delta < 0) {
                   //Log.d(TAG, "Slide!!!");
                   //Flip to Next Word
                   if (swipeController.canSwipe()) {
                       setCurrentItem(getCurrentItem() + 1, true);
                        swipeController.onSwiped(getCurrentItem());
                   } else
                       Log.d(TAG, "Cannot swipe!!!");
               }
               return false;
           }
        });
    }
    public void setSwipeController(SwipeController swipeController) {
        this.swipeController = swipeController;
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }


    /**
     * Interface defining the interaction between a OneDirectionViewPager and its containing Activity or Fragment
     */
    interface SwipeController {
        /**
         * Determines whether swiping is currently allowed on this OneDirectionalViewPager
         * @return whether or not swiping is allowed
         */
        boolean canSwipe();

        /**
         * Callback for when this OneDirectionViewPager has been swiped
         * @param newIndex the index of the OneDirectionViewPager after being swiped.
         */
        void onSwiped(int newIndex);
    }


}
