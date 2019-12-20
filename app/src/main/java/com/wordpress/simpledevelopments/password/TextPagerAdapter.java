package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adaptor that is used to display a swipeable word in the ViewPager of the TurnActivity.
 * By Connor Reeder
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

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup newbie = (ViewGroup) inflater.inflate(R.layout.single_text, container, false);
        TextView singleTextView = (TextView) newbie.findViewById(R.id.singleTextView);
        singleTextView.setText(textList.get(position));
        container.addView(newbie);
        return newbie;
    }

    @Override
    public void destroyItem (ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


    @Override
    public int getCount() {
        return textList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        currentView = (ViewGroup) object;
        super.setPrimaryItem(container,position,object);
    }
    ViewGroup getCurrentView() {
        return currentView;
    }
}
