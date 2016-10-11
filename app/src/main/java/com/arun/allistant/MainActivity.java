package com.arun.allistant;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.arun.allistant.shared.Constants;
import com.arun.allistant.shared.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.accessibility_error_layout)
    CardView accessibilityErrorLayout;
    @BindView(R.id.allo_error_layout)
    CardView alloErrorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.isAccessibilityServiceEnabled(this)) {
            accessibilityErrorLayout.setVisibility(View.GONE);
        }

        if (Util.isPackageInstalled(this, Constants.ALLO)) {
            alloErrorLayout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        if (Util.isAccessibilityServiceEnabled(this)) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION_LAUNCH_ASSISTANT));
        } else {
            Snackbar.make(coordinatorLayout, R.string.accessibility_snack, Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.accessibility_error_layout)
    public void onAccessibilityErrorClick() {
        startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
    }

    @OnClick(R.id.allo_error_layout)
    public void onAlloErrorClicked() {
        Util.openPlayStore(this, Constants.ALLO);
    }
}
