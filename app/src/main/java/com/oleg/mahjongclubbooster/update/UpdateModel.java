package com.oleg.mahjongclubbooster.update;

public class UpdateModel {
    public final String url;
    public final String fileName;
    public final boolean cancellable;
    public final String updateMessage;
    private final int versionCode;

    public UpdateModel(String url, String fileName, int versionCode, boolean cancellable, String updateMessage) {
        this.url = url;
        this.fileName = fileName;
        this.versionCode = versionCode;
        this.cancellable = cancellable;
        this.updateMessage = updateMessage;
    }

    public int getVersionCode() {
        return versionCode;
    }
}