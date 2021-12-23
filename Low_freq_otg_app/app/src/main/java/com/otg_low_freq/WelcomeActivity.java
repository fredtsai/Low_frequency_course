package com.otg_low_freq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;


public class WelcomeActivity extends Activity {

    private Button button;
    private static final int GOTO_SELECT_ACTIVITY = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessageDelayed(GOTO_SELECT_ACTIVITY, 200);
            }
        });
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent();
            switch (msg.what) {
                case GOTO_SELECT_ACTIVITY:
                    intent = new Intent();
                    //將原本Activity的換成MainActivity
                    intent.setClass(WelcomeActivity.this, MainActivity.class);

                    startActivity(intent);
                    WelcomeActivity.this.finish();
                    break;
                default:
                    break;
            }
        }

    };
}