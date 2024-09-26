package com.oleg.mahjongclubbooster.overlay;

import static com.oleg.mahjongclubbooster.MainActivityViewInit.getAutoClickButtonTimerValue;
import static com.oleg.mahjongclubbooster.MainActivityViewInit.getAutoClickPointTimerValue;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.oleg.mahjongclubbooster.App;
import com.oleg.mahjongclubbooster.R;
import com.oleg.mahjongclubbooster.autoclick.TapAccessibilityService;
import com.oleg.mahjongclubbooster.constant.BroadcastCode;
import com.oleg.mahjongclubbooster.constant.TapCode;
import com.oleg.mahjongclubbooster.json.GameJSON;
import com.oleg.mahjongclubbooster.tools.SharedPreferencesTools;

import java.util.Objects;
import java.util.Set;

public class FloatingView extends FrameLayout implements View.OnClickListener {

	private final Context mContext;
	private final Button button;
	private final FloatingManager floatingManager;
	private ImageButton point;
	private long lastPressTime;
	private boolean autoClickEnabled = false;
	private static boolean secondPointIsOn = false;

	@SuppressLint({"ClickableViewAccessibility", "InflateParams"})
	public FloatingView(@NonNull Context context) {
		super(context);
		mContext = context.getApplicationContext();
		button = new Button(mContext);
		button.setBackgroundResource(R.drawable.round_button);
		button.setTextSize(25);
		button.setText(R.string.button_default_text);
		button.setOnClickListener(this);
		button.setOnClickListener(arg0 -> {
			if (autoClickEnabled) {
				Intent intentAutoClick = new Intent(App.get(), TapAccessibilityService.class);
				intentAutoClick.putExtra(TapCode.ACTION, TapCode.STOP);
				mContext.startService(intentAutoClick);
				button.setBackgroundResource(R.drawable.round_button);
				autoClickEnabled = false;
			} else {
				String level = GameJSON.getCurrentLevel(App.get());
				button.setText(level);
				if (!level.equals(App.get().getResources().getString(R.string.button_try_again_text)))
					GameJSON.currentLevelStatusPatch(mContext, level);
			}
		});
		floatingManager = FloatingManager.getInstance(mContext);

		LocalBroadcastManager.getInstance(App.get()).registerReceiver(
				new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						if (Objects.equals(intent.getAction(), BroadcastCode.BROADCAST_BUTTON_TEXT)) {
							String text = intent.getStringExtra(BroadcastCode.BUTTON_TEXT);
							button.setText(text);
						}
					}
				}, new IntentFilter(BroadcastCode.BROADCAST_BUTTON_TEXT)
		);
	}

	private void setOverlayListener(View view, WindowManager.LayoutParams paramsF, boolean isSecondPoint) {
		view.setOnTouchListener(new View.OnTouchListener() {
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!autoClickEnabled) switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						long pressTime = System.currentTimeMillis();
						if (!isSecondPoint) {
							// If double click...
							if (pressTime - lastPressTime <= 500) {
								initiatePopupWindow(v);
							}
							lastPressTime = pressTime;
						}
						initialX = paramsF.x;
						initialY = paramsF.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						break;
					case MotionEvent.ACTION_UP:
						int[] location = new int[2];
						if (isSecondPoint) {
							point.getLocationOnScreen(location);
						} else {
							button.getLocationOnScreen(location);
						}
						SharedPreferencesTools.addProperties(
								isSecondPoint ?
										SharedPreferencesTools.APP_PREFERENCES_POINT_COORDINATES :
										SharedPreferencesTools.APP_PREFERENCES_BUTTON_COORDINATES,
								location[0],
								location[1]);
						break;
					case MotionEvent.ACTION_MOVE:
						paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
						paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
						floatingManager.updateView(view, paramsF);
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
				mContext.getString(
						R.string.popupmenu_item1,
						getAutoClickButtonTimerValue() +
								(secondPointIsOn ? getAutoClickPointTimerValue() : 0)
				),
				mContext.getString(R.string.popupmenu_item2),
				mContext.getString(R.string.popupmenu_item3),
				mContext.getString(R.string.popupmenu_item4),
				mContext.getString(R.string.popupmenu_item5)
		};
		ListPopupWindow listPopupWindow = new ListPopupWindow(mContext);
		listPopupWindow.setAnchorView(anchor);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.popup_menu_item, menu);
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
				intent.putExtra("interval", getAutoClickButtonTimerValue());
				int[] location = new int[2];
				button.getLocationOnScreen(location);
				intent.putExtra("x", location[0]);
				intent.putExtra("y", location[1] + button.getHeight());
				mContext.startService(intent);
				if (secondPointIsOn) {
					intent = new Intent(App.get(), TapAccessibilityService.class);
					intent.putExtra(TapCode.ACTION, TapCode.SECOND_POINT);
					intent.putExtra("interval", getAutoClickPointTimerValue());
					location = new int[2];
					point.getLocationOnScreen(location);
					intent.putExtra("x", location[0] - 1);
					intent.putExtra("y", location[1] - 1);
					mContext.startService(intent);
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
			} else if (position == 3) {             //Switch on the second point of auto click
				if (point == null) {
					point = new ImageButton(mContext);
					point.setBackgroundResource(R.drawable.aim);
					point.setClickable(false);
					point.setFocusable(false);
					Set<String> storedPosition = SharedPreferencesTools.getProperties(SharedPreferencesTools.APP_PREFERENCES_POINT_COORDINATES);
					int[] location = new int[2];
					if (storedPosition == null) {
						location[0] = 100;
						location[1] = 100;
					} else {
						String[] val = storedPosition.toArray(new String[2]);
						location[0] = Integer.parseInt(val[0]);
						location[1] = Integer.parseInt(val[1]);
					}
					show(location[0], location[1], true);
					secondPointIsOn = true;
				}
			} else if (position == 4) {             //Switch off second point of auto click
				if (point != null) {
					hide(true);
					point = null;
					Intent intent = new Intent(App.get(), TapAccessibilityService.class);
					intent.putExtra(TapCode.ACTION, TapCode.SECOND_POINT_OFF);
					mContext.startService(intent);
					secondPointIsOn = false;
				}
			}
			listPopupWindow.dismiss();
		});
		listPopupWindow.show();
	}

	public void show(int x, int y, boolean isSecondPoint) {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.x = x;
		params.y = y;
		/*if (isSecondPoint) */params.gravity = Gravity.TOP | Gravity.START;
		if (Build.VERSION.SDK_INT >= 26) {
			params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		} else {
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		}
		params.format = PixelFormat.RGBA_8888;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		if (isSecondPoint) {
			floatingManager.addView(point, params);
			setOverlayListener(point, params, true);
		} else {
			floatingManager.addView(button, params);
			setOverlayListener(button, params, false);
			button.setText(GameJSON.getCurrentLevel(App.get()));
		}
	}

	public void hide(boolean isSecondPoint) {
		floatingManager.removeView(isSecondPoint ? point : button);
	}

	public static boolean isSecondPointIsOn() {
		return secondPointIsOn;
	}

	public boolean isSecondPoint() {
		return secondPointIsOn;
	}

	@Override
	public void onClick(View view) {
		if (autoClickEnabled) {
			Intent intentAutoClick = new Intent(App.get(), TapAccessibilityService.class);
			intentAutoClick.putExtra(TapCode.ACTION, TapCode.STOP);
			mContext.startService(intentAutoClick);
			button.setBackgroundResource(R.drawable.round_button);
			autoClickEnabled = false;
		} else {
			String level = GameJSON.getCurrentLevel(App.get());
			button.setText(level);
			if (!level.equals(App.get().getResources().getString(R.string.button_try_again_text)))
				GameJSON.currentLevelStatusPatch(mContext, level);
		}
	}
}
