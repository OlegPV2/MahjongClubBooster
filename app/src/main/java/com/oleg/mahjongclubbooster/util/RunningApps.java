package com.oleg.mahjongclubbooster.util;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.List;

public class RunningApps {

	public static boolean isAppRunning(final Context context, final String packageName) {
		UsageStatsManager usage = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
		long time = System.currentTimeMillis();
		List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, time);
		if (stats != null) {
			for (UsageStats usageStats : stats) {
				if (packageName.equals(usageStats.getPackageName())) {
					return true;
				}
			}
		}
		return false;
	}
}
