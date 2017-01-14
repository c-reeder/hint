package com.wordpress.simpledevelopments.password;

import android.app.FragmentTransaction;
import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TestActivity extends AppCompatActivity {

    public static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        /*FirstFragment fragment = FirstFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, fragment)
                .commit();*/


        Button switchButton = (Button) findViewById(R.id.switch_button);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "switchButton!");
                MyDialogFragment myDialogFragment = new MyDialogFragment();
                myDialogFragment.show(getFragmentManager(), "DIALOG_FRAGMENT");
                /*getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragmentContainer,myDialogFragment)
                        .commit();
                myDialogFragment.show(getSupportFragmentManager(), "test");*/

            }
        });
    }
}
