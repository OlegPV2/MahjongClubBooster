package com.oleg.mahjongclubbooster.update;

import org.json.JSONObject;

public interface UpdateListener {
    void onJsonDataReceived(UpdateModel updateModel, JSONObject jsonObject);
    void onError(String error);
}