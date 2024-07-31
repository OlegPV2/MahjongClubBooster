package com.oleg.mahjongclubbooster.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.oleg.mahjongclubbooster.constant.PathType;
import com.oleg.mahjongclubbooster.readdata.Future;
import com.oleg.mahjongclubbooster.readdata.ShizukuFileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class GameJSON {

	private static String loadExternalJSON(Context context, String path, String file) {
		if (FileTools.specialPathReadType != PathType.SHIZUKU) {
			if (!FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
					return FileTools.readDocumentFile(context, path, file);
				} else {
					return FileTools.readFile(path, file);
				}
			}
		} else {
			return ShizukuFileUtil.read(path + file);
		}
		return "";
	}

	private static String loadJSONFromAsset(Context context, String file) {
		String jString;
		try {
			InputStream is = context.getAssets().open(file);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			jString = new String(buffer, StandardCharsets.UTF_8);
		} catch (IOException e) {
			Log.e("loadJSON", String.valueOf(e));
			return "";
		}
		return jString;
	}

	private static void saveToFile(Context context, String path, String file, byte[] data) {
		if (FileTools.specialPathReadType != PathType.SHIZUKU) {
			if (!FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
					FileTools.saveDocumentFile(context, path, file, data);
				} else {
					FileTools.saveFile(path, file, data);
				}
			}
		} else {
			FileTools.saveFile(FileTools.localPath, file, data);
			Future<Boolean> out = ShizukuFileUtil.remove(path + file);
			Log.e("saveToFile", out.toString());
			out = ShizukuFileUtil.move(FileTools.localPath + file, FileTools.mahjongClubFilesPath + file);
			Log.e("saveToFile", out.toString());
		}
	}

	public static String currentLevel(Context context) {
		try {
			String a = loadExternalJSON(context, FileTools.mahjongClubFilesPath, "playerProfile.json");
			JSONObject names = new JSONObject(a);
			return String.valueOf((Integer.parseInt(names.getString("levelsCompleted")) + 1));
		} catch (JSONException e) {
			Log.e("currentLevel", String.valueOf(e));
		}
		return "N/A";
	}

	public static void currentLevelStatusPatch(Context context, String level) {
		currentLevelStatus(context, "dummy.json", level);
	}

	public static void currentLevelStatusPuzzlesPatch(Context context, String level) {
		currentLevelStatus(context, "puzzles.json", level);
	}

	private static void currentLevelStatus(Context context, String file, String level) {
		try {
			JSONObject dummy = new JSONObject(loadJSONFromAsset(context, file));
			dummy.put("levelIndex", level);// currentLevel(context));
			saveToFile(context, FileTools.mahjongClubFilesPath, "CurrentLevelStatus", dummy.toString().getBytes());
		} catch (JSONException e) {
			Log.e("currentLevelStatus", String.valueOf(e));
		}
	}
}