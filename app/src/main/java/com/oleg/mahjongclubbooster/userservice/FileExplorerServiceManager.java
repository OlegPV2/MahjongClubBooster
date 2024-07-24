package com.oleg.mahjongclubbooster.userservice;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.oleg.mahjongclubbooster.App;
import com.oleg.mahjongclubbooster.R;
import com.oleg.mahjongclubbooster.util.FileTools;
import com.oleg.mahjongclubbooster.util.ToastUtils;

import rikka.shizuku.Shizuku;
import rikka.shizuku.shared.BuildConfig;

public class FileExplorerServiceManager {
    private static final String TAG = "MahjongServiceManager";
    private static boolean isBind = false;

    private static final Shizuku.UserServiceArgs USER_SERVICE_ARGS = new Shizuku.UserServiceArgs(
            new ComponentName(App.get().getPackageName(), FileExplorerService.class.getName())
    ).daemon(false).debuggable(BuildConfig.DEBUG).processNameSuffix("mahjong_club_booster_service").version(1);

    private static final ServiceConnection SERVICE_CONNECTION = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            isBind = true;
            FileTools.iFileExplorerService = IFileExplorerService.Stub.asInterface(service);
            ToastUtils.shortCall(R.string.toast_shizuku_connected);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
            isBind = false;
            FileTools.iFileExplorerService = null;
            ToastUtils.shortCall(R.string.toast_shizuku_disconnected);
        }
    };

    public static void bindService() {
        Log.d(TAG, "bindService: isBind = " + isBind);
        if (!isBind) {
            Shizuku.bindUserService(USER_SERVICE_ARGS, SERVICE_CONNECTION);
        }
    }
}
