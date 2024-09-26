package com.oleg.mahjongclubbooster.json;

import static com.oleg.mahjongclubbooster.MainActivityViewInit.getGoldenTilesNumber;
import static com.oleg.mahjongclubbooster.MainActivityViewInit.getTileTypeNumber;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.oleg.mahjongclubbooster.R;
import com.oleg.mahjongclubbooster.constant.PathType;
import com.oleg.mahjongclubbooster.shizukuutil.ShizukuFileUtil;
import com.oleg.mahjongclubbooster.tools.FileTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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
			if (is.read(buffer) > 0) {
				jString = new String(buffer, StandardCharsets.UTF_8);
			} else {
				jString = "";
			}
			is.close();
		} catch (IOException e) {
			Log.w("loadJSONFromAsset", String.valueOf(e));
			return "";
		}
		return jString;
	}

	public static String getCurrentLevel(Context context) {
		try {
			String a = loadExternalJSON(context, FileTools.mahjongClubFilesPath, "playerProfile.json");
			JSONObject b = new JSONObject(a);
			return String.valueOf((Integer.parseInt(b.getString("levelsCompleted")) + 1));
		} catch (JSONException e) {
			Log.w("currentLevel", String.valueOf(e));
		}
		return context.getResources().getString(R.string.button_try_again_text);
	}

	public static String getCurrentLevelFromLevelsData(Context context) {
		List<String> list = FileTools.getFilesList();
		List<Integer> integerList = list.stream().map(Integer::valueOf).collect(Collectors.toList());
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < integerList.size(); i++){
			if (integerList.get(i) > max) {
				max = integerList.get(i);
			}
		}
		try {
			String a = loadExternalJSON(context, FileTools.mahjongClubFilesPath + "LevelsData/", max + ".json");
			JSONObject b = new JSONObject(a);
			JSONArray c = b.getJSONArray("subchapters");
			JSONObject d = c.getJSONObject(c.length() - 1);
			JSONArray e = d.getJSONArray("levels");
			JSONObject f = e.getJSONObject(e.length() - 1);
			if (f.getInt("numberOfStars") == -1) f = e.getJSONObject(e.length() - 2);
			return String.valueOf((f.getInt("levelIndex")) + 1);
		} catch (JSONException e) {
			Log.w("currentLevel", String.valueOf(e));
		}
		return context.getResources().getString(R.string.button_try_again_text);
	}

	public static void currentLevelStatusPatch(Context context, String level) {
		currentLevelStatus(context, "dummy.json", level);
	}

	public static void currentLevelStatusPuzzlesPatch(Context context, String level) {
		currentLevelStatus(context, "puzzles.json", level);
	}

	public static void currentLevelStatusButterflyPatch(Context context, String level) {
		currentLevelStatus(context, "butterfly.json", level);
	}

	private static void currentLevelStatus(Context context, String file, String level) {
		try {
			JSONObject dummy = new JSONObject(loadJSONFromAsset(context, file));
			dummy.put("levelIndex", level);
			dummy.put("goldenTileCounter",
					getTileTypeNumber() == 43 ?
							getGoldenTilesNumber() - 2 :
							getGoldenTilesNumber());
			JSONArray layers = dummy.getJSONArray("layers");
			JSONObject layer0 = layers.getJSONObject(0);
			JSONArray stonesType0 = layer0.getJSONArray("stonesType");
			stonesType0.put(0, getTileTypeNumber());
			JSONObject layer1 = layers.getJSONObject(1);
			JSONArray stonesType1 = layer1.getJSONArray("stonesType");
			stonesType1.put(0, getTileTypeNumber());

			FileTools.saveToFile(context, FileTools.mahjongClubFilesPath, "CurrentLevelStatus", dummy.toString().getBytes());
		} catch (JSONException e) {
			Log.w("currentLevelStatus", String.valueOf(e));
		}
	}

	public static String[] getDataInfo(Context context) {
		String[] data = new String[12];
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
			JSONArray c = b.getJSONArray("tilesRequired");
			int sum = 0;
			if (c.length() > 0) {
				for (int i = 0; i <= c.length(); i++) {
					sum += c.getInt(i);
				}
			}
			data[8] = String.valueOf(sum);
			a = loadExternalJSON(context, FileTools.mahjongClubFilesPath + "Data/", "zen_event_data.gvmc");
			b = new JSONObject(a);
			data[9] = b.getString("currentTournamentStartDate");
			data[10] = b.getString("currentTournamentEndDate");
			c = b.getJSONArray("tiersData");
			if (c.length() > 0) {
				JSONObject d = c.getJSONObject(c.length() - 1);
				data[11] = d.getString("maxProgressToUnlockNextTear");
			} else {
				data[11] = "0";
			}
		} catch (JSONException e) {
			Log.w("playerInventory", String.valueOf(e));
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
			Log.w("playerInventory", String.valueOf(e));
		}
		return data;
	}

	public static int getPlayerId(Context context) {
		int id = 0;
		try {
			String a = loadExternalJSON(context, FileTools.mahjongClubFilesPath + "Data/", "player_credentials.gvmc");
			JSONObject b = new JSONObject(a);
			id = b.getInt("user_id");
		} catch (JSONException e) {
			Log.w("playerInventory", String.valueOf(e));
		}
		return id;
	}

	public static boolean copyRequestFile(Context context) {
		byte[] file = FileTools.readByteFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Telegram/requests_queue.gvmc"));
		if (file != null) {
			FileTools.saveToFile(context, FileTools.mahjongClubFilesPath + "Requests/", "requests_queue.gvmc", file);
			return true;
		}
		return false;
	}
}