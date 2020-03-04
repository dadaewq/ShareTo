package com.mihotel.shareto.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

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
                Intent contentIntent;
                if (ContentResolver.SCHEME_CONTENT.equals(intent.getScheme())) {

                    String realPath = null;
                    contentIntent = getIntent();
                    if (contentIntent.hasExtra("realPath")) {
                        realPath = "realPath";
                    } else if (contentIntent.hasExtra("url")) {
                        realPath = "url";
                    }
                    if (realPath != null) {
                        Log.e("extraRealPath", contentIntent.getStringExtra(realPath) + "");
                        contentIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(contentIntent.getStringExtra(realPath) + "")));
                    }

                    contentIntent = OpUtil.fromActionSomeContent2ActionMyContentIntent(this, Intent.ACTION_SEND, Intent.ACTION_VIEW, contentIntent);

                    if (contentIntent != null) {
                        intent = contentIntent;
                        startover();
                        return;
                    }
                } else {
                    Uri uri = intent.getData();

                    if (uri != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.P && ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {

                        Intent contentintent = OpUtil.intentFile2MyContentIntent(this, Intent.ACTION_VIEW, intent.getType(), new File(intent.getData().getPath() + ""));

                        if (OpUtil.isneedinstallapkwithcontent(this, contentintent) || OpUtil.isneedfile2content(this, intent)) {
                            intent = contentintent;
                            startover();
                            return;
                        }
                    }
                }
//                OpUtil.praseIntent(intent);
                startActivityForResult(Intent.createChooser(intent, null), (int) System.currentTimeMillis());
            }
        }
    }
}
