package com.oleg.mahjongclubbooster;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.oleg.mahjongclubbooster.autoclick.TapAccessibilityService;
import com.oleg.mahjongclubbooster.constant.PathType;
import com.oleg.mahjongclubbooster.constant.RequestCode;
import com.oleg.mahjongclubbooster.util.FileTools;
import com.oleg.mahjongclubbooster.util.GameJSON;
import com.oleg.mahjongclubbooster.util.PermissionTools;
import com.oleg.mahjongclubbooster.util.ToastUtils;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener {

	LinearLayout layoutEventsCollapsable, layoutEvents;
	LinearLayout layoutInventoryCollapsable, layoutInventory;
	TextView butterflyStart, butterflyEnd;
	TextView chestStart, chestEnd;
	TextView tournamentStart, tournamentEnd;
	TextView puzzlesStart, puzzlesEnd;
	TextView zenStart, zenEnd;
	TextView zenSilver, hint, bomb, shuffle, thunder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		CheckUpdate.checkUpdate(this);
		FileTools.defineRootPath(this);
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
				expand(layoutEventsCollapsable);
			} else {
				collapse(layoutEventsCollapsable);
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
		zenStart = findViewById(R.id.zen_start_date);
		zenEnd = findViewById(R.id.zen_end_date);
		String[] dates = GameJSON.getDates(this);
		butterflyStart.setText(dates[0]);
		butterflyEnd.setText(dates[1]);
		chestStart.setText(dates[2]);
		chestEnd.setText(dates[3]);
		tournamentStart.setText(dates[4]);
		tournamentEnd.setText(dates[5]);
		puzzlesStart.setText(dates[6]);
		puzzlesEnd.setText(dates[7]);
		zenStart.setText(dates[8]);
		zenEnd.setText(dates[9]);

		layoutInventoryCollapsable = findViewById(R.id.layout_inventory_collapsable);
		layoutInventory = findViewById(R.id.layout_inventory);
		layoutInventory.setOnClickListener(view -> {
			if (layoutInventoryCollapsable.getVisibility() == View.GONE) {
				expand(layoutInventoryCollapsable);
			} else {
				collapse(layoutInventoryCollapsable);
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
		thunder.setText(inventory[4]);
	}

	private void expand(View view) {
		view.setVisibility(View.VISIBLE);

		final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(widthSpec, heightSpec);

		ValueAnimator mAnimator = slideAnimator(view, 0, view.getMeasuredHeight());
		mAnimator.start();
	}

	private void collapse(View view) {
		int finalHeight = view.getHeight();

		ValueAnimator mAnimator = slideAnimator(view, finalHeight, 0);

		mAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(@NonNull Animator animation) {
			}

			@Override
			public void onAnimationEnd(@NonNull Animator animator) {
				//Height=0, but it set visibility to GONE
				view.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(@NonNull Animator animation) {
			}

			@Override
			public void onAnimationRepeat(@NonNull Animator animation) {
			}
		});
		mAnimator.start();
	}

	private ValueAnimator slideAnimator(View view, int start, int end) {

		ValueAnimator animator = ValueAnimator.ofInt(start, end);

		animator.addUpdateListener(valueAnimator -> {
			//Update Height
			int value = (Integer) valueAnimator.getAnimatedValue();
			ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
			layoutParams.height = value;
			view.setLayoutParams(layoutParams);
		});
		return animator;
	}

	@Override
	protected void onDestroy() {
		stopService(new Intent(this, OverlayService.class));
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
		Intent intent = new Intent(this, OverlayService.class);
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