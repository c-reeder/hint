package dev.handcraftedsoftware.hint;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import dev.handcraftedsoftware.hint.R;

public class TestActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

    }

    public void start(View view) {
//        ObjectAnimator animation = ObjectAnimator.ofInt (pb, "progress", 0, 100); // see this max value coming back here, we animale towards that value
//        animation.setDuration (4000); //in milliseconds
//        animation.setInterpolator (new LinearInterpolator());
//        animation.start();
    }
}
