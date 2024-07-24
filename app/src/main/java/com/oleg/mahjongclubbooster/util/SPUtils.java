package com.oleg.mahjongclubbooster.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.oleg.mahjongclubbooster.App;

public class SPUtils {
    private static final SharedPreferences SP = App.get().getSharedPreferences("SPUtils", Context.MODE_PRIVATE);
    private static final String KEY_USE_NEW_DOCUMENT = "use_new_document";

    public static boolean getUseNewDocument() {
        return SP.getBoolean(KEY_USE_NEW_DOCUMENT, true);
    }
}
