package com.mihotel.shareto.util;

import android.annotation.SuppressLint;
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

    public static void showDetail(Uri uri) {
        Log.e("--Uri--", uri + "");
        Log.e("--getPath--", "[" + uri.getPath() + "]");
        Log.e("--getLastPathSegment--", "[" + uri.getLastPathSegment() + "]");
        Log.e("--getQuery--", "[" + uri.getQuery() + "]");
        Log.e("--getScheme--", "[" + uri.getScheme() + "]");
        Log.e("--getEncodedPath--", "[" + uri.getEncodedPath() + "]");
        Log.e("--getAuthority--", "[" + uri.getAuthority() + "]");
        Log.e("--getEncodedAuthority--", "[" + uri.getEncodedAuthority() + "]");
        Log.e("--getEncodedFragment--", "[" + uri.getEncodedFragment() + "]");
        Log.e("--getUserInfo--", uri.getUserInfo() + "");
        Log.e("--getHost--", uri.getHost() + "");
        Log.e("--getPathSegments--", uri.getPathSegments() + "");
        Log.e("--getSchemeSpecificPart", uri.getSchemeSpecificPart() + "");
        Log.e("--getPort--", uri.getPort() + "");
        Log.e("-getQueryParameterNames", uri.getQueryParameterNames() + "");
        Log.e("--isAbsolute--", uri.isAbsolute() + "");
        Log.e("--isHierarchical--", uri.isHierarchical() + "");
        Log.e("--isOpaque--", uri.isOpaque() + "");
        Log.e("--isRelative--", uri.isRelative() + "");
    }

    public static void praseIntent(Intent originalintent) {

        Log.e("Intent  ===>", originalintent + "");
        Log.e("Intent scheme  >", originalintent.getScheme() + "");
        Bundle originalExtras = originalintent.getExtras();
        if (originalExtras != null) {
            int i = 0;
            for (String key : originalExtras.keySet()) {
                Log.e("Bundle " + ++i, key + " | " + originalExtras.get(key));
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
     *根据 originalintent 的type和Intent.EXTRA_STREAM生成成ViewIntent
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
     *根据 content、targetIntentAction、type和file生成MyContentIntent
     *
     */
    public static Intent intentFile2MyContentIntent(Context context, String targetIntentAction, String type, File file) {
        Intent intent = null;
        if (Intent.ACTION_VIEW.equals(targetIntentAction)) {
            Intent tempintent = new Intent()
                    .setType(type)
                    .putExtra(Intent.EXTRA_STREAM, getMyContentUriForFile(context, file));

            intent = intentsend2View(tempintent)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    .putExtra("realPath", file.getAbsolutePath());
        } else if (Intent.ACTION_SEND.equals(targetIntentAction)) {
            Intent tempintent = new Intent()
                    .setDataAndType(OpUtil.getMyContentUriForFile(context, file), type);
            intent = intentview2Send(tempintent)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    .putExtra("realPath", file.getAbsolutePath());
        }
        return intent;
    }


    public static Intent viewSomeContent2sendMyContent(Context context, String referrer, Intent originalintent) {
        Intent intent = null;
        Uri uri = originalintent.getData();
        if (uri != null) {
            File file = OpUtil.getSomeFileFromReferrerAndUri(referrer, uri);
            if (file != null) {
                intent = intentFile2MyContentIntent(context, Intent.ACTION_SEND, originalintent.getType(), file);
            }

        }

        return intent;
    }

    public static File getSomeFileFromReferrerAndUri(String referrer, Uri uri) {
        String authority = uri.getAuthority();
        File file = null;
        if (authority != null) {
            @SuppressLint("SdCardPath") String storageisolation = "/Android/data/" + referrer + "/sdcard";

//            Log.e("isAbsolute", uri.isAbsolute() + "");
            switch (referrer) {
                case "com.tencent.mm":
                    if ("com.tencent.mm.external.fileprovider".equals(authority)) {
                        String pathSegments0 = uri.getPathSegments().get(0);

                        String path = Environment.getExternalStoragePublicDirectory("") + uri.getPath().substring(pathSegments0.length() + 1);
                        file = new File(path);
                        if (file.exists()) {
                            return file;
                        } else {
                            path = Environment.getExternalStoragePublicDirectory("") + storageisolation + uri.getPath().substring(pathSegments0.length() + 1);
                            file = new File(path);
                        }
                    }
                    break;
                case "com.tencent.mobileqq":
                    if ("com.tencent.mobileqq.fileprovider".equals(authority)) {
                        String pathSegments0 = uri.getPathSegments().get(0);

                        String path = uri.getPath().substring(pathSegments0.length() + 1);
                        file = new File(path);
                        if (file.exists()) {
                            return file;
                        } else {
                            int indexTencent = path.indexOf(uri.getPathSegments().get(4)) - 1;
                            StringBuilder stringBuilder = new StringBuilder(path)
                                    .insert(indexTencent, storageisolation);
                            path = stringBuilder.toString();
                            file = new File(path);
                        }
                    }

                    break;
                case "com.coolapk.market":
                    if ("com.coolapk.market.fileprovider".equals(authority)) {
                        String pathSegments0 = uri.getPathSegments().get(0);

                        String path = Environment.getExternalStoragePublicDirectory("") + uri.getPath().substring(pathSegments0.length() + 1);
                        file = new File(path);
                        if (file.exists()) {
                            return file;
                        } else {
                            path = Environment.getExternalStoragePublicDirectory("") + storageisolation + uri.getPath().substring(pathSegments0.length() + 1);
                            file = new File(path);
                        }
                    }
                    break;
                default:
                    return null;
            }
        }
        if (file != null && file.exists()) {
            return file;
        } else {
            return null;
        }

    }

//    public static void viewIntentFile2MyContent(Context context, Intent fileintent) {
//
//        fileintent.setDataAndType(getMyContentUriForFile(context, new File(fileintent.getData().getPath())), fileintent.getType());
//        fileintent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        fileintent.putExtra("realPath", fileintent.getData().getPath());
//    }

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

}
