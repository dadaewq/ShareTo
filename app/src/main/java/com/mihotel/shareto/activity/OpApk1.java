package com.mihotel.shareto.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.mihotel.shareto.R;
import com.mihotel.shareto.util.OpUtil;
import com.mihotel.shareto.util.PermissionUtil;

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


    private void opIntent() {
//        OpUtil.praseIntent(getIntent());

        Uri uri = getIntent().getData();

        if ("com.tencent.mm.external.fileprovider".equals(uri.getAuthority())) {
            File file = OpUtil.getWechatfile(uri);
            if (file == null) {
                Toast.makeText(this, String.format(getString(R.string.failed_prase), uri), Toast.LENGTH_LONG).show();
                finish();
            } else {
                intent = OpUtil.getInstalIntentBylUri(Uri.fromFile(file));

                Intent contentintent = OpUtil.getInstalIntentBylUri(OpUtil.getContentUri(this, file));

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P && OpUtil.isneedinstallapkwithcontent(this, contentintent)) {
                    intent = contentintent;
                    if (!PermissionUtil.checkReadPermission(this)) {
                        PermissionUtil.requestReadPermission(this);
                        return;
                    }
                }

//                OpUtil.praseIntent(intent);

                startActivityForResult(Intent.createChooser(intent, null), (int) System.currentTimeMillis());
            }
        } else {
            Toast.makeText(this, R.string.tip_notsupport, Toast.LENGTH_SHORT).show();
            finish();
        }

    }

}
