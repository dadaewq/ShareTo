package com.mihotel.shareto.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.List;

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

    public static boolean isneedinstallapkwithcontent(Context context, Intent intentInstallapkwithcontent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intentInstallapkwithcontent, 0);
//        int i = 0;
//        for (ResolveInfo resolveInfo : list) {
//            Log.e("resolveInfo " + ++i, resolveInfo.activityInfo.packageName + " | " + resolveInfo.activityInfo.name);
//        }
        boolean need = false;
        for (ResolveInfo resolveInfo : list) {
//            Log.e("resolveInfo " + ++i, resolveInfo.activityInfo.packageName);
            if ("com.android.packageinstaller".equals(resolveInfo.activityInfo.packageName) || "com.google.android.packageinstaller".equals(resolveInfo.activityInfo.packageName)) {
                need = true;
                break;
            }
        }
        return need;

    }

    public static boolean isneedfile2content(Context context, Intent intentwithfile) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intentwithfile, 0);

        int i = 0;
        for (ResolveInfo resolveInfo : list) {
            Log.e("resolveInfo " + ++i, resolveInfo.activityInfo.packageName + " | " + resolveInfo.activityInfo.name);
        }

        if (list.size() == 0) {
            return true;
        } else if (list.size() == 1) {
            ResolveInfo resolveInfo = list.get(0);
            return resolveInfo.activityInfo.packageName.equals(context.getPackageName());
        } else {
            return false;
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

        Uri uri = Uri.parse(originalintent.getStringExtra(Intent.EXTRA_TEXT) + "");
        String scheme = uri.getScheme();

        if ("http".equals(scheme) || "https".equals(scheme)) {
            return new Intent(Intent.ACTION_VIEW)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setData(uri);
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
                .setDataAndType((Uri) originalintent.getParcelableExtra(Intent.EXTRA_STREAM), type);

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

    public static void intentFile2Content(Context context, Intent fileintent) {

        fileintent.setDataAndType(getContentUri(context, new File(fileintent.getData().getPath())), fileintent.getType());
        fileintent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    public static Uri getContentUri(Context context, File file) {

        return FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileProvider", file);
    }

    public static Intent getInstalIntentBylUri(Uri uri) {
        return new Intent(Intent.ACTION_VIEW)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .setDataAndType(uri, "application/vnd.android.package-archive");
    }

}
