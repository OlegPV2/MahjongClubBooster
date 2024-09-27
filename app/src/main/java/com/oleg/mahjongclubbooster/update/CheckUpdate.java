package com.oleg.mahjongclubbooster.update;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.oleg.mahjongclubbooster.MainActivity;
import com.oleg.mahjongclubbooster.R;
import com.oleg.mahjongclubbooster.json.RetrieveJSON;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CheckUpdate {
    protected static final String jsonUrl = "https://raw.githubusercontent.com/OlegPV2/MahjongClubBooster/master/update.json";

    public static void checkUpdate(MainActivity mainActivity) {

        new RetrieveJSON(mainActivity, jsonUrl) {

            @Override
            public JSONObject doInBackground() {
                try {
                    URL url = new URL(jsonUrl);
                    InputStream is = url.openStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

                    StringBuilder sb = new StringBuilder();
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    is.close();

                    return new JSONObject(sb.toString());
                } catch (Exception e) {
                    Log.w("CheckUpdate->doInBackground", e.toString());
                }

                return null;
            }

            @Override
            public void onPostExecute(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        UpdateModel updateModel = new UpdateModel(
                                jsonObject.getString("url"),
                                jsonObject.getString("fileName"),
                                jsonObject.getInt("versionCode"),
                                jsonObject.getBoolean("cancellable"),
                                jsonObject.getString("updateMessage")
                        );

                        if (RetrieveJSON.getCurrentVersionCode(mainActivity) < updateModel.getVersionCode()) {
                            downloadAndInstall(mainActivity, updateModel);
                        }
                    } catch (JSONException e) {
                        Log.w("CheckUpdate->onPostExecute", e.toString());
                    }
                } else {
                    Log.w("CheckUpdate->onPostExecute", "JSON data null");
                }
            }
        }.execute();
    }

    protected static void downloadAndInstall(MainActivity mainActivity, UpdateModel updateModel) {
        final String CHANNEL_2_ID = "channel2";
        int processId = 2;
        Intent activityIntent = new Intent(mainActivity, MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(
                mainActivity,
                0,
                activityIntent,
                PendingIntent.FLAG_IMMUTABLE);

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(mainActivity, CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(mainActivity.getResources().getString(R.string.update_download))
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setContentText(mainActivity.getResources().getString(R.string.update_download_in_process))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_2_ID,
                    mainActivity.getResources().getString(R.string.update_download),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(mainActivity.getResources().getString(R.string.update_download_in_process));
            notificationManager.createNotificationChannel(notificationChannel);
        }

        AppUpdater appUpdater = new AppUpdater(mainActivity, updateModel.url, updateModel.fileName) {
            @Override
            public void beforeDownloading() {
                notification.setProgress(100, 0, true);
                notificationManager.notify(processId, notification.build());
            }

            @Override
            public void onDownloading(int maxProgress, int progress) {
                notification.setProgress(maxProgress, progress, false);
                notification.setShowWhen(true);
                notificationManager.notify(processId, notification.build());
            }

            @Override
            public void onDownloaded() {
                notificationManager.cancel(processId);
            }
        };
        new AlertDialog.Builder(mainActivity)
                .setTitle(R.string.update_available)
                .setCancelable(!updateModel.cancellable)
                .setMessage(updateModel.updateMessage)
                .setPositiveButton(R.string.update_positive_text, (dialog, which) -> appUpdater.execute())
                .setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {})
                .create()
                .show();
    }
}
