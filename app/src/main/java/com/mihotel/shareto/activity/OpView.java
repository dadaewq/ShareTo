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
public class OpView extends Activity {
    private boolean needfinish = false;

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
        String scheme = getIntent().getScheme();
        Intent intent;
        if ("http".equals(scheme) || "https".equals(scheme)) {
            intent = OpUtil.shareUrl(getIntent());
            Toast.makeText(this, String.format(getString(R.string.Share), intent.getStringExtra(Intent.EXTRA_TEXT)), Toast.LENGTH_SHORT).show();
        } else {
            intent = OpUtil.view2send(getIntent());
        }

//        OpUtil.praseIntent(intent);
        if ("http".equals(scheme) || "https".equals(scheme)) {
            needfinish = true;
            Toast.makeText(this, String.format(getString(R.string.Share), intent.getStringExtra(Intent.EXTRA_TEXT)), Toast.LENGTH_SHORT).show();
        }
        if (needfinish) {
            startActivity(intent);
            finish();
        } else {
            startActivityForResult(Intent.createChooser(intent, null), (int) System.currentTimeMillis());
        }

    }
}
