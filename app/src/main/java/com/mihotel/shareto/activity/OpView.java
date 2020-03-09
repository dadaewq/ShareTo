package com.mihotel.shareto.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mihotel.shareto.R;
import com.mihotel.shareto.util.OpUtil;
import com.mihotel.shareto.util.PermissionUtil;

import java.io.File;

import static com.mihotel.shareto.util.OpUtil.showToast1;

/**
 * @author mihotel
 */
public class OpView extends Activity {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            opIntent();
        } catch (Exception e) {
            showToast1(this, e + "");
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (PermissionUtil.checkReadPermission(this)) {
            try {
                startActivity(intent);
            } catch (Exception e) {
                showToast1(this, e + "");
            }
            finish();
        } else {
            PermissionUtil.requestReadPermission(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }


    private void startover() {
        if (PermissionUtil.checkReadPermission(this)) {
//            Log.e("startover", "startover:\n------------------- ");
//            OpUtil.praseIntent(intent);
            startActivity(intent);
            finish();
        } else {
            PermissionUtil.requestReadPermission(this);
        }
    }

    private void opIntent() {
        intent = getIntent();
//        Log.e("00000000", "00000000:\n------------------- ");
//        OpUtil.praseIntent(intent);

        String scheme = intent.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            intent = OpUtil.intentshareUrl(getIntent().getDataString());
            showToast1(this, String.format(getString(R.string.Share), intent.getStringExtra(Intent.EXTRA_TEXT)));
            startActivity(intent);
            finish();
        } else {
            if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                String realPath = null;

                if (intent.hasExtra("realPath")) {
                    realPath = "realPath";
                } else if (intent.hasExtra("url")) {
                    realPath = "url";
                }
                if (realPath != null) {
                    Log.e("extraRealPath", intent.getStringExtra(realPath) + "");
                    intent.setDataAndType(Uri.fromFile(new File(intent.getStringExtra(realPath) + "")), intent.getType());
                }
//                Intent contentIntent = OpUtil.viewSomeContent2ActionMyContent(this, Intent.ACTION_SEND, intent);
                Intent contentIntent = OpUtil.fromActionSomeContent2ActionMyContentIntent(this, Intent.ACTION_VIEW, Intent.ACTION_SEND, intent);
                if (contentIntent != null) {
                    intent = contentIntent;
                    startover();
                    return;
                }
            }
            intent = OpUtil.intentview2Send(getIntent());
//            OpUtil.praseIntent(intent);
            startActivityForResult(Intent.createChooser(intent, null), (int) System.currentTimeMillis());
        }
    }
}
