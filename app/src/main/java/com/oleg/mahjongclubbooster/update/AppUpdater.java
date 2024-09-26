package com.oleg.mahjongclubbooster.update;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.oleg.mahjongclubbooster.MainActivity;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class AppUpdater {

	private final Activity activity;
	private final String path;
	private final String file;

	AppUpdater(Activity activity, String path, String file) {
		this.activity = activity;
		this.path = path;
		this.file = file;
	}

	private void run() {
		new Thread(() -> downloadFiles(path, file)).start();
	}

	public void execute() {
		run();
	}

	private void downloadFiles(String path, String file){

		try {
			beforeDownloading();
			URL u = new URL(path + file);
			InputStream is = u.openStream();
			int srcLength = u.openConnection().getContentLength();

			DataInputStream dis = new DataInputStream(is);

			byte[] buffer = new byte[1024];
			int length, totalLength = 0;

			try (FileOutputStream fos = new FileOutputStream(activity.getExternalFilesDir(null) + "/" + file)) {
				while ((length = dis.read(buffer)) > 0) {
					fos.write(buffer, 0, length);
					totalLength += length;
					onDownloading(srcLength, totalLength);
				}
				onDownloaded();
				installDownloadedApp();
			} catch (Exception e) {
				Log.w("downloadFiles_fos", e.toString());
			}
		} catch (IOException ioe) {
			Log.e("downloadFiles", "IO error", ioe);
		}
	}

	private void installDownloadedApp () {
		Uri fileLoc = FileProvider.getUriForFile(
				activity,
				"com.oleg.mahjongclubbooster.provider",
				new File(activity.getExternalFilesDir(null) + "/" + file));
		Intent promptInstall = new Intent(Intent.ACTION_VIEW)
				.setDataAndType(fileLoc, "application/vnd.android.package-archive");
		promptInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		promptInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		if (MainActivity.isUnknownAppSourceAllowed) {
			try {
				activity.startActivity(promptInstall);
			} catch (ActivityNotFoundException e) {
				Log.w("installDownloadedApp", e.toString());
			}
		}
	}

	public abstract void beforeDownloading();
	public abstract void onDownloading(int maxProgress, int progress);
	public abstract void onDownloaded();
}
