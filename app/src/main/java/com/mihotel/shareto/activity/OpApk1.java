package com.mihotel.shareto.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mihotel.shareto.R;
import com.mihotel.shareto.util.OpUtil;
import com.mihotel.shareto.util.PermissionUtil;
import com.mihotel.shareto.util.PraseContentUtil;

import java.io.File;

/**
 * @author mihotel
 */
public class OpApk1 extends Activity {
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

    private void startover() {
        if (PermissionUtil.checkReadPermission(this)) {
            //OpUtil.praseIntent(intent);
            startActivity(intent);
            finish();
        } else {
            PermissionUtil.requestReadPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            startover();
        } catch (Exception e) {
            Toast.makeText(this, e + "", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    private void opIntent() {
        intent = getIntent();
//        OpUtil.praseIntent(intent);
        Uri uri = getIntent().getData();

        if (uri != null) {
            File file = null;
            if (ContentResolver.SCHEME_FILE.equals(intent.getScheme())) {
                String path = uri.getPath();
                if (path != null) {
                    file = new File(path);
                }
            } else if (intent.hasExtra("realPath")) {
                file = new File(intent.getStringExtra("realPath") + "");
            } else {
                file = PraseContentUtil.getFile(this, uri);
            }

            if (file != null && file.exists()) {
                intent = OpUtil.getInstallIntentForFile(file);

                Intent contentintent = OpUtil.intentFile2MyContentIntent(this, Intent.ACTION_VIEW, intent.getType(), file);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P && OpUtil.isneedinstallapkwithcontent(this, contentintent)) {
                    intent = contentintent;
                    startover();
                    return;
                }
//                OpUtil.praseIntent(intent);
                startActivity(intent);
            } else {
                Toast.makeText(this, String.format(getString(R.string.failed_prase), uri), Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.tip_notsupport, Toast.LENGTH_SHORT).show();
            finish();
        }

    }

}
