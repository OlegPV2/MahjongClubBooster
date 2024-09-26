package com.oleg.mahjongclubbooster.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.oleg.mahjongclubbooster.App;

import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesTools {

	public static final String APP_PREFERENCES = "booster_settings";
	public static final String APP_PREFERENCES_BUTTON_INTERVAL = "button_interval";
	public static final String APP_PREFERENCES_POINT_INTERVAL = "point_interval";
	public static final String APP_PREFERENCES_BUTTON_COORDINATES = "button_coordinates";
	public static final String APP_PREFERENCES_POINT_COORDINATES = "point_coordinates";
	public static final String APP_PREFERENCES_TILES = "tiles_on_level";

	private static SharedPreferences mSettings;
	private static SharedPreferences.Editor editor;

	public static void init() {
		mSettings = App.get().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		editor = mSettings.edit();
	}

	public static void addProperty(String name, String value) {
		if (mSettings == null)
			init();
		editor.putString(name, value);
		editor.apply();
	}

	public static void addProperties(String name, int v1, int v2) {
		if (mSettings == null)
			init();
		Set<String> values = new HashSet<>();
		values.add(String.valueOf(v1));
		values.add(String.valueOf(v2));
		editor.putStringSet(name, values);
		editor.apply();
	}

	public static String getProperty(String name) {
		if (mSettings == null)
			init();
		return  mSettings.getString(name, null);
	}

	public static Set<String> getProperties(String name) {
		if (mSettings == null)
			init();
		Set<String> def = new HashSet<>();
		return  mSettings.getStringSet(name, def);
	}
}
