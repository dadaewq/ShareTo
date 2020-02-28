package com.mihotel.shareto.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.mihotel.shareto.R;
import com.mihotel.shareto.util.OpUtil;

/**
 * @author mihotel
 */
public class OpShare extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            opIntent();
        } catch (Exception e) {
            Toast.makeText(this, e + "", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void opIntent() {
//        OpUtil.praseIntent(getIntent());
        Intent intent = getIntent();
        boolean isUrl = false;
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            intent = OpUtil.send2view(intent);
        } else {
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                isUrl = true;
                intent = OpUtil.openUrl(intent);
            } else {
                intent = null;
            }
        }

        if (intent == null) {
            if (isUrl) {
                Toast.makeText(this, R.string.tip_invalidURL, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.tip_notsupport, Toast.LENGTH_SHORT).show();
            }
            finish();
        } else {
//            OpUtil.praseIntent(intent);
            String scheme = intent.getScheme();
            if ("http".equals(scheme) || "https".equals(scheme)) {
                Toast.makeText(this, String.format(getString(R.string.View), intent.getDataString()), Toast.LENGTH_SHORT).show();
            }
            startActivityForResult(Intent.createChooser(intent, null), (int) System.currentTimeMillis());
        }

    }
}
