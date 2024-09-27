package com.oleg.mahjongclubbooster.overlay;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.oleg.mahjongclubbooster.R;
import com.oleg.mahjongclubbooster.tools.SharedPreferencesTools;

import java.util.Set;

public class ButtonOverlayService extends Service {

	private FloatingView floatingView;
//	private Handler handler;
//	private int currentLevelCounter;
//	private boolean useDataLevelNumber = false;
	private static boolean serviceRun = false;

	@Override
	public void onCreate() {
		super.onCreate();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String CHANNEL_1_ID = "channel1";
			NotificationChannel channel = new NotificationChannel(CHANNEL_1_ID,
					"Overlay notification",
					NotificationManager.IMPORTANCE_LOW);

			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
					.createNotificationChannel(channel);

			Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
					.setContentTitle("Mahjong Club Booster")
					.setContentText("Foreground process")
					.setSmallIcon(R.drawable.ic_stat_name)
					.build();

			startForeground(1, notification);
		}

		floatingView = new FloatingView(this);
		HandlerThread handlerThread = new HandlerThread("level-handler");
		handlerThread.start();
/*
		handler = new Handler(handlerThread.getLooper());
		runnable = new LevelRunnable();
		handler.post(runnable);
*/
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onCreate();
		Set<String> storedPosition = SharedPreferencesTools.getProperties(SharedPreferencesTools.APP_PREFERENCES_BUTTON_COORDINATES);
		int[] location = new int[2];
		if (storedPosition.size() < 2) {
			location[0] = 100;
			location[1] = 100;
		} else {
			String[] val = storedPosition.toArray(new String[2]);
			location[0] = Integer.parseInt(val[0]);
			location[1] = Integer.parseInt(val[1]);
		}

		floatingView.show(location[0], location[1], false);

		serviceRun = true;
		return super.onStartCommand(intent, flags, startId);
	}

	public static boolean isServiceRun() {
		return serviceRun;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
/*		if (handler != null) {
			handler.removeCallbacksAndMessages(runnable);
		}*/
		if (floatingView.isSecondPoint())
			floatingView.hide(true);
		floatingView.hide(false);
		serviceRun = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

/*	private LevelRunnable runnable;

	private class LevelRunnable implements Runnable {
		@Override
		public void run() {
			currentLevelCounter = Integer.parseInt(GameJSON.getCurrentLevelFromLevelsData(context));
			floatingView.updateText(String.valueOf(currentLevelCounter));
			useDataLevelNumber = true;
			currentLevelCounter--;
			handler.removeCallbacksAndMessages(runnable);
		}
	}*/
}