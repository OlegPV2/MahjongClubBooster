package com.oleg.mahjongclubbooster.autoclick;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.accessibility.AccessibilityEvent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.oleg.mahjongclubbooster.App;
import com.oleg.mahjongclubbooster.R;
import com.oleg.mahjongclubbooster.constant.BroadcastCode;
import com.oleg.mahjongclubbooster.constant.TapCode;
import com.oleg.mahjongclubbooster.overlay.FloatingView;
import com.oleg.mahjongclubbooster.json.GameJSON;
import com.oleg.mahjongclubbooster.tools.ToastTools;

public class TapAccessibilityService extends AccessibilityService {

	private Handler mHandler;
	private int mX;
	private int mY;
	private long mInterval;
	private int sX;
	private int sY;
	private long sInterval;
	private int nextTap = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		HandlerThread handlerThread = new HandlerThread("tap-handler");
		handlerThread.start();
		mHandler = new Handler(handlerThread.getLooper());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent != null) {
			String action = intent.getStringExtra(TapCode.ACTION);
			if (TapCode.PLAY.equals(action)) {
				mX = intent.getIntExtra("x", 0);
				mY = intent.getIntExtra("y", 0);
				mInterval = intent.getLongExtra("interval", 10000);
				if (myRunnable == null) {
					myRunnable = new myRunnable();
				}
				mHandler.post(myRunnable);
				ToastTools.shortCall(R.string.autoclick_enabled);
			} else if (TapCode.STOP.equals(action)) {
				mHandler.removeCallbacksAndMessages(null);
				nextTap = 0;
				ToastTools.shortCall(R.string.autoclick_disabled);
			} else if (TapCode.SECOND_POINT.equals(action)) {
				sX = intent.getIntExtra("x", 0);
				sY = intent.getIntExtra("y", 0);
				sInterval = intent.getLongExtra("interval", 10000);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
	}

	@Override
	public void onInterrupt() {

	}

	private void tap(int x, int y) {
		String level = GameJSON.getCurrentLevel(App.get());
		if (level.equals(App.get().getResources().getString(R.string.button_try_again_text)))
			level = GameJSON.getCurrentLevel(App.get());
		GameJSON.currentLevelStatusPatch(App.get(), level);
		Path swipePath = new Path();
		swipePath.moveTo(x, y);
		swipePath.lineTo(x, y);
		GestureDescription.Builder gBuilder = new GestureDescription.Builder();
		gBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 1));
		String finalLevel = level;
		dispatchGesture(gBuilder.build(), new GestureResultCallback() {
			@Override
			public void onCompleted(GestureDescription gestureDescription) {
				super.onCompleted(gestureDescription);
				if (FloatingView.isSecondPointIsOn()) {
					if (nextTap == 0) {
						sendUpdateTextToButton(finalLevel);
						mHandler.removeCallbacksAndMessages(myRunnable);
						mHandler.postDelayed(myRunnable, sInterval);
					} else if (nextTap == 1) {
						mHandler.postDelayed(myRunnable, mInterval);
					}
				} else {
					sendUpdateTextToButton(finalLevel);
					mHandler.removeCallbacksAndMessages(myRunnable);
					mHandler.postDelayed(myRunnable, mInterval);
				}
			}

			@Override
			public void onCancelled(GestureDescription gestureDescription) {
				super.onCancelled(gestureDescription);
			}
		}, null);
	}

	private myRunnable myRunnable;

	private class myRunnable implements Runnable {

		@Override
		public void run() {
			switch (nextTap) {
				case 0:
					tap(mX, mY);
					if (FloatingView.isSecondPointIsOn()) nextTap += 1;
					break;
				case 1:
					tap(sX, sY);
					nextTap = 0;
					break;
			}
		}
	}

	private void sendUpdateTextToButton(String text) {
		Intent intent = new Intent(BroadcastCode.BROADCAST_BUTTON_TEXT);
		intent.putExtra(BroadcastCode.BUTTON_TEXT, text);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
