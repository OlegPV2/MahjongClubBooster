package com.oleg.mahjongclubbooster.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.oleg.mahjongclubbooster.App;
import com.oleg.mahjongclubbooster.constant.PathType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GameJSON {

	private static String loadExternalJSON(Context context, String path, String file) {
		String jString = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && FileTools.specialPathReadType != PathType.SHIZUKU) {
			Uri pathUri = FileTools.pathToUri(path);
			DocumentFile documentPath = DocumentFile.fromTreeUri(App.get(), pathUri);
			if (documentPath != null) {
				DocumentFile df = documentPath.findFile(file);
				try {
					assert df != null;
					InputStream is = context.getContentResolver().openInputStream(df.getUri());
					if (is != null) {
						int size = is.available();
						byte[] buffer = new byte[size];
						is.read(buffer);
						is.close();
						jString = new String(buffer, StandardCharsets.UTF_8);
					}
				} catch (IOException e) {
					Log.e("loadJSON", String.valueOf(e));
					return "";
				}
			}
		} else {
			try {
				File yourFile = new File(path, file);
				try (FileInputStream stream = new FileInputStream(yourFile)) {
					FileChannel fc = stream.getChannel();
					MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

					jString = Charset.defaultCharset().decode(bb).toString();
				} catch (Exception e) {
					Log.e("loadExternalJSON", e.toString());
				}
			} catch (Exception e) {
				Log.e("loadExternalJSON", e.toString());
			}		}
		return jString;
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

	private static Boolean saveToFile(Context context, String path, String file, byte[] data) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			Uri pathUri = FileTools.pathToUri(path);
			DocumentFile documentPath = DocumentFile.fromTreeUri(App.get(), pathUri);
			if (documentPath != null) {
				DocumentFile df = documentPath.findFile(file);
				if (df != null) {
					df.delete();
				}
				df = documentPath.createFile("application/*", file);
				try {
					assert df != null;
					OutputStream os = context.getContentResolver().openOutputStream(df.getUri());
					if (os != null) {
						os.write(data);
						os.close();
					}
					return true;
				} catch (IOException e) {
					Log.e("saveToFile", String.valueOf(e));
					return false;
				}
			}
		} else {
			try {
				File fileToSave = new File(path, file);
				if (fileToSave.exists()) {
					fileToSave.delete();
				}
				if (fileToSave.createNewFile()) {
					FileOutputStream fileOutputStream = new FileOutputStream(fileToSave);
					fileOutputStream.write(data);
					fileOutputStream.close();
					return true;
				}
			} catch (Exception e) {
				Log.e("saveToFile", e.toString());
			}
		}
		return false;
	}

	public static String currentLevel(Context context) {
		if (!FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
			try {
				JSONObject names = new JSONObject(loadExternalJSON(context, FileTools.mahjongClubFilesPath, "playerProfile.json"));
				return String.valueOf((Integer.parseInt(names.getString("levelsCompleted")) + 1));
			} catch (JSONException e) {
				Log.e("JSON_CurrentLevel", String.valueOf(e));
			}
		}
		return "N/A";
	}

	public static void currentLevelStatusPatch(Context context) {
		if (!FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
			try {
				JSONObject dummy = new JSONObject(loadJSONFromAsset(context, "dummy.json"));
				dummy.put("levelIndex", currentLevel(context));
				saveToFile(context, FileTools.mahjongClubFilesPath, "CurrentLevelStatus", dummy.toString().getBytes());
			} catch (JSONException e) {
				Log.e("JSON_CurrentLevelStatus", String.valueOf(e));
			}
		}
	}

/*
	private static int gameId(JSONArray idList, LocalDate date) {
		try {
			for (int i = 0; i < idList.length(); i++) {
				JSONObject jsonMonth = idList.getJSONObject(i);
				String m = jsonMonth.getString("Month");
				if (!m.isEmpty() && Integer.parseInt(m) == date.getMonthValue()) {
					JSONArray jsonDays = jsonMonth.getJSONArray("Days");
					for (int j = 0; j < jsonDays.length(); j++) {
						JSONObject jsonDay = jsonDays.getJSONObject(j);
						String d = jsonDay.getString("Day");
						if (!d.isEmpty() && Integer.parseInt(d) == date.getDayOfMonth()) {
							JSONArray jsonID = jsonDay.getJSONArray("ID");
							if (date.getYear() % 2 == 0)
								return jsonID.getInt(1);
							else
								return jsonID.getInt(0);
						}
					}
				}
			}
		} catch (JSONException e) {
			Log.e("ID", String.valueOf(e));
		}
		return 0;
	}

	public static void dailyLevelsMaker(Context context, String startDate, String endDate){
		try {
			JSONObject dummy = new JSONObject(loadJSONFromAsset(context, "dummy.json"));
			JSONArray idList = new JSONArray(loadJSONFromAsset(context, "IDs.json"));
			JSONObject jo = idList.getJSONObject(0);
			idList = jo.getJSONArray("ID");

			DateTimeFormatter parser = DateTimeFormatter.ofPattern("d-M-yyyy");

			LocalDate start_date = LocalDate.parse(startDate, parser);
			LocalDate end_date = LocalDate.parse(endDate, parser);
			LocalDate oldDate = start_date;

			JSONObject day_json = new JSONObject();
			JSONObject month_json = new JSONObject();
			JSONObject year_json = new JSONObject();
			JSONArray days_json = new JSONArray();
			JSONArray months_json = new JSONArray();
			JSONArray years_json = new JSONArray();

			for (LocalDate date = start_date; date.isBefore(end_date); date = date.plusDays(1)) {
				dummy.put("levelIndex", gameId(idList, date));
				String outputFileName = "DailyChallenge_" + date.getYear() + "_" + date.getMonthValue() +
						"_" + date.getDayOfMonth() + "_" + dummy.getString("levelIndex") + "_Status.json";
				byte[] buffer = dummy.toString().getBytes();
				saveToFile(context, FileTools.mahjongClubFilesPath + "DailyChallengeLevelsStatus/", outputFileName, buffer);
				if (processTextView != null)
					processTextView.updateButtonText("File " + outputFileName + " saved");
				Log.i("dailyLevelsMaker", "File " + outputFileName + " saved");

				if (oldDate.getYear() == date.getYear()) {
					if (oldDate.getMonthValue() == date.getMonthValue()) {
						day_json.put("day", date.getDayOfYear());
						day_json.put("total_stones", 144);
						day_json.put("remaining_stones", 2);
						day_json.put("completed", true);
						days_json.put(day_json);
//						day_json = new JSONObject();
					} else {
						month_json.put("month", oldDate.getMonthValue());
						month_json.put("days", days_json);
						month_json.put("monthCompleted", true);
						months_json.put(month_json);
//						month_json = new JSONObject();
						days_json = new JSONArray();
						day_json.put("day", date.getDayOfYear());
						day_json.put("total_stones", 144);
						day_json.put("remaining_stones", 2);
						day_json.put("completed", true);
						days_json.put(day_json);
//						day_json = new JSONObject();
						oldDate = date;
					}
				} else {
					month_json.put("month", oldDate.getMonthValue());
					month_json.put("days", days_json);
					month_json.put("monthCompleted", true);
					months_json.put(month_json);
					year_json.put("year", oldDate.getYear());
					year_json.put("months", months_json);
					years_json.put(year_json);
					days_json = new JSONArray();
					day_json.put("day", date.getDayOfYear());
					day_json.put("total_stones", 144);
					day_json.put("remaining_stones", 2);
					day_json.put("completed", true);
					days_json.put(day_json);
					months_json = new JSONArray();
					month_json.put("month", oldDate.getMonthValue());
					month_json.put("days", days_json);
					month_json.put("monthCompleted", true);
					months_json.put(month_json);
				}
			}
			month_json.put("month", oldDate.getMonthValue());
			month_json.put("days", days_json);
			month_json.put("monthCompleted", true);
			months_json.put(month_json);
			year_json.put("year", oldDate.getYear());
			year_json.put("months", months_json);
			years_json.put(year_json);
			JSONObject final_json = new JSONObject();
			final_json.put("years", years_json);

			String outputFileName = "daily_challenge_data.gvmc";
			String buffer = Arrays.toString(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
			saveToFile(context, FileTools.mahjongClubFilesPath + "Data/", outputFileName, (buffer + final_json).getBytes());
			if (processTextView != null)
				processTextView.updateButtonText("File " + outputFileName + " saved");
		} catch (JSONException e) {
			Log.e("JSON_CurrentLevel", String.valueOf(e));
		}
	}

	public static void butterflyEventFilePatched(Context context) {
		try {
			JSONObject names = new JSONObject(loadExternalJSON(context, FileTools.mahjongClubFilesPath + "Data/", "butterfly_event_data.gvmc"));
			names.put("butterflyCollectedAmount", 540);
			JSONArray rewards = names.getJSONArray("rewards");
			JSONObject a = rewards.getJSONObject(0);
			a.put("claimed", false);
			rewards.put(0, a);
			a = rewards.getJSONObject(1);
			a.put("claimed", false);
			rewards.put(0, a);
			a = rewards.getJSONObject(2);
			a.put("claimed", false);
			rewards.put(0, a);
			a = rewards.getJSONObject(3);
			a.put("claimed", false);
			rewards.put(0, a);
			names.put("rewards", rewards);
			saveToFile(context, FileTools.mahjongClubFilesPath + "Data/", "butterfly_event_data.gvmc", names.toString().getBytes());
			if (processTextView != null)
				processTextView.updateButtonText("File butterfly_event_data.gvmc saved");
		} catch (JSONException e) {
			Log.e("JSON_ButterflyEvent", String.valueOf(e));
		}
	}*/
}