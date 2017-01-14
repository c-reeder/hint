package com.wordpress.simpledevelopments.password;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by connor on 1/13/17.
 */

public class MenuFragment extends DialogFragment {
    public static final String TAG = "MenuFragment";
    private ArrayAdapter<String> menuOptionsAdapter;
    private static final String[] menuOptions = {"Restart Game", "Resume Game"};
    private MenuActionsHandler handler;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MenuDialog menuDialog = new MenuDialog(getActivity());
        menuDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        menuDialog.setContentView(R.layout.dialog_menu);
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        menuDialog.getWindow().setLayout((int) (width * .8), WindowManager.LayoutParams.WRAP_CONTENT);

        menuOptionsAdapter = new MenuAdapter(menuOptions);
        ListView optionsListView = (ListView) menuDialog.findViewById(R.id.optionsList);
        optionsListView.setAdapter(menuOptionsAdapter);
        optionsListView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case 0:
                        handler.restartGame();
                        break;
                    case 1:
                        dismiss();
                        handler.resumeGame();
                        break;
                }
            }
        });

        return menuDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        return view;
    }

    private class MenuDialog extends Dialog {

        public MenuDialog(Context context) {
            super(context);
        }

        @Override
        public void show() {
            // Set the dialog to not focusable.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            copySystemUiVisibility();
            setCancelable(false);
            super.show();

            // Set the dialog to focusable again.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        private void copySystemUiVisibility() {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                        getActivity().getWindow().getDecorView().getSystemUiVisibility());
            }
        }
    }

    private class MenuAdapter extends ArrayAdapter<String> {

        public MenuAdapter(String[] menuOptions) {
            super(getActivity(), 0, menuOptions);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String optionText = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_menu, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.menuItemText);

            textView.setText(optionText);
            return convertView;
        }
    }
    public interface MenuActionsHandler {
        public void restartGame();
        public void resumeGame();
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: ");
        super.onAttach(context);
        try {
            handler = (MenuActionsHandler) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " is not a MenuActionsHandler");
        }
    }
}
