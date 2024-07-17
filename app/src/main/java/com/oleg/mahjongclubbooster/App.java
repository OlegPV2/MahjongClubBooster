package com.oleg.mahjongclubbooster;

import android.app.Application;

import java.io.File;
import java.io.FileOutputStream;

public class App extends Application {
    private static App sApp;

    public static App get() {
        return sApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
    }

}
