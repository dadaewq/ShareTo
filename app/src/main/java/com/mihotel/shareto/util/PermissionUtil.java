package com.mihotel.shareto.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * @author mihotel
 */
@SuppressWarnings("SameParameterValue")
public class PermissionUtil {

    private static final String[] READ_PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    private static void requestPermission(Activity context, String[] permission, int requestCode) {
        ActivityCompat.requestPermissions(context, permission, requestCode);
    }

    public static void requestReadPermission(Activity context) {
        requestPermission(context, READ_PERMISSIONS, 233);
    }

    private static boolean checkPermission(Context context, String permission) {
        int permissionRead = ContextCompat.checkSelfPermission(context, permission);
        return (permissionRead == 0);
    }

    public static boolean checkReadPermission(Context context) {
        return checkPermission(context, READ_PERMISSIONS[0]);
    }
}
