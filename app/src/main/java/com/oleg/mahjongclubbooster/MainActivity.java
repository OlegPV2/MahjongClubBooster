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
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.oleg.mahjongclubbooster.autoclick.TapAccessibilityService;
import com.oleg.mahjongclubbooster.constant.PathType;
import com.oleg.mahjongclubbooster.constant.RequestCode;
import com.oleg.mahjongclubbooster.update.CheckUpdate;
import com.oleg.mahjongclubbooster.userservice.FileExplorerServiceManager;
import com.oleg.mahjongclubbooster.util.FileTools;
import com.oleg.mahjongclubbooster.util.PermissionTools;
import com.oleg.mahjongclubbooster.util.SPUtils;
import com.oleg.mahjongclubbooster.util.ToastUtils;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener {

	private static final String TAG = "Overlay";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		CheckUpdate.checkUpdate(this);
		FileTools.defineRootPath(this);
		if (PermissionTools.isShizukuAvailable()) {
			Shizuku.addRequestPermissionResultListener(this);
		}
		checkAllPermissions();
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
		if(!isAccessibilityServiceEnabled(this, TapAccessibilityService.class)){
			Toast.makeText(MainActivity.this, "please enable accessibility service", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
		// Check storage permission
		if (PermissionTools.hasStoragePermission()) {
			if (!SPUtils.getUseNewDocument()) {
				checkShizukuPermission();
			}
		} else {
			showStoragePermissionDialog();
		}
	}

	private boolean checkDrawOverlayPermission() {
		/* check if we already  have permission to draw over other apps */
		if (!Settings.canDrawOverlays(this)) {
			Log.d(TAG, "canDrawOverlays NOK");
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

		String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
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

	ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> this.startOverlayService());

	// Storage and file access part

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

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == RequestCode.STORAGE) {
			onStoragePermissionResult(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
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

	private void checkShizukuPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (PermissionTools.isShizukuAvailable()) {
				if (PermissionTools.hasShizukuPermission()) {
					FileTools.specialPathReadType = PathType.SHIZUKU;
					FileExplorerServiceManager.bindService();
				} else {
					PermissionTools.requestShizukuPermission();
				}
			}
		}
	}

	protected void onStoragePermissionResult(boolean granted) {
		if (granted) {
			ToastUtils.shortCall(R.string.toast_permission_granted);
			checkShizukuPermission();
			if (FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
				showRequestUriPermissionDialog();
			}
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
				FileExplorerServiceManager.bindService();
			}
		}
	}
}