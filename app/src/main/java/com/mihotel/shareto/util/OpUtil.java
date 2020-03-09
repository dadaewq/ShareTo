package com.mihotel.shareto.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

    /*
     * 列出originalintent的参数
     *
     */
    public static void praseIntent(Intent originalintent) {
        Log.e("Intent  ===>", originalintent + "");
        Log.e("action scheme type >", originalintent.getAction() + " | " + originalintent.getScheme() + " | " + originalintent.getType());
        Bundle originalExtras = originalintent.getExtras();
        if (originalExtras != null) {
            int i = 0;
            for (String key : originalExtras.keySet()) {
                Log.e("Bundle " + ++i, key + " | " + originalExtras.get(key));
            }
        }

    }


    /*
     * 检测text，如果是网址则生成OpenIntent
     *
     */
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

    /*
     * 检测text，如果是网址则生成OpenIntent
     *
     */
    public static boolean isneedfile2content(Context context, Intent intentwithfile) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intentwithfile, 0);

//        int i = 0;
//        for (ResolveInfo resolveInfo : list) {
//            Log.e("resolveInfo " + ++i, resolveInfo.activityInfo.packageName + " | " + resolveInfo.activityInfo.name);
//        }

        if (list.size() == 0) {
            return true;
        } else if (list.size() == 1) {
            ResolveInfo resolveInfo = list.get(0);
            return resolveInfo.activityInfo.packageName.equals(context.getPackageName());
        } else {
            return false;
        }

    }

    /*
     * 检测text，如果是网址则生成OpenIntent
     *
     */
    public static Intent intentOpenUrl(String text) {

        Uri uri = Uri.parse(text);
        String scheme = uri.getScheme();

        if ("http".equals(scheme) || "https".equals(scheme)) {
            return new Intent(Intent.ACTION_VIEW)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setData(uri);
        } else {
            return null;
        }
    }

    /*
     *以text生成SendIntent
     *
     */
    public static Intent intentshareUrl(String text) {
        return new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, text)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setType("text/plain");
    }

    /*
     *根据 originalintent 的type和data生成SendIntent
     *
     */
    public static Intent intentview2Send(Intent originalintent) {
        String type = originalintent.getType();

        if (type == null) {
            type = "*/*";
        }

        return new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, originalintent.getData())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setType(type);
    }

    /*
     *根据 originalintent 的type和Intent.EXTRA_STREAM生成ViewIntent
     *
     */
    public static Intent intentsend2View(Intent originalintent) {
        String type = originalintent.getType();
        if (type == null) {
            type = "*/*";
        }

        return new Intent(Intent.ACTION_VIEW)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setDataAndType((Uri) originalintent.getParcelableExtra(Intent.EXTRA_STREAM), type);
    }

    /*
     *根据 context、toAction、type和file生成MyContentIntent
     *
     */
    public static Intent intentFile2MyContentIntent(Context context, String toAction, String type, File file) {
        Intent intent = null;
        if (Intent.ACTION_VIEW.equals(toAction)) {
            Intent tempintent = new Intent()
                    .setType(type)
                    .putExtra(Intent.EXTRA_STREAM, getMyContentUriForFile(context, file));

            intent = intentsend2View(tempintent)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    .putExtra("realPath", file.getPath());
        } else if (Intent.ACTION_SEND.equals(toAction)) {
            Intent tempintent = new Intent()
                    .setDataAndType(OpUtil.getMyContentUriForFile(context, file), type);
            intent = intentview2Send(tempintent)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    .putExtra("realPath", file.getPath());
        }
        return intent;
    }

    /*
     *根据 context、fromAction,toAction和originalintent生成toActionMyContentIntent
     *
     */
    public static Intent fromActionSomeContent2ActionMyContentIntent(Context context, String fromAction, String toAction, Intent originalintent) {
        Intent intent = null;
        Uri uri = null;

        if (Intent.ACTION_VIEW.equals(fromAction)) {
            uri = originalintent.getData();
        } else if (Intent.ACTION_SEND.equals(fromAction)) {
            uri = originalintent.getParcelableExtra(Intent.EXTRA_STREAM);
        }

        if (uri != null && uri.getPath() != null) {

            File file = PraseContentUtil.getFile(context, uri);

            if (file != null && file.exists()) {
                intent = intentFile2MyContentIntent(context, toAction, originalintent.getType(), file);
            }
        }

        return intent;
    }

    private static Uri getMyContentUriForFile(Context context, File file) {

        return FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileProvider", file);
    }

    public static Intent getInstallIntentForFile(File file) {
        return new Intent(Intent.ACTION_VIEW)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
    }

    public static void showToast0(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast0(Context context, int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_LONG).show();
    }

    public static void showToast1(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast1(Context context, int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_LONG).show();
    }

}
