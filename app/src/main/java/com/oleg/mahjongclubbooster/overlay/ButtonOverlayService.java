package com.oleg.mahjongclubbooster.overlay;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.oleg.mahjongclubbooster.App;
import com.oleg.mahjongclubbooster.MainActivity;
import com.oleg.mahjongclubbooster.R;
import com.oleg.mahjongclubbooster.autoclick.TapAccessibilityService;
import com.oleg.mahjongclubbooster.constant.BroadcastCode;
import com.oleg.mahjongclubbooster.constant.TapCode;
import com.oleg.mahjongclubbooster.util.GameJSON;

import java.util.Objects;

public class ButtonOverlayService extends Service {

	public static boolean isServiceRun = false;
	public static boolean secondPointIsOn = false;

	private WindowManager windowManager;
	private Button button;
	private ImageButton point;
	private boolean autoClickEnabled = false;

	long lastPressTime;
	boolean mHasDoubleClicked = false;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onCreate();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		button = new Button(this);
		button.setBackgroundResource(R.drawable.round_button);
		button.setTextSize(25);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						if (Objects.equals(intent.getAction(), BroadcastCode.BROADCAST_BUTTON_TEXT)) {
							String text = intent.getStringExtra(BroadcastCode.BUTTON_TEXT);
							if (button != null) button.setText(text);
						}
					}
				}, new IntentFilter(BroadcastCode.BROADCAST_BUTTON_TEXT)
		);

		WindowManager.LayoutParams params = getLayoutParams();
		params.gravity = Gravity.TOP | Gravity.START;
		params.x = 0;
		params.y = 100;

		windowManager.addView(button, params);

		setOverlayListener(button, params, true);

		button.setOnClickListener(arg0 -> {
			if (autoClickEnabled) {
				Intent intentAutoClick = new Intent(App.get(), TapAccessibilityService.class);
				intentAutoClick.putExtra(TapCode.ACTION, TapCode.STOP);
				startService(intentAutoClick);
				button.setBackgroundResource(R.drawable.round_button);
				autoClickEnabled = false;
			} else {
				String level = GameJSON.getCurrentLevel(App.get());
				button.setText(level);
				if (!level.equals(App.get().getResources().getString(R.string.button_try_again_text)))
					GameJSON.currentLevelStatusPatch(this, level);
			}
		});
		button.setText(GameJSON.getCurrentLevel(App.get()));

		isServiceRun = true;

		return START_STICKY;
	}

	private WindowManager.LayoutParams getLayoutParams() {
		if (Build.VERSION.SDK_INT >= 26) {
			String CHANNEL_ID = "channel1";
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
					"Overlay notification",
					NotificationManager.IMPORTANCE_LOW);

			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
					.createNotificationChannel(channel);

			Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
					.setContentTitle("Mahjong Club Booster")
					.setContentText("Foreground process")
					.setSmallIcon(R.mipmap.ic_launcher)
					.build();

			startForeground(1, notification);

			return new WindowManager.LayoutParams(
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);
		} else {
			return new WindowManager.LayoutParams(
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_PHONE,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);

		}
	}

	private void setOverlayListener(View view, WindowManager.LayoutParams paramsF, boolean isDoubleClickOn) {
		view.setOnTouchListener(new View.OnTouchListener() {
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!autoClickEnabled) switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						long pressTime = System.currentTimeMillis();
						if (isDoubleClickOn) {
							// If double click...
							if (pressTime - lastPressTime <= 500) {
								initiatePopupWindow(v);
								mHasDoubleClicked = true;
							} else {     // If not double click....
								mHasDoubleClicked = false;
							}
							lastPressTime = pressTime;
						}
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
						windowManager.updateViewLayout(view, paramsF);
						break;
				}
				return false;
			}
		});
	}
	private int measureContentWidth(String[] listAdapter) {
		int maxWidth = 0;
		Paint p = new Paint();
		Rect bounds = new Rect();
		for (String s : listAdapter) {
			p.getTextBounds(s, 0, s.length(), bounds);
			maxWidth = Math.max(bounds.width(), maxWidth);
		}
		return maxWidth;
	}

	private void initiatePopupWindow(View anchor) {
		String[] menu = new String[]{
				getString(R.string.popupmenu_item1, MainActivity.autoClickButtonTimerValue + (secondPointIsOn ? MainActivity.autoClickPointTimerValue : 0)),
				getString(R.string.popupmenu_item2),
				getString(R.string.popupmenu_item3),
				getString(R.string.popupmenu_item4),
				getString(R.string.popupmenu_item5)
		};
		ListPopupWindow listPopupWindow = new ListPopupWindow(this);
		listPopupWindow.setAnchorView(anchor);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), R.layout.popup_menu_item, menu);
		listPopupWindow.setWidth(measureContentWidth(menu) * 2);
		listPopupWindow.setAdapter(adapter);
		listPopupWindow.setModal(true);
		listPopupWindow.setOnItemClickListener((arg0, view, position, id3) -> {
			if (position == 0) {                    //Switch on auto click
				String level = GameJSON.getCurrentLevel(App.get());
				button.setText(level);
				if (!level.equals(App.get().getResources().getString(R.string.button_try_again_text)))
					GameJSON.currentLevelStatusPatch(App.get(), level);
				button.setBackgroundResource(R.drawable.round_button_green);
				Intent intent = new Intent(App.get(), TapAccessibilityService.class);
				intent.putExtra(TapCode.ACTION, TapCode.PLAY);
				intent.putExtra("interval", MainActivity.autoClickButtonTimerValue);
				int[] location = new int[2];
				button.getLocationOnScreen(location);
				intent.putExtra("x", location[0]);
				intent.putExtra("y", location[1] + button.getHeight());
				startService(intent);
				if (secondPointIsOn) {
					intent = new Intent(App.get(), TapAccessibilityService.class);
					intent.putExtra(TapCode.ACTION, TapCode.SECOND_POINT);
					intent.putExtra("interval", MainActivity.autoClickPointTimerValue);
					location = new int[2];
					point.getLocationOnScreen(location);
					intent.putExtra("x", location[0] - 1);
					intent.putExtra("y", location[1] - 1);
					startService(intent);
				}
				autoClickEnabled = true;
			} else if (position == 1) {             //Make level with puzzles
				String level = GameJSON.getCurrentLevel(App.get());
				button.setText(level);
				if (!level.equals(App.get().getResources().getString(R.string.button_try_again_text)))
					GameJSON.currentLevelStatusPuzzlesPatch(App.get(), level);
			} else if (position == 2) {             //Make level with butterflies
				String level = GameJSON.getCurrentLevel(App.get());
				button.setText(level);
				if (!level.equals(App.get().getResources().getString(R.string.button_try_again_text)))
					GameJSON.currentLevelStatusButterflyPatch(App.get(), level);
			} else if (position == 3) {             //Switch on second point of auto click
				point = new ImageButton(this);
				point.setBackgroundResource(R.drawable.aim);
				point.setClickable(false);
				point.setFocusable(false);
				WindowManager.LayoutParams params = getLayoutParams();
				params.gravity = Gravity.TOP | Gravity.START;
				params.x = 100;
				params.y = 100;
				windowManager.addView(point, params);
				setOverlayListener(point, params, false);

				secondPointIsOn = true;
			} else if (position == 4) {             //Switch off second point of auto click
				windowManager.removeView(point);
				point = null;
				Intent intent = new Intent(App.get(), TapAccessibilityService.class);
				intent.putExtra(TapCode.ACTION, TapCode.SECOND_POINT_OFF);
				startService(intent);
				secondPointIsOn = false;
			}
			listPopupWindow.dismiss();
		});
		listPopupWindow.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (button != null) {
			windowManager.removeView(button);
			button = null;
		}
		if (point != null) {
			windowManager.removeView(point);
			point = null;
		}
		isServiceRun = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}