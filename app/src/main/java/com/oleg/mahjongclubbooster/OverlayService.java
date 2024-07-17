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

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.app.NotificationCompat;

import com.oleg.mahjongclubbooster.interfaces.ProcessCallbackInterface;
import com.oleg.mahjongclubbooster.util.FileTools;
import com.oleg.mahjongclubbooster.util.GameJSON;
import com.oleg.mahjongclubbooster.util.PermissionTools;

public class OverlayService extends Service implements ProcessCallbackInterface {
	private WindowManager windowManager;
	private Button button;
	private FileObserver observerPlayerProfile;

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
		button.setText("Button");
		button.setAlpha(1);

		params.gravity = Gravity.TOP | Gravity.START;
		params.x = 0;
		params.y = 100;

		windowManager.addView(button, params);

		if (PermissionTools.hasStoragePermission()) {
			observerPlayerProfile = new FileObserver(FileTools.mahjongClubFilesPath + "playerProfile.json") {
				@Override
				public void onEvent(int event, @Nullable String s) {
					if (event == FileObserver.MODIFY) {
						updateButtonText(GameJSON.currentLevel(getApplicationContext()));
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
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						long pressTime = System.currentTimeMillis();

						// If double click...
						if (pressTime - lastPressTime <= 300) {
							createNotification();
							OverlayService.this.stopSelf();
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
			updateButtonText(GameJSON.currentLevel(this));
			GameJSON.currentLevelStatusPatch(this);
//			initiatePopupWindow(button);
			//				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			//				getApplicationContext().startActivity(intent);
		});
		updateButtonText(GameJSON.currentLevel(getApplicationContext()));

		return START_NOT_STICKY;
	}

	private void initiatePopupWindow(View anchor) {
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		ListPopupWindow popup = new ListPopupWindow(this);
		popup.setAnchorView(anchor);
		Point p = new Point();
		display.getSize(p);
		popup.setWidth((int) (p.x / (1.5)));

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

	@Override
	public void updateButtonText(String text) {
		button.setText(text);
	}
}