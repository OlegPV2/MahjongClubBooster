package com.oleg.mahjongclubbooster.json;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import org.json.JSONObject;

public abstract class RetrieveJSON {
    private final Activity activity;
    private final String jsonUrl;

    public RetrieveJSON(Activity activity, String jsonUrl) {
        this.activity = activity;
        this.jsonUrl = jsonUrl;
    }

    private void startBackground() {
        boolean cancel = false;
        if (jsonUrl == null) {
            Log.d("startBackground", "onPreExecute: jsonUrl == null");
            cancel = true;
        } else if (!isNetworkAvailable(activity)) {
            Log.d("startBackground", "Please check your network connection");
            cancel = true;
        } else if (jsonUrl.isEmpty()) {
            Log.d("startBackground", "Please provide a valid JSON URL");
            cancel = true;
        }

        if(!cancel) new Thread(() -> {

            JSONObject jsonObject = doInBackground();
            activity.runOnUiThread(() -> onPostExecute(jsonObject));

        }).start();
    }
    public void execute(){
        startBackground();
    }

    private boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null;
    }

    public static int getCurrentVersionCode(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("getCurrentVersionCode", e.toString());
        }

        if (pInfo != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) pInfo.getLongVersionCode();
            } else {
                return pInfo.versionCode;
            }

        return 0;
    }

    public abstract JSONObject doInBackground();
    public abstract void onPostExecute(JSONObject jsonObject);

}
