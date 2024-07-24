package com.oleg.mahjongclubbooster.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.oleg.mahjongclubbooster.App;
import com.oleg.mahjongclubbooster.constant.PathType;
import com.oleg.mahjongclubbooster.constant.RequestCode;
import com.oleg.mahjongclubbooster.userservice.IFileExplorerService;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileTools {
    public static String ROOT_PATH;
//            "/storage/0110-0030/";
    public static String dataPath;
    public static String mahjongClubFilesPath;
    public static int specialPathReadType = PathType.DOCUMENT;
    public static IFileExplorerService iFileExplorerService;

    public static void defineRootPath(Context context) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N &&
                android.os.Build.DEVICE.contains("Huawei") || android.os.Build.MANUFACTURER.contains("Huawei")) {
	        ArrayList<File> extStorages = new ArrayList<>(Arrays.asList(context.getExternalFilesDirs(null)));
            ROOT_PATH = extStorages.get(1).getPath().split("/Android")[0];
        } else {
            ROOT_PATH = Environment.getExternalStorageDirectory().getPath();
        }
        dataPath = FileTools.ROOT_PATH + "/Android/data/";
        mahjongClubFilesPath = FileTools.ROOT_PATH + "/Android/data/com.gamovation.mahjongclub/files/";
    }

    public static boolean shouldRequestUriPermission(String path) {
/*
        if (getPathType(path) != PathType.DOCUMENT) {
            return false;
        }
*/
        return !hasUriPermission(path);
    }

    @PathType.PathType1
    private static int getPathType(String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if ((dataPath).contains(path)) {
                return PathType.DOCUMENT;
            } else {
                return PathType.FILE;
            }
        } else {
            return PathType.FILE;
        }
    }

    private static boolean hasUriPermission(String path) {
        List<UriPermission> uriPermissions = App.get().getContentResolver().getPersistedUriPermissions();
        Log.d("TAG", "hasUriPermission: uriPermissions = " + uriPermissions);
        String uriPath = pathToUri(path).getPath();
        Log.d("TAG", "hasUriPermission: uriPath = "+uriPath);
        for (UriPermission uriPermission : uriPermissions) {
            String itemPath = uriPermission.getUri().getPath();
            Log.d("TAG", "hasUriPermission: itemPath = " + itemPath);
            if (uriPath != null && itemPath != null && (uriPath + "/").contains(itemPath + "/")) {
                return true;
            }
        }
        return false;
    }

    public static void requestUriPermission(Activity activity, String path) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
	    Uri treeUri = pathToUri(path);
	    DocumentFile df = DocumentFile.fromTreeUri(activity, treeUri);
	    if (df != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, df.getUri());
	    }
	    activity.startActivityForResult(intent, RequestCode.DOCUMENT);
    }

    public static Uri pathToUri(String path) {
        String halfPath = path.replace(ROOT_PATH + "/", "");
        String[] segments = halfPath.split("/");
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme("content")
                .authority("com.android.externalstorage.documents")
                .appendPath("tree");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uriBuilder.appendPath("primary:A\u200Bndroid/" + segments[1]);
        } else {
            uriBuilder.appendPath("primary:Android/" + segments[1]);
        }
        uriBuilder.appendPath("document");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uriBuilder.appendPath("primary:A\u200Bndroid/" + halfPath.replace("Android/", ""));
        } else {
            uriBuilder.appendPath("primary:" + halfPath);
        }
        return uriBuilder.build();
    }

    public static void cleanDailyChallengeLevelsStatus() {
        Uri pathUri = pathToUri(mahjongClubFilesPath + "DailyChallengeLevelsStatus/");
        Log.d("Files", "getFileListByDocument: pathUri = "+pathUri);
        DocumentFile documentFile = DocumentFile.fromTreeUri(App.get(), pathUri);
        if (documentFile != null) {
            DocumentFile[] documentFiles = documentFile.listFiles();
            for (DocumentFile df : documentFiles) {
                df.delete();
            }
        }
    }

    public static Boolean makeBackupFolder(Context context) {
        File dir = new File (Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath() + "/MahjongClubBackup");
        if(!dir.exists()) {
	        return dir.mkdirs();
        }
        return false;
    }

    public static String copyFile(Context context, String inputPath, String inputFile, String destPath) {
        InputStream in = null;
        OutputStream out = null;
        String error = null;
        Uri pathUri = pathToUri(inputPath);
        DocumentFile inputDir = DocumentFile.fromTreeUri(context, pathUri);
        pathUri = pathToUri(destPath);
        DocumentFile destDir = DocumentFile.fromTreeUri(context, pathUri);

        try {
            DocumentFile newFile = inputDir.createFile("application/*", inputFile);
            out = context.getContentResolver().openOutputStream(newFile.getUri());
            in = context.getContentResolver().openInputStream(destDir.getUri());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            // write the output file (You have now copied the file)
            out.flush();
            out.close();

        } catch (Exception e) {
            error = e.getMessage();
        }
        return error;
    }
}
