package com.oleg.mahjongclubbooster.update;

import com.king.app.dialog.AppDialog;
import com.king.app.dialog.AppDialogConfig;
import com.king.app.updater.AppUpdater;
import com.king.app.updater.callback.UpdateCallback;
import com.king.app.updater.http.OkHttpManager;
import com.oleg.mahjongclubbooster.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CheckUpdate {
    protected static final String jsonUrl = "https://raw.githubusercontent.com/OlegPV2/MahjongClubBooster/master/update.json";

    public static void checkUpdate(MainActivity mainActivity) {
        UpdateListener listener = new UpdateListener() {
            @Override
            public void onJsonDataReceived(UpdateModel updateModel, JSONObject jsonObject) {
                if (RetrieveJSON.getCurrentVersionCode(mainActivity) < updateModel.getVersionCode()) {
                    downloadAndInstall(mainActivity, updateModel);
                }
            }

            @Override
            public void onError(String error) {

            }
        };

        new RetrieveJSON(mainActivity, jsonUrl, listener) {

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
                    e.printStackTrace();
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

                        listener.onJsonDataReceived(updateModel, jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    listener.onError("JSON data null");
                }
            }
        }.execute();
    }

    protected static void downloadAndInstall(MainActivity mainActivity, UpdateModel updateModel) {
        AppDialogConfig config = new AppDialogConfig(mainActivity);
        config.setTitle("Upgrade available")
                .setConfirm("Upgrade")
                .setCancel("Cancel")
                .setHideCancel(!updateModel.cancellable)
                .setContent(updateModel.updateMessage)
                .setOnClickConfirm(v -> {
                    AppUpdater appUpdater = new AppUpdater(mainActivity, updateModel.url + updateModel.fileName);
                    appUpdater.setHttpManager(OkHttpManager.getInstance())
                            .setUpdateCallback(new UpdateCallback() {
                                @Override
                                public void onDownloading(boolean isDownloading) {
                                    // Downloading: When isDownloading is true, it means that the
                                    // download is already in progress, that is, the download has been
                                    // started before; when it is false, it means that the download
                                    // has not started yet, and the download will start soon
                                }

                                @Override
                                public void onStart(String url) {

                                }

                                @Override
                                public void onProgress(long progress, long total, boolean isChanged) {
                                    // Download progress update: It is recommended to update the
                                    // progress of the interface only when isChanged is true;
                                    // because the actual progress changes frequently
                                }

                                @Override
                                public void onFinish(File file) {

                                }

                                @Override
                                public void onError(Exception e) {

                                }

                                @Override
                                public void onCancel() {

                                }
                            }).start();

                    AppDialog.INSTANCE.dismissDialogFragment(mainActivity.getSupportFragmentManager());
                });
        AppDialog.INSTANCE.showDialogFragment(mainActivity.getSupportFragmentManager(), config);
    }
}
