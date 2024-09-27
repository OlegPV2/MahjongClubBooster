package com.oleg.mahjongclubbooster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.oleg.mahjongclubbooster.overlay.ButtonOverlayService;
import com.oleg.mahjongclubbooster.json.GameJSON;
import com.oleg.mahjongclubbooster.tools.RunningApps;
import com.oleg.mahjongclubbooster.tools.SharedPreferencesTools;
import com.oleg.mahjongclubbooster.tools.ToastTools;
import com.oleg.mahjongclubbooster.tools.ViewAnimationTools;
import com.oleg.mahjongclubbooster.json.RetrieveJSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class MainActivityViewInit extends AppCompatActivity {

	protected static final String jsonAccessUrl = "https://raw.githubusercontent.com/OlegPV2/MahjongClubBooster/master/access.json";

	private LinearLayout layoutEventsCollapsable;
	private LinearLayout layoutInventoryCollapsable;
	private Button buttonSend;
	private ImageView tileType;
	private PopupWindow changeStatusPopUp;
	private static int goldenTilesNumber = 8;
	private static int tileTypeNumber = 44;
	private static long autoClickButtonTimerValue;
	private static long autoClickPointTimerValue;

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);

		layoutEventsCollapsable = findViewById(R.id.layout_events_collapsable);
		layoutEventsCollapsable.setVisibility(View.GONE);
		LinearLayout layoutEvents = findViewById(R.id.layout_events);
		layoutEvents.setOnClickListener(view -> {
			if (layoutEventsCollapsable.getVisibility() == View.GONE) {
				ViewAnimationTools.expand(layoutEventsCollapsable);
			} else {
				ViewAnimationTools.collapse(layoutEventsCollapsable);
			}
		});
		TextView butterflyStart = findViewById(R.id.butterfly_start_date);
		TextView butterflyEnd = findViewById(R.id.butterfly_end_date);
		TextView chestStart = findViewById(R.id.chest_start_date);
		TextView chestEnd = findViewById(R.id.chest_end_date);
		TextView tournamentStart = findViewById(R.id.tournament_start_date);
		TextView tournamentEnd = findViewById(R.id.tournament_end_date);
		TextView puzzlesStart = findViewById(R.id.puzzle_start_date);
		TextView puzzlesEnd = findViewById(R.id.puzzle_end_date);
		TextView puzzleTiles = findViewById(R.id.puzzle_number_of_the_tiles);
		TextView zenStart = findViewById(R.id.zen_start_date);
		TextView zenEnd = findViewById(R.id.zen_end_date);
		TextView zenLevels = findViewById(R.id.zen_number_of_the_levels);
		String[] data = GameJSON.getDataInfo(this);
		butterflyStart.setText(data[0]);
		butterflyEnd.setText(data[1]);
		chestStart.setText(data[2]);
		chestEnd.setText(data[3]);
		tournamentStart.setText(data[4]);
		tournamentEnd.setText(data[5]);
		puzzlesStart.setText(data[6]);
		puzzlesEnd.setText(data[7]);
		puzzleTiles.setText(data[8]);
		zenStart.setText(data[9]);
		zenEnd.setText(data[10]);
		zenLevels.setText(data[11]);

		Spinner goldenTiles = findViewById(R.id.golden_tiles_number);
		ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.golden_tiles_numbers, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		goldenTiles.setAdapter(adapter);
		goldenTiles.setSelection(3);
		goldenTiles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				goldenTilesNumber = 2;
				for (int j = 0; j < i; j++) goldenTilesNumber += 2;
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		layoutInventoryCollapsable = findViewById(R.id.layout_inventory_collapsable);
		LinearLayout layoutInventory = findViewById(R.id.layout_inventory);
		layoutInventory.setOnClickListener(view -> {
			if (layoutInventoryCollapsable.getVisibility() == View.GONE) {
				ViewAnimationTools.expand(layoutInventoryCollapsable);
			} else {
				ViewAnimationTools.collapse(layoutInventoryCollapsable);
			}
		});
		TextView zenSilver = findViewById(R.id.zen_silver);
		TextView hint = findViewById(R.id.hint);
		TextView bomb = findViewById(R.id.bomb);
		TextView shuffle = findViewById(R.id.shuffle);
		TextView thunder = findViewById(R.id.thunder);
		String[] inventory = GameJSON.playerInventory(this);
		zenSilver.setText(inventory[0]);
		hint.setText(inventory[1]);
		bomb.setText(inventory[2]);
		shuffle.setText(inventory[3]);
		String tiles = "";
		if (inventory[4] != null) tiles = String.valueOf(Integer.parseInt(inventory[4]) * 2);
		thunder.setText(getString(R.string.inventory_thunder_and_tiles, inventory[4], tiles));
		EditText autoClickButtonTimer = findViewById(R.id.auto_click_button_timer);
		String value = SharedPreferencesTools.getProperty(SharedPreferencesTools.APP_PREFERENCES_BUTTON_INTERVAL);
		if (value != null) {
			autoClickButtonTimer.setText(value);
		}
		autoClickButtonTimerValue = Long.parseLong(autoClickButtonTimer.getText().toString());
		autoClickButtonTimer.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.length() > 0) {
					autoClickButtonTimerValue = Integer.parseInt(charSequence.toString());
					SharedPreferencesTools.addProperty(SharedPreferencesTools.APP_PREFERENCES_BUTTON_INTERVAL, charSequence.toString());
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		EditText autoClickPointTimer = findViewById(R.id.auto_click_point_timer);
		value = SharedPreferencesTools.getProperty(SharedPreferencesTools.APP_PREFERENCES_POINT_INTERVAL);
		if (value != null) {
			autoClickPointTimer.setText(value);
		}
		autoClickPointTimerValue = Long.parseLong(autoClickPointTimer.getText().toString());
		autoClickPointTimer.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.length() > 0) {
					autoClickPointTimerValue = Integer.parseInt(charSequence.toString());
					SharedPreferencesTools.addProperty(SharedPreferencesTools.APP_PREFERENCES_POINT_INTERVAL, charSequence.toString());
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		tileType = findViewById(R.id.tile_type);
		value = SharedPreferencesTools.getProperty(SharedPreferencesTools.APP_PREFERENCES_TILES);
		if (value != null) {
			Drawable src;
			switch (value) {
				case "44":
					src = AppCompatResources.getDrawable(this, R.drawable.tile_44);
					break;
				case "45":
					src = AppCompatResources.getDrawable(this, R.drawable.tile_45);
					break;
				case "46":
					src = AppCompatResources.getDrawable(this, R.drawable.tile_46);
					break;
				case "47":
					src = AppCompatResources.getDrawable(this, R.drawable.tile_47);
					break;
				default:
					src = AppCompatResources.getDrawable(this, R.drawable.tile_43);
			}
			tileTypeNumber = Integer.parseInt(value);
			tileType.setImageDrawable(src);
		}
		tileType.setOnClickListener(tileTypeListener);
		buttonSend = findViewById(R.id.button_send);
		if (sendButtonAccess()) {
			buttonSend.setVisibility(View.VISIBLE);
			buttonSend.setOnClickListener(buttonSendListener);
		}
		Button buttonOn = findViewById(R.id.button_on);
		buttonOn.setOnClickListener(view -> {
			if (!ButtonOverlayService.isServiceRun())
				startService(new Intent(this, ButtonOverlayService.class));
		});
		Button buttonOff = findViewById(R.id.button_off);
		buttonOff.setOnClickListener(view -> {
			if (ButtonOverlayService.isServiceRun())
				stopService(new Intent(this, ButtonOverlayService.class));
		});
	}

	private final View.OnClickListener tileTypeListener = this::showPopup;

	private void showPopup(View view) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		@SuppressLint("InflateParams") View layout = layoutInflater.inflate(R.layout.tile_selection_layout, null);

		changeStatusPopUp = new PopupWindow(this);
		changeStatusPopUp.setContentView(layout);
		changeStatusPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
		int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		layout.measure(spec, spec);
		changeStatusPopUp.setHeight(layout.getMeasuredHeight());
		changeStatusPopUp.setFocusable(true);

		changeStatusPopUp.showAsDropDown(view);
	}

	@SuppressLint("ResourceType")
	public void getTileTypeListener (View view) {
		tileTypeNumber = Integer.parseInt((String) view.getContentDescription());
		SharedPreferencesTools.addProperty(SharedPreferencesTools.APP_PREFERENCES_TILES, String.valueOf(tileTypeNumber));
		String res = (String) view.getTag();
		tileType.setImageResource(getResources().getIdentifier(res, "drawable", getPackageName()));
		changeStatusPopUp.dismiss();
	}

	private final View.OnClickListener buttonSendListener = view -> {
		if (RunningApps.isAppRunning(this, MainActivity.packageName)) {
			ToastTools.longCall(R.string.game_is_running_message);
		} else {
			if (GameJSON.copyRequestFile(App.get())) {
				Intent launchIntent = getPackageManager().getLaunchIntentForPackage(MainActivity.packageName);
				if (launchIntent != null) {
					startActivity(launchIntent);
				}
			}
		}
	};

	private boolean sendButtonAccess() {
		int id = GameJSON.getPlayerId(this);

		new RetrieveJSON(this, jsonAccessUrl) {
			@Override
			public JSONObject doInBackground() {
				try {
					URL url = new URL(jsonAccessUrl);
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
					Log.w("doInBackground", e.toString());
				}

				return null;
			}

			@Override
			public void onPostExecute(JSONObject jsonObject) {
				if (jsonObject != null) {
					try {
						JSONArray array = jsonObject.getJSONArray("allowable");
						for(int i = 0; i < array.length(); i++) {
							JSONObject data = array.optJSONObject(i);
							int allowed_id = data.getInt("user_id");
							if (id == allowed_id) {
								buttonSend.setVisibility(View.VISIBLE);
								break;
							}
						}
					} catch (JSONException e) {
						Log.w("onPostExecute", e.toString());
					}
				} else {
					Log.w("onPostExecute", "JSON data null");
				}
			}
		}.execute();
		return false;
	}

	public static int getGoldenTilesNumber() {
		return goldenTilesNumber;
	}

	public static int getTileTypeNumber() {
		return tileTypeNumber;
	}

	public static long getAutoClickButtonTimerValue() {
		return autoClickButtonTimerValue;
	}

	public static long getAutoClickPointTimerValue() {
		return autoClickPointTimerValue;
	}


}
