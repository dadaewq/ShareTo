package com.mihotel.shareto.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mihotel.shareto.R;
import com.mihotel.shareto.util.OpUtil;

import java.io.File;

public class OpApk1 extends Activity {
    private final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private boolean needpreceed = true;
    private boolean gtsdk28 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            gtsdk28 = true;
            if (checkPermission()) {
                proceed();
            } else {
                requestPermission();
            }
        } else {
            proceed();
        }
    }

    private void proceed() {
        if (needpreceed) {
            needpreceed = false;
            try {
                opIntent();
            } catch (Exception e) {
                Toast.makeText(this, e + "", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void opIntent() {
//        OpUtil.praseIntent(getIntent());

        Uri uri = getIntent().getData();

        Intent intent;
        Uri installuri;
        if ("com.tencent.mm.external.fileprovider".equals(uri.getAuthority())) {
            File file = OpUtil.getWechatfile(uri);
            if (file == null) {
                Toast.makeText(this, String.format(getString(R.string.failed_prase), uri), Toast.LENGTH_LONG).show();
                finish();
            } else {


                if (gtsdk28) {
                    installuri = OpUtil.getContentUri(this, file);
                } else {
                    installuri = Uri.fromFile(file);
                }

                intent = OpUtil.getInstallUri(installuri);

//                OpUtil.praseIntent(intent);

                startActivityForResult(Intent.createChooser(intent, null), (int) System.currentTimeMillis());
            }
        } else {
            Toast.makeText(this, R.string.tip_notsupport, Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0x233);
    }

    private boolean checkPermission() {
        int permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return (permissionRead == 0);
    }
}
