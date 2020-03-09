package com.mihotel.shareto.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.mihotel.shareto.R;
import com.mihotel.shareto.util.PermissionUtil;
import com.mihotel.shareto.util.PraseContentUtil;

import java.io.File;

import static com.mihotel.shareto.util.OpUtil.getInstallIntentForFile;
import static com.mihotel.shareto.util.OpUtil.intentFile2MyContentIntent;
import static com.mihotel.shareto.util.OpUtil.isneedinstallapkwithcontent;
import static com.mihotel.shareto.util.OpUtil.showToast0;
import static com.mihotel.shareto.util.OpUtil.showToast1;

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
            showToast1(this, e + "");
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
            showToast1(this, e + "");
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
                intent = getInstallIntentForFile(file);

                Intent contentintent = intentFile2MyContentIntent(this, Intent.ACTION_VIEW, intent.getType(), file);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P && isneedinstallapkwithcontent(this, contentintent)) {
                    intent = contentintent;
                }
                startover();
            } else {
                showToast1(this, String.format(getString(R.string.tip_failed_prase), uri));
                finish();
            }
        } else {
            showToast0(this, R.string.tip_notsupport);
            finish();
        }

    }

}
