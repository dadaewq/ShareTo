package com.mihotel.shareto.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.mihotel.shareto.R;
import com.mihotel.shareto.util.OpUtil;
import com.mihotel.shareto.util.PermissionUtil;

import java.io.File;
import java.lang.reflect.Field;

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
            Toast.makeText(this, e + "", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (PermissionUtil.checkReadPermission(this)) {
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, e + "", Toast.LENGTH_LONG).show();
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


    private String reflectGetReferrer() {
        try {
            Class activityClass = Class.forName("android.app.Activity");

            //noinspection JavaReflectionMemberAccess
            Field refererField = activityClass.getDeclaredField("mReferrer");
            refererField.setAccessible(true);
            return (String) refererField.get(this);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startover() {
        if (PermissionUtil.checkReadPermission(this)) {
            startActivity(intent);
            finish();
        } else {
            PermissionUtil.requestReadPermission(this);
        }
    }

    private void opIntent() {
        intent = getIntent();
//        OpUtil.praseIntent(intent);

        String scheme = intent.getScheme();

        if ("http".equals(scheme) || "https".equals(scheme)) {
            intent = OpUtil.IntentshareUrl(getIntent().getDataString());
            Toast.makeText(this, String.format(getString(R.string.Share), intent.getStringExtra(Intent.EXTRA_TEXT)), Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        } else {
            if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                if (intent.hasExtra("realPath")) {
                    intent = OpUtil.IntentFile2MyContentIntent(this, Intent.ACTION_SEND, intent.getType(), new File(intent.getStringExtra("realPath")));
                    startover();
                    return;
                }

                String referrer = reflectGetReferrer();
                if ("com.tencent.mm".equals(referrer)) {
                    Intent intent1 = OpUtil.viewWechatContent2sendMyContent(this, intent);
                    if (intent1 != null) {
                        intent = intent1;
//                        OpUtil.praseIntent(intent);
                        startover();
                        return;
                    }
                }
            }
            intent = OpUtil.intentview2Send(getIntent());
//        OpUtil.praseIntent(intent);
            startActivityForResult(Intent.createChooser(intent, null), (int) System.currentTimeMillis());
        }
    }
}
