package dev.handcraftedsoftware.hint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class TutorialDialogFragment extends DialogFragment {
    private static final String TAG = "TutorialDialogFragment";
    private ActionsHandler handler;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] tutorialOptions = {getString(R.string.restart_game), getString(R.string.resume_game)};
        final TutorialDialogFragment.TutorialDialog tutorialDialog = new TutorialDialogFragment.TutorialDialog(getActivity());
        tutorialDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        tutorialDialog.setContentView(R.layout.dialog_tutorial);
        FragmentActivity fragmentActivity = getActivity();
        assert fragmentActivity != null;
        Resources resources = fragmentActivity.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int width = metrics.widthPixels;

        Window window = tutorialDialog.getWindow();
        if (window != null) {
            window.setLayout((int) (width * .9), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        Button noButton = tutorialDialog.findViewById(R.id.noButton);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Activity activity = getActivity();
                if (activity != null) {
                    activity.getPreferences(MODE_PRIVATE).edit().putBoolean(BeginActivity.FIRST_RUN_KEY, false).apply();
                }
                handler.startGame();
            }
        });
        Button yesButton = tutorialDialog.findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Activity activity = getActivity();
                if (activity != null) {
                    activity.getPreferences(MODE_PRIVATE).edit().putBoolean(BeginActivity.FIRST_RUN_KEY, false).apply();
                }
                handler.startTutorial();
            }
        });

        return tutorialDialog;


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        FragmentActivity fragmentActivity = getActivity();
        assert fragmentActivity != null;
        int value = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            value = value | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        fragmentActivity.getWindow().getDecorView().setSystemUiVisibility(value);
        return view;
    }

    private class TutorialDialog extends Dialog {

        TutorialDialog(Context context) {
            super(context);
        }

        @Override
        public void show() {
            // Set the dialog to not focusable.
            Window window = getWindow();
            if (window != null) {
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                copySystemUiVisibility();
                setCancelable(false);
                super.show();

                // Set the dialog to focusable again.
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }

        }
        private void copySystemUiVisibility() {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                Window window = getWindow();
                FragmentActivity fragmentActivity = getActivity();
                if (window != null && fragmentActivity != null) {
                    window.getDecorView().setSystemUiVisibility(
                            getActivity().getWindow().getDecorView().getSystemUiVisibility());
                }
            }
        }
    }

    private class TutorialAdapter extends ArrayAdapter<String> {

        TutorialAdapter(Context context, String[] tutorialOptions) {
            super(context, 0, tutorialOptions);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            String optionText = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_menu, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.menuItemText);

            textView.setText(optionText);
            return convertView;
        }
    }


    interface ActionsHandler {
        void startTutorial();
        void startGame();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: ");
        super.onAttach(context);
        try {
            handler = (TutorialDialogFragment.ActionsHandler) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " is not a ActionsHandler");
        }
    }
}
