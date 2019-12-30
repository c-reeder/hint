package dev.handcraftedsoftware.hint;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adaptor that is used to display a swipeable word in the ViewPager of the TurnActivity.
 * @author Connor Reeder
 */

class TextPagerAdapter extends PagerAdapter {

    private Context context;
    private List<String> textList;
    private ViewGroup currentView;

    TextPagerAdapter(Context context, String[] textList) {
        this.context = context;
        this.textList = new ArrayList<>(textList.length);
        Collections.addAll(this.textList, textList);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup newbie = (ViewGroup) inflater.inflate(R.layout.single_text, container, false);
        TextView singleTextView = newbie.findViewById(R.id.singleTextView);
        singleTextView.setText(textList.get(position));
        container.addView(newbie);
        return newbie;
    }

    @Override
    public void destroyItem (ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


    @Override
    public int getCount() {
        return textList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        currentView = (ViewGroup) object;
        super.setPrimaryItem(container,position,object);
    }
    ViewGroup getCurrentView() {
        return currentView;
    }
}
