package com.oleg.mahjongclubbooster;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.FileObserver;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.app.NotificationCompat;

import com.oleg.mahjongclubbooster.autoclick.TapAccessibilityService;
import com.oleg.mahjongclubbooster.util.FileTools;
import com.oleg.mahjongclubbooster.util.GameJSON;
import com.oleg.mahjongclubbooster.util.PermissionTools;
import com.oleg.mahjongclubbooster.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OverlayService extends Service {
	private static final String TITLE = "Autoclick";
	private WindowManager windowManager;
	private static Button button;
	private boolean autoclickEnabled = false;

	long lastPressTime;
	boolean mHasDoubleClicked = false;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onCreate();

		WindowManager.LayoutParams params;

		if (Build.VERSION.SDK_INT >= 26) {
			String CHANNEL_ID = "channel1";
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
					"Overlay notification",
					NotificationManager.IMPORTANCE_LOW);

			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
					.createNotificationChannel(channel);

			Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
					.setContentTitle("adsf")
					.setContentText("asdf1")
					.setSmallIcon(R.mipmap.ic_launcher)
					.build();

			startForeground(1, notification);

			params = new WindowManager.LayoutParams(
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);
		} else {
			params = new WindowManager.LayoutParams(
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_PHONE,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);

		}

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		button = new Button(this);
		button.setBackgroundResource(R.drawable.round_button);
		button.setTextSize(25);
		button.setText("Button");
		button.setAlpha(1);

		params.gravity = Gravity.TOP | Gravity.START;
		params.x = 0;
		params.y = 100;

		windowManager.addView(button, params);

		if (PermissionTools.hasStoragePermission()) {
			FileObserver observerPlayerProfile = new FileObserver(FileTools.mahjongClubFilesPath + "playerProfile.json") {
				@Override
				public void onEvent(int event, @Nullable String s) {
					if (event == FileObserver.MODIFY) {
						updateButtonText();
					}
				}
			};
			observerPlayerProfile.startWatching();
		}

		button.setOnTouchListener(new View.OnTouchListener() {
			private final WindowManager.LayoutParams paramsF = params;
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!autoclickEnabled) switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						long pressTime = System.currentTimeMillis();

						// If double click...
						if (pressTime - lastPressTime <= 500) {
							createNotification();
							initiatePopupWindow(v);
							mHasDoubleClicked = true;
						} else {     // If not double click....
							mHasDoubleClicked = false;
						}
						lastPressTime = pressTime;
						initialX = paramsF.x;
						initialY = paramsF.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						break;
					case MotionEvent.ACTION_UP:
						break;
					case MotionEvent.ACTION_MOVE:
						paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
						paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
						windowManager.updateViewLayout(button, paramsF);
						break;
				}
				return false;
			}
		});


		button.setOnClickListener(arg0 -> {
			if (autoclickEnabled) {
				Intent intentAutoclick = new Intent(App.get(), TapAccessibilityService.class);
				intentAutoclick.putExtra(TapAccessibilityService.ACTION, TapAccessibilityService.STOP);
				startService(intentAutoclick);
				autoclickEnabled = false;
				ToastUtils.shortCall(R.string.autoclick_disabled);
			} else {
				updateButtonText();
				GameJSON.currentLevelStatusPatch(this);
			}
		});
		updateButtonText();

		return START_STICKY;
	}

	private void initiatePopupWindow(View anchor) {
		List<HashMap<String, Object>> data = new ArrayList<>();
		HashMap<String, Object> map = new HashMap<>();
		map.put(TITLE, getString(R.string.popupmenu_item1));
		data.add(map);

		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		ListPopupWindow popup = new ListPopupWindow(this);
		ListAdapter adapter = new SimpleAdapter(
				this,
				data,
				R.layout.row,
				new String[]{TITLE}, // These are just the keys that the data uses (constant strings)
				new int[]{R.id.textView1}); // The view ids to map the data to

		popup.setAnchorView(anchor);
		Point p = new Point();
		display.getSize(p);
		popup.setWidth(p.x / (2));
		popup.setAdapter(adapter);
		popup.setOnItemClickListener((arg0, view, position, id3) -> {
			updateButtonText();
			GameJSON.currentLevelStatusPatch(App.get());
			Intent intent = new Intent(App.get(), TapAccessibilityService.class);
			intent.putExtra(TapAccessibilityService.ACTION, TapAccessibilityService.PLAY);
			intent.putExtra("interval", 10000);
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			intent.putExtra("x", location[0] - 1);
			intent.putExtra("y", location[1] - 1);
			startService(intent);
			autoclickEnabled = true;
			popup.dismiss();
		});
		popup.show();
	}

	public void createNotification() {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (button != null) {
			windowManager.removeView(button);
			button = null;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static void updateButtonText() {
		if (PermissionTools.hasStoragePermission()) {
			button.setText(GameJSON.currentLevel(App.get()));
		}
	}

}