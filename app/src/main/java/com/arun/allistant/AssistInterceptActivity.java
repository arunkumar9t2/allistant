package com.arun.allistant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import static com.arun.allistant.shared.Constants.ACTION_LAUNCH_ASSISTANT;

public class AssistInterceptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_LAUNCH_ASSISTANT));
        finish();
    }
}
