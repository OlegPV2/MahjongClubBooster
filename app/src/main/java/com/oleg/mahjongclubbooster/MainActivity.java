package com.oleg.mahjongclubbooster;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.oleg.mahjongclubbooster.autoclick.TapAccessibilityService;
import com.oleg.mahjongclubbooster.constant.PathType;
import com.oleg.mahjongclubbooster.constant.RequestCode;
import com.oleg.mahjongclubbooster.overlay.ButtonOverlayService;
import com.oleg.mahjongclubbooster.util.FileTools;
import com.oleg.mahjongclubbooster.util.GameJSON;
import com.oleg.mahjongclubbooster.util.PermissionTools;
import com.oleg.mahjongclubbooster.util.RunningApps;
import com.oleg.mahjongclubbooster.util.ToastUtils;
import com.oleg.mahjongclubbooster.util.ViewAnimationUtil;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener {

	LinearLayout layoutEventsCollapsable, layoutEvents;
	LinearLayout layoutInventoryCollapsable, layoutInventory;
	TextView butterflyStart, butterflyEnd;
	TextView chestStart, chestEnd;
	TextView tournamentStart, tournamentEnd;
	TextView puzzlesStart, puzzlesEnd, puzzleTiles;
	TextView zenStart, zenEnd, zenLevels;
	TextView zenSilver, hint, bomb, shuffle, thunder;
	Spinner goldenTiles;
	EditText autoClickButtonTimer, autoClickPointTimer;
	Button buttonOn, buttonOff, buttonSend;

	private static final String packageName = "com.gamovation.mahjongclub";
	private static final boolean availableSendButton = true;

	public static int goldenTilesNumber = 8;
	public static long autoClickButtonTimerValue;
	public static long autoClickPointTimerValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		CheckUpdate.checkUpdate(this);
		FileTools.defineRootPath(this, packageName);
		if (PermissionTools.isShizukuAvailable()) {
			Shizuku.addRequestPermissionResultListener(this);
		}
		checkAllPermissions();
		initView();
	}

	private void initView() {
		layoutEventsCollapsable = findViewById(R.id.layout_events_collapsable);
		layoutEventsCollapsable.setVisibility(View.GONE);
		layoutEvents = findViewById(R.id.layout_events);
		layoutEvents.setOnClickListener(view -> {
			if (layoutEventsCollapsable.getVisibility() == View.GONE) {
				ViewAnimationUtil.expand(layoutEventsCollapsable);
			} else {
				ViewAnimationUtil.collapse(layoutEventsCollapsable);
			}
		});
		butterflyStart = findViewById(R.id.butterfly_start_date);
		butterflyEnd = findViewById(R.id.butterfly_end_date);
		chestStart = findViewById(R.id.chest_start_date);
		chestEnd = findViewById(R.id.chest_end_date);
		tournamentStart = findViewById(R.id.tournament_start_date);
		tournamentEnd = findViewById(R.id.tournament_end_date);
		puzzlesStart = findViewById(R.id.puzzle_start_date);
		puzzlesEnd = findViewById(R.id.puzzle_end_date);
		puzzleTiles = findViewById(R.id.puzzle_number_of_the_tiles);
		zenStart = findViewById(R.id.zen_start_date);
		zenEnd = findViewById(R.id.zen_end_date);
		zenLevels = findViewById(R.id.zen_number_of_the_levels);
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

		goldenTiles = findViewById(R.id.golden_tiles_number);
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
			public void onNothingSelected(AdapterView<?> adapterView) {}
		});

		layoutInventoryCollapsable = findViewById(R.id.layout_inventory_collapsable);
		layoutInventory = findViewById(R.id.layout_inventory);
		layoutInventory.setOnClickListener(view -> {
			if (layoutInventoryCollapsable.getVisibility() == View.GONE) {
				ViewAnimationUtil.expand(layoutInventoryCollapsable);
			} else {
				ViewAnimationUtil.collapse(layoutInventoryCollapsable);
			}
		});
		zenSilver = findViewById(R.id.zen_silver);
		hint = findViewById(R.id.hint);
		bomb = findViewById(R.id.bomb);
		shuffle = findViewById(R.id.shuffle);
		thunder = findViewById(R.id.thunder);
		String[] inventory = GameJSON.playerInventory(this);
		zenSilver.setText(inventory[0]);
		hint.setText(inventory[1]);
		bomb.setText(inventory[2]);
		shuffle.setText(inventory[3]);
		String tiles = "";
		if (inventory[4] != null) tiles = String.valueOf(Integer.parseInt(inventory[4]) * 2);
		thunder.setText(getString(R.string.inventory_thunder_and_tiles, inventory[4], tiles));
		autoClickButtonTimer = findViewById(R.id.autoclick_button_timer);
		autoClickButtonTimerValue = Long.parseLong(autoClickButtonTimer.getText().toString());
		autoClickButtonTimer.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.length() > 0)
					autoClickButtonTimerValue = Integer.parseInt(charSequence.toString());
			}

			@Override
			public void afterTextChanged(Editable editable) {}
		});
		autoClickPointTimer = findViewById(R.id.autoclick_point_timer);
		autoClickPointTimerValue = Long.parseLong(autoClickPointTimer.getText().toString());
		autoClickPointTimer.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.length() > 0)
					autoClickPointTimerValue = Integer.parseInt(charSequence.toString());
			}

			@Override
			public void afterTextChanged(Editable editable) {}
		});
		buttonSend = findViewById(R.id.button_send);
		if (availableSendButton) {
			buttonSend.setOnClickListener(buttonSendListener);
		} else {
			buttonSend.setVisibility(View.GONE);
		}
		buttonOn = findViewById(R.id.button_on);
		buttonOn.setOnClickListener(view -> {
			if (!ButtonOverlayService.isServiceRun)
				startService(new Intent(this, ButtonOverlayService.class));
		});
		buttonOff = findViewById(R.id.button_off);
		buttonOff.setOnClickListener(view -> {
			if (ButtonOverlayService.isServiceRun)
				stopService(new Intent(this, ButtonOverlayService.class));
		});
	}

	private final View.OnClickListener buttonSendListener = view ->  {
		if (RunningApps.isAppRunning(this, packageName)) {
			ToastUtils.longCall(R.string.game_is_running_message);
		} else {
			if (GameJSON.copyRequestFile(App.get())) {
				Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
				if (launchIntent != null) {
					startActivity(launchIntent);
				}
			}
		}
	};

	@Override
	protected void onDestroy() {
		stopService(new Intent(this, ButtonOverlayService.class));
		if (PermissionTools.isShizukuAvailable()) {
			Shizuku.removeRequestPermissionResultListener(this);
		}
		super.onDestroy();
	}

	private void checkAllPermissions() {
		// Check overlay permission
		if (checkDrawOverlayPermission()) {
			startOverlayService();
		}
		// Check accessibility permission
		if (!isAccessibilityServiceEnabled(this, TapAccessibilityService.class)) {
			showAccessibilityPermission();
		}
		// Check storage permission
		if (PermissionTools.hasStoragePermission()) {
			checkPermissionToFiles();
		} else {
			showStoragePermissionDialog();
		}
	}

	private void checkPermissionToFiles() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && PermissionTools.isShizukuAvailable()) {
			if (PermissionTools.hasShizukuPermission()) {
				FileTools.specialPathReadType = PathType.SHIZUKU;
			} else {
				PermissionTools.requestShizukuPermission();
			}
		} else if (FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
			showRequestUriPermissionDialog();
		}
	}

	private boolean checkDrawOverlayPermission() {
		/* check if we already  have permission to draw over other apps */
		if (!Settings.canDrawOverlays(this)) {
			/* if not construct intent to request permission */
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
					Uri.parse("package:" + getPackageName()));
			/* request permission via start activity for result */
			startActivityIntent.launch(intent);
			return false;
		}
		return true;
	}

	public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
		ComponentName expectedComponentName = new ComponentName(context, accessibilityService);

		String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
		if (enabledServicesSetting == null)
			return false;

		TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
		colonSplitter.setString(enabledServicesSetting);

		while (colonSplitter.hasNext()) {
			String componentNameString = colonSplitter.next();
			ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

			if (enabledService != null && enabledService.equals(expectedComponentName))
				return true;
		}

		return false;
	}

	private void startOverlayService() {
		Intent intent = new Intent(this, ButtonOverlayService.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(intent);
		} else {
			startService(intent);
		}
	}

	private void showAccessibilityPermission() {
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.dialog_accessibility_message)
				.setPositiveButton(R.string.dialog_button_request_permission, (dialog, which) ->
						PermissionTools.requestAccessibilityPermission(this))
				.setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
				}).create().show();
	}

	private void showStoragePermissionDialog() {
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.dialog_storage_message)
				.setPositiveButton(R.string.dialog_button_request_permission, (dialog, which) ->
						PermissionTools.requestStoragePermission(this))
				.setNegativeButton(R.string.dialog_button_cancel, (dialog, which) ->
						finish()).create().show();
	}

	private void showRequestUriPermissionDialog() {
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.dialog_need_uri_permission_message)
				.setPositiveButton(R.string.dialog_button_request_permission, (dialog, which) ->
						FileTools.requestUriPermission(this, FileTools.dataPath))
				.setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
				}).create().show();
	}

	ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> this.startOverlayService());

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == RequestCode.STORAGE) {
			onStoragePermissionResult(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RequestCode.STORAGE) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				onStoragePermissionResult(Environment.isExternalStorageManager());
			}
		} else if (requestCode == RequestCode.DOCUMENT) {
			Uri uri;
			if (data != null && (uri = data.getData()) != null) {
				getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				onDocumentPermissionResult(true);
			} else {
				onDocumentPermissionResult(false);
			}
		}
	}

	protected void onStoragePermissionResult(boolean granted) {
		if (granted) {
			ToastUtils.shortCall(R.string.toast_permission_granted);
			checkPermissionToFiles();
		} else {
			ToastUtils.shortCall(R.string.toast_permission_not_granted);
			showStoragePermissionDialog();
		}
	}

	protected void onDocumentPermissionResult(boolean granted) {
		if (granted) {
			ToastUtils.shortCall(R.string.toast_permission_granted);
		} else {
			ToastUtils.shortCall(R.string.toast_permission_not_granted);
		}
	}

	@Override
	public void onRequestPermissionResult(int requestCode, int grantResult) {
		if (requestCode == RequestCode.SHIZUKU) {
			if (grantResult == PackageManager.PERMISSION_GRANTED) {
				FileTools.specialPathReadType = PathType.SHIZUKU;
			}
		}
	}
}