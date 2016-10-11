package com.arun.allistant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ShortcutCreatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent assistOpenIntent = new Intent(this, AssistInterceptActivity.class);
        assistOpenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        assistOpenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        final Intent resultIntent = new Intent();
        resultIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, assistOpenIntent);
        resultIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Google Assistant");
        resultIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this,
                R.mipmap.ic_launcher));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
