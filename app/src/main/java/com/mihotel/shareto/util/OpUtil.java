package com.mihotel.shareto.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.mihotel.shareto.BuildConfig;

import java.io.File;

/**
 * @author mihotel
 */
public class OpUtil {

//    public static void showDetail(Uri uri) {
//        Log.e("--Uri--", uri + "");
//        Log.e("--getPath--", "[" + uri.getPath() + "]");
//        Log.e("--getLastPathSegment--", "[" + uri.getLastPathSegment() + "]");
//        Log.e("--getQuery--", "[" + uri.getQuery() + "]");
//        Log.e("--getScheme--", "[" + uri.getScheme() + "]");
//        Log.e("--getEncodedPath--", "[" + uri.getEncodedPath() + "]");
//        Log.e("--getAuthority--", "[" + uri.getAuthority() + "]");
//        Log.e("--getEncodedAuthority--", "[" + uri.getEncodedAuthority() + "]");
//        Log.e("--getEncodedFragment--", "[" + uri.getEncodedFragment() + "]");
//        Log.e("--getUserInfo--", uri.getUserInfo() + "");
//        Log.e("--getHost--", uri.getHost() + "");
//        Log.e("--getPathSegments--", uri.getPathSegments() + "");
//        Log.e("--getSchemeSpecificPart", uri.getSchemeSpecificPart() + "");
//        Log.e("--getPort--", uri.getPort() + "");
//        Log.e("-getQueryParameterNames", uri.getQueryParameterNames() + "");
//        Log.e("--isAbsolute--", uri.isAbsolute() + "");
//        Log.e("--isHierarchical--", uri.isHierarchical() + "");
//        Log.e("--isOpaque--", uri.isOpaque() + "");
//        Log.e("--isRelative--", uri.isRelative() + "");
//    }

    public static void praseIntent(Intent originalintent) {

        Log.e("Intent  ==>", originalintent + "");
        Log.e("Intent scheme  ==>", originalintent.getScheme() + "");
        Bundle originalExtras = originalintent.getExtras();
        if (originalExtras != null) {
            int i = 0;
            for (String key : originalExtras.keySet()) {
                Log.e("Bundle" + ++i, key + " | " + originalExtras.get(key));
            }
        }

    }

    public static Intent shareUrl(Intent originalintent) {
        return new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, originalintent.getDataString())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setType("text/plain");
    }

    public static Intent view2send(Intent originalintent) {
        String type = originalintent.getType();

        if (type == null) {
            type = "*/*";
        }
        return new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, originalintent.getData())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setType(type);

    }

    public static Intent openUrl(Intent originalintent) {

        String scheme = Uri.parse(originalintent.getExtras().get(Intent.EXTRA_TEXT) + "").getScheme();

        if ("http".equals(scheme) || "https".equals(scheme)) {
            return new Intent(Intent.ACTION_VIEW)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setData(Uri.parse(originalintent.getExtras().get(Intent.EXTRA_TEXT) + ""));
        } else {
            return null;
        }
    }

    public static Intent send2view(Intent originalintent) {
        String type = originalintent.getType();
        if (type == null) {
            type = "*/*";
        }

        return new Intent(Intent.ACTION_VIEW)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setDataAndType((Uri) originalintent.getExtras().get(Intent.EXTRA_STREAM), type);

    }

    public static File getWechatfile(Uri uri) {
        String pathSegments0 = uri.getPathSegments().get(0);

        String path = Environment.getExternalStoragePublicDirectory("") + "" + uri.getPath().substring(pathSegments0.length() + 1);
        File file = new File(path);
        if (file.exists()) {
            return file;
        } else {
            path = Environment.getExternalStoragePublicDirectory("") + "/Android/data/com.tencent.mm/sdcard" + uri.getPath().substring(pathSegments0.length() + 1);
            file = new File(path);
            if (file.exists()) {
                return file;
            } else {
                return null;
            }
        }

    }

    public static Uri getContentUri(Context context, File file) {

        return FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".fileProvider", file);
    }

    public static Intent getInstallUri(Uri uri) {
        return new Intent(Intent.ACTION_VIEW)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .setDataAndType(uri, "application/vnd.android.package-archive");
    }

}
