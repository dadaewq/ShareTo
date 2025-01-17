package com.mihotel.shareto.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @author mihotel (begin with 2020.03.04)
 */
public class PraseContentUtil {
    private static Uri uri;
    private static String shardUid;
    private static String uriPath;
    private static String authority;
    private static String SDCARD;
    private static String getExternalStorageDirectory;
    private static String getExternalRootDir;
    private static String getExternalFilesDir;
    private static String getExternalCacheDir;
    private static String getStorageIsolationDir;
    private static String getfirstPathSegment;
    private static String getPathFromIndex1PathSegment;
    private static String getLastPathSegment;
    private final static String AUTHOR_OF_PRASE_CONTENT_UTIL = "mihotel (begin with 2020.03.04)";

    /**
     * 反射获取准确的Intent Referrer
     */
    private static String reflectGetReferrer(Context context) {
        try {

            Class activityClass = Class.forName("android.app.Activity");

            //noinspection JavaReflectionMemberAccess
            Field refererField = activityClass.getDeclaredField("mReferrer");
            refererField.setAccessible(true);
            return (String) refererField.get(context);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取pkgName的SharedUserId,兼容"存储空间隔离"新特性
     */
    private static String getSharedUserId(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        String sharedUserId = null;
        try {
            applicationInfo = pm.getApplicationInfo(pkgName, 0);
        } catch (Exception ignore) {
        }

        if (applicationInfo != null) {
            PackageInfo pkgInfo = pm.getPackageArchiveInfo(applicationInfo.sourceDir, PackageManager.GET_ACTIVITIES);
            if (pkgInfo != null) {
                sharedUserId = pkgInfo.sharedUserId;
            }
        }
        return sharedUserId;
    }

    //显示Uri的一些信息
    private static void showDetail(Uri uri) {
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

    //主要根据getAuthority和 getDataColumn获取file
    private static File getFileFromUri(final Context context, final Uri uri) {
        String path = null;
        String getScheme = uri.getScheme();
        String getAuthority = uri.getAuthority() + "";
        Log.e("query_authority", "" + uri.getAuthority());

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {

            String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Log.e("docId", docId);

            switch (getAuthority) {
                // DownloadsProvider
                case "com.android.providers.downloads.documents":

                    if (split.length > 1) {
                        if ("raw".equalsIgnoreCase(type)) {
                            path = split[1];
                        }
                    } else {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            //不支持Android10 及以上
                            // 需要android.permission.ACCESS_ALL_DOWNLOADS
                            String[] uriString = new String[]{"content://downloads/public_downloads", "content://downloads/my_downloads", "content://downloads/all_downloads"};

                            for (String s : uriString) {
                                path = getDataColumn(context, ContentUris.withAppendedId(Uri.parse(s), Long.parseLong(docId)), null, null);
                                if (path != null) {
                                    break;
                                }
                            }
                        }
                    }
                    break;
                // ExternalStorageProvider
                case "com.android.externalstorage.documents":
                    if (split.length > 1) {
                        if ("primary".equalsIgnoreCase(type)) {
                            path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        } else if ("home".equalsIgnoreCase(type)) {
                            path = Environment.getExternalStorageDirectory() + "/Documents/" + split[1];
                        }
                    }
                    break;
                // MediaProvider
                case "com.android.providers.media.documents":
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                default:
            }

        } else if (ContentResolver.SCHEME_FILE.equals(getScheme)) {
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(getScheme)) {
            // Return the remote address
            if ("com.google.android.apps.photos.content".equals(getAuthority)) {
                path = uri.getLastPathSegment();
            } else {
                path = getDataColumn(context, uri, null, null);
            }
        }

        if (checkFileorPath(path)) {
            return new File(path);
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Log.e("query_uri", uri + "");
        final String column = MediaStore.MediaColumns.DATA;
        final String[] projection = {
                column
        };
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                DatabaseUtils.dumpCursor(cursor);
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //一些变量的初始化
    @SuppressLint("SdCardPath")
    private static void initValue(Context context, Uri fromUri) {
        //        showDetail(uri);
        uri = fromUri;
        String referrer = reflectGetReferrer(context);
        shardUid = getSharedUserId(context, referrer);

        try {
            uriPath = uri.getPath();
            authority = uri.getAuthority();
            //"/storage/emulated/0"
            getExternalStorageDirectory = Environment.getExternalStorageDirectory().getPath();
            SDCARD = "/sdcard";
            getExternalRootDir = "/Android/data/" + referrer;
            getExternalFilesDir = "/Android/data/" + referrer + "/files";
            getExternalCacheDir = "/Android/data/" + referrer + "/cache";
            getStorageIsolationDir = "/Android/data/" + referrer + "/sdcard";

            //获取索引0路径段
            getfirstPathSegment = uri.getPathSegments().get(0);
            //最后一个路径段
            getLastPathSegment = uri.getLastPathSegment();

            //删索引0路径段和其前面的"/"
            getPathFromIndex1PathSegment = uriPath.substring(getfirstPathSegment.length() + 1);

        } catch (Exception e) {
            authority = null;
        }

    }

    //方法 1
    private static File getSomeFile() {

        if (authority == null) {
            return null;
        } else {
            Log.e("getSomeFile_FROM_URI", uri + "");
            File file = null;
            try {
                file = getSomeFileFromAuthorityAndUri();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (file == null) {
                file = getSomeFileFromReferrerAndUri();
            }
            return file;
        }
    }

    //方法 1.1 根据authority的自定义规则获取file
    @SuppressLint("SdCardPath")
    private static File getSomeFileFromAuthorityAndUri() {
        String path = "";
        String path0;
        ArrayList<String> pathList = new ArrayList<>();

        switch (authority) {
            case "moe.shizuku.redirectstorage.ServerFileProvider":
                if (uri.getPathSegments().size() > 2) {
                    String getIndex1PathSegment = uri.getPathSegments().get(1);
                    String getPathfromIndex2PathSegment = uriPath.substring(getfirstPathSegment.length() + 1 + getIndex1PathSegment.length() + 1);
                    if (SDCARD.equalsIgnoreCase("/" + getIndex1PathSegment)) {
                        path = getPathFromIndex1PathSegment;
                        pathList.add(path);
                    }
                    path = getExternalStorageDirectory + "/Android/data/" + getIndex1PathSegment + "/sdcard" + getPathfromIndex2PathSegment;
                    pathList.add(path);
                }
                break;
            case "com.taptap.fileprovider":
                if ("downloads_external".equals(getfirstPathSegment)) {
                    path = getExternalStorageDirectory + getExternalFilesDir + "/Download" + getPathFromIndex1PathSegment;
                }
            case "com.coolapk.market.fileprovider":

                switch (getfirstPathSegment) {
                    case "files_root":
                        //content://com.coolapk.market.fileprovider/files_root/file.apk
                        //content://com.coolapk.market.fileprovider/files_root/files/Download/file.apk

                        //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer"
                        path = getExternalStorageDirectory + getExternalRootDir + getPathFromIndex1PathSegment;
                        break;
                    case "external_files_path":
                        //content://com.coolapk.market.fileprovider/external_files_path/file.apk
                        //content://com.coolapk.market.fileprovider/external_files_path/files/Download/file.apk

                        //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/files"+"/Download"
                        path = getExternalStorageDirectory + getExternalFilesDir + "/Download" + getPathFromIndex1PathSegment;
                        break;
                    case "gdt_sdk_download_path":
                        //content://com.coolapk.market.fileprovider/gdt_sdk_download_path/file.apk

                        //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/GDTDOWNLOAD"
                        path = getExternalStorageDirectory + "/GDTDOWNLOAD" + getPathFromIndex1PathSegment;
                        break;
                    case "external_storage_root":
                        //content://com.coolapk.market.fileprovider/external_storage_root/6/file.apk

                        //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"
                        path = getExternalStorageDirectory + getPathFromIndex1PathSegment;
                        break;
                    default:
                }
                path0 = path;


                //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/sdcard"
                path = getExternalStorageDirectory + getStorageIsolationDir + getPathFromIndex1PathSegment;
                pathList.add(path);


                //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/sdcard"+"/GDTDOWNLOAD"
                path = getExternalStorageDirectory + getStorageIsolationDir + "/GDTDOWNLOAD" + getPathFromIndex1PathSegment;
                pathList.add(path);

                pathList.add(path0);
                break;
            case "com.coolapk.market.vn.fileProvider":
                switch (getfirstPathSegment) {
                    case "files_root":
                        //content://com.coolapk.market.vn.fileProvider/files_root/file.apk
                        //content://com.coolapk.market.vn.fileProvider/files_root/files/Download/file.apk

                        //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer"
                        path = getExternalStorageDirectory + getExternalRootDir + getPathFromIndex1PathSegment;
                        break;
                    case "external_storage_root":
                        //content://com.coolapk.market.vn.fileProvider/external_storage_root/6/file.apk

                        //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"
                        path = getExternalStorageDirectory + getPathFromIndex1PathSegment;
                        break;
                    default:
                }
                path0 = path;
                pathList.add(path);

                //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/sdcard"
                path = getExternalStorageDirectory + getStorageIsolationDir + getPathFromIndex1PathSegment;
                pathList.add(path);
                pathList.add(path0);
                break;
//                //content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Tencent/QQfile_recv/file.apk
//                //content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/QQfile_recv/file.apk
//
//                //删索引0路径段和其前面的"/"
//                path = getPathExcludeFirstPathSegment;
//                pathList.add(path);
//
//                //删索引0路径段和前面的"/"，在索引4路径段前的"/"前面插入"/Android/data/com.referrer/sdcard"
//                if (uri.getPathSegments().size() > 3) {
//                    int indexTencent = path.indexOf(uri.getPathSegments().get(4)) - 1;
//                    StringBuilder stringBuilder = new StringBuilder(path)
//                            .insert(indexTencent, getStorageIsolationDir);
//                    path = stringBuilder.toString();
//                    pathList.add(path);
//                }
//
//                //content://com.tencent.mm.external.fileprovider/external/tencent/MicroMsg/Download/file.apk
//
//                //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"
//                path = getExternalStorageDirectory + getPathExcludeFirstPathSegment;
//                pathList.add(path);
//
//                //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/sdcard"
//                path = getExternalStorageDirectory + getStorageIsolationDir + getPathExcludeFirstPathSegment;
//                pathList.add(path);

//                pathList = getPathListAboutExternalStoragePublicDirectory();

            case "com.tencent.mm.external.fileprovider":
            case "com.tencent.mobileqq.fileprovider":
            case "com.mi.android.globalFileexplorer.myprovider":
            case "in.mfile.files":
            case "com.estrongs.files":
            case "com.ktls.fileinfo.provider":
            case "pl.solidexplorer2.files":
            case "cn.ljt.p7zip.fileprovider":
            default:
                if ("downloads".equals(getfirstPathSegment)) {
                    if (uri.getPathSegments().size() > 1) {
                        path = getExternalStorageDirectory + "/Download" + getPathFromIndex1PathSegment;
                        path0 = path;
                        path = getExternalStorageDirectory + getStorageIsolationDir + "/Download" + getPathFromIndex1PathSegment;
                        pathList.add(path);
                        pathList.add(path0);
                        return pickValidFilefromPathList(pathList);
                    }
                }
                return null;
        }
        return pickValidFilefromPathList(pathList);
    }

    //在pathList挑选有效的file
    private static File pickValidFilefromPathList(ArrayList<String> pathList) {
        if (pathList == null) {
            return null;
        } else {
            for (String getPath : pathList) {
                if (checkFileorPath(getPath)) {
                    return new File(getPath);
                } else {
                    Log.e("fakePath", getPath + "");
                }
            }
            return null;
        }
    }

    //方法 1.2 的一个具体操作获取PathList
    private static ArrayList<String> getPathListAboutExternalStoragePublicDirectory() {
        if (uriPath == null) {
            return null;
        } else {
            ArrayList<String> pathList = new ArrayList<>();
            int size = uri.getPathSegments().size();
            if (uriPath.startsWith(getExternalStorageDirectory)) {
                if (size > 3) {
                    //content://in.mfile.files/storage/emulated/0/file.apk
                    pathList = getPathListStartWith(getExternalStorageDirectory, uriPath);
                }
            } else if (getPathFromIndex1PathSegment.startsWith(getExternalStorageDirectory)) {

                if (size > 4) {
                    //content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Tencent/QQfile_recv/file.apk
                    pathList = getPathListStartWith(getExternalStorageDirectory, getPathFromIndex1PathSegment);
                }
            } else if (SDCARD.equalsIgnoreCase("/" + getfirstPathSegment)) {
                if (size > 1) {
                    //content://in.mfile.files/storage/emulated/0/file.apk
                    pathList = getPathListStartWith(SDCARD, uriPath);
                }
            } else if (getPathFromIndex1PathSegment.startsWith(SDCARD)) {
                if (size > 2) {
                    //content://in.mfile.files/storage/emulated/0/file.apk
                    pathList = getPathListStartWith(SDCARD, getPathFromIndex1PathSegment);
                }
            } else {
                String path0 = getExternalStorageDirectory + uriPath;
                String path1 = getExternalStorageDirectory + getPathFromIndex1PathSegment;
                if (size > 0) {
                    //content://com.referrer.fileprovider/file.apk
                    pathList = getPathListStartWith(getExternalStorageDirectory, path0);
                }
                if (size > 1) {
                    //content://com.tencent.mm.external.fileprovider/external/tencent/MicroMsg/Download/file.apk
                    pathList.addAll(getPathListStartWith(getExternalStorageDirectory, path1));
                }
            }
            return pathList;
        }
    }

    @SuppressLint("SdCardPath")
    private static ArrayList<String> getPathListStartWith(String startWith, @NonNull String path) {
        ArrayList<String> pathList = new ArrayList<>();
        String path0;
        if (path.startsWith(startWith)) {
            //content://in.mfile.files/storage/emulated/0/file.apk
            path0 = path;


            pathList.add(getPathWithIsolation(startWith, getStorageIsolationDir, path));

            if (shardUid != null) {
                Log.e("shardUid", "" + shardUid);
                pathList.add(getPathWithIsolation(startWith, "/Android/data/shared-" + shardUid + "/sdcard", path));
            }
            pathList.add(getPathWithIsolation(startWith, getExternalFilesDir, path));
            pathList.add(path0);

            return pathList;
        } else {
            return pathList;
        }
    }

    //兼容"存储空间隔离"
    private static String getPathWithIsolation(String startWith, String prefix, @NonNull String path) {
        StringBuilder stringBuilder = new StringBuilder(path)
                .insert(startWith.length(), prefix);
        return stringBuilder.toString();

    }

    //方法 1.2 根据对ExternalStoragePublicDirectory的相关操作获取file
    private static File getSomeFileFromReferrerAndUri() {
        ArrayList<String> pathList = getPathListAboutExternalStoragePublicDirectory();

        return pickValidFilefromPathList(pathList);
    }

    private static boolean checkFileorPath(File file) {
        return file != null && file.exists() && !file.isDirectory();

    }

    private static boolean checkFileorPath(String path) {
        if (path == null) {
            return false;
        } else {
            return checkFileorPath(new File(path));
        }
    }

    //两种方法获取file
    public static File getFile(Context context, Uri fromUri) {
        initValue(context, fromUri);
        File file = null;

        try {
            file = getSomeFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("getSomeFile", file + "");

        if (file == null) {
            try {
                file = getFileFromUri(context, fromUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("getFileFromUri", file + "");
        }

        return file;

    }
}