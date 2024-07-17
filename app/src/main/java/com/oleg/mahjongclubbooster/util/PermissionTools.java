package com.oleg.mahjongclubbooster.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import com.oleg.mahjongclubbooster.App;
import com.oleg.mahjongclubbooster.constant.RequestCode;

public class PermissionTools {

    public static boolean hasStoragePermission() {
        Context context = App.get();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:"+activity.getPackageName()));
            activity.startActivityForResult(intent, RequestCode.STORAGE);
        } else {
            activity.requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE } , RequestCode.STORAGE);
        }
    }

}
