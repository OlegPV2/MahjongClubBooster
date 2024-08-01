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
			JSONObject b = new JSONObject(a);
			return String.valueOf((Integer.parseInt(b.getString("levelsCompleted")) + 1));
		} catch (JSONException e) {
			Log.e("currentLevel", String.valueOf(e));
		}
		return "Попробуй еще раз";
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

	public static String[] getDates(Context context) {
		String[] data = new String[10];
		try {
			String a = loadExternalJSON(context, FileTools.mahjongClubFilesPath + "Data/", "butterfly_event_data.gvmc");
			JSONObject b = new JSONObject(a);
			data[0] = b.getString("startDate");
			data[1] = b.getString("endDate");
			a = loadExternalJSON(context, FileTools.mahjongClubFilesPath + "Data/", "chest_event_data.gvmc");
			b = new JSONObject(a);
			data[2] = b.getString("startDate");
			data[3] = b.getString("endDate");
			a = loadExternalJSON(context, FileTools.mahjongClubFilesPath + "Data/", "club_tournament_data.gvmc");
			b = new JSONObject(a);
			data[4] = b.getString("startDate");
			data[5] = b.getString("endDate");
			a = loadExternalJSON(context, FileTools.mahjongClubFilesPath + "Data/", "puzzle_event_data.gvmc");
			b = new JSONObject(a);
			data[6] = b.getString("startDate");
			data[7] = b.getString("endDate");
			a = loadExternalJSON(context, FileTools.mahjongClubFilesPath + "Data/", "zen_event_data.gvmc");
			b = new JSONObject(a);
			data[8] = b.getString("currentTournamentStartDate");
			data[9] = b.getString("currentTournamentEndDate");
		} catch (JSONException e) {
			Log.e("playerInventory", String.valueOf(e));
		}
		return data;
	}

	public static String[] playerInventory(Context context) {
		String[] data = new String[5];
		try {
			String a = loadExternalJSON(context, FileTools.mahjongClubFilesPath + "Data/", "player_inventory.gvmc");
			JSONObject b = new JSONObject(a);
			data[0] = b.getString("zenSilver");
			JSONObject c = b.getJSONObject("boosters");
			data[1] = c.getString("booster_hint");
			data[2] = c.getString("booster_bomb");
			data[3] = c.getString("booster_shuffle");
			data[4] = c.getString("booster_thunder");
		} catch (JSONException e) {
			Log.e("playerInventory", String.valueOf(e));
		}
		return data;
	}
}