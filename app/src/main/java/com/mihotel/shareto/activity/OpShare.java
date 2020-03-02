package com.mihotel.shareto.activity;

import android.app.Activity;
import android.content.ContentResolver;
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
public class OpShare extends Activity {
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

//        Log.e("requestCode", requestCode + "");
//        for (String key : permissions) {
//            Log.e("permissions", key);
//        }
//        for (int key : grantResults) {
//            Log.e("grantResults", key + "");
//        }

        if (PermissionUtil.checkReadPermission(this)) {
            try {
                startActivityForResult(Intent.createChooser(intent, null), (int) System.currentTimeMillis());
            } catch (Exception e) {
                Toast.makeText(this, e + "", Toast.LENGTH_LONG).show();
                finish();
            }
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
            //                OpUtil.praseIntent(intent);
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
        boolean isUrl = false;

        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            intent = OpUtil.intentsend2View(intent);
        } else {
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                isUrl = true;
                intent = OpUtil.intentOpenUrl(intent.getStringExtra(Intent.EXTRA_TEXT) + "");
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


            if ("http".equals(scheme) || "https".equals(scheme)) {
                Toast.makeText(this, String.format(getString(R.string.View), intent.getDataString()), Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
            } else {
                if (ContentResolver.SCHEME_CONTENT.equals(intent.getScheme())) {
                    if (getIntent().hasExtra("realPath")) {
                        intent = OpUtil.intentFile2MyContentIntent(this, Intent.ACTION_VIEW, intent.getType(), new File(getIntent().getStringExtra("realPath")));
                        startover();
                        return;
                    }
                }
                Uri uri = intent.getData();

                if (uri != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.P && ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
//                    Intent contentintent = (Intent) intent.clone();
//                    OpUtil.viewIntentFile2MyContent(this, contentintent);

                    Intent contentintent = OpUtil.intentFile2MyContentIntent(this, Intent.ACTION_VIEW, intent.getType(), new File(intent.getData().getPath()));

                    if (OpUtil.isneedinstallapkwithcontent(this, contentintent) || OpUtil.isneedfile2content(this, intent)) {
                        intent = contentintent;
                        startover();
                        return;
                    }
                }
//                OpUtil.praseIntent(intent);
                startActivityForResult(Intent.createChooser(intent, null), (int) System.currentTimeMillis());
            }
        }
    }
}
