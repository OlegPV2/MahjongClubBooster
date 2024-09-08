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
import com.oleg.mahjongclubbooster.R;
import com.oleg.mahjongclubbooster.constant.PathType;
import com.oleg.mahjongclubbooster.constant.RequestCode;
import com.oleg.mahjongclubbooster.shizukuutil.ShizukuFileUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileTools {
    public static String ROOT_PATH;
    public static String dataPath;
    public static String mahjongClubFilesPath;
    public static String localPath;
    public static int specialPathReadType = PathType.DOCUMENT;

    public static void defineRootPath(Context context, String packageName) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N &&
                android.os.Build.DEVICE.contains("Huawei") || android.os.Build.MANUFACTURER.contains("Huawei")) {
	        ArrayList<File> extStorages = new ArrayList<>(Arrays.asList(context.getExternalFilesDirs(null)));
            ROOT_PATH = extStorages.get(1).getPath().split("/Android")[0];
        } else {
            ROOT_PATH = Environment.getExternalStorageDirectory().getPath();
        }
        dataPath = FileTools.ROOT_PATH + "/Android/data/";
        localPath = Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath() + "/";
        mahjongClubFilesPath = FileTools.dataPath + packageName + "/files/";
    }

    public static boolean shouldRequestUriPermission(String path) {
        if (getPathType(path) != PathType.DOCUMENT) {
            return false;
        }
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
        String uriPath = pathToUri(path).getPath();
        for (UriPermission uriPermission : uriPermissions) {
            String itemPath = uriPermission.getUri().getPath();
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

    public static String readDocumentFile(Context context, String path, String file) {
        Uri pathUri = FileTools.pathToUri(path);
        DocumentFile documentPath = DocumentFile.fromTreeUri(App.get(), pathUri);
        if (documentPath != null) {
            DocumentFile df = documentPath.findFile(file);
            try {
                if (df != null) {
                    InputStream is = context.getContentResolver().openInputStream(df.getUri());
                    if (is != null) {
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        return new String(buffer, StandardCharsets.UTF_8);
                    }
                }
            } catch (IOException e) {
                Log.e("readDocumentFile", String.valueOf(e));
                return "";
            }
        }
        return "";
    }

    public static String readFile(String path, String file) {
        try {
            File yourFile = new File(path, file);
            try (FileInputStream stream = new FileInputStream(yourFile)) {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                return Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                Log.e("readFile", e.toString());
            }
        } catch (Exception e) {
            Log.e("readFile", e.toString());
            return "";
        }
        return "";
    }

    public static byte[] readByteFile(File yourFile) {
        try {
            if (yourFile.length() > 0) {
                byte[] bytes = new byte[(int) yourFile.length()];
                try {
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(yourFile));
                    DataInputStream dis = new DataInputStream(bis);
                    dis.readFully(bytes);
                    return bytes;
                } catch (Exception e) {
                    Log.e("readFile", e.toString());
                }
            } else {
                ToastUtils.shortCall(R.string.file_not_available);
            }
        } catch (Exception e) {
            Log.e("readFile", e.toString());
            return null;
        }
        return null;
    }

    private static void saveDocumentFile(Context context, String path, String file, byte[] data){
        Uri pathUri = FileTools.pathToUri(path);
        DocumentFile documentPath = DocumentFile.fromTreeUri(App.get(), pathUri);
        if (documentPath != null) {
            DocumentFile df = documentPath.findFile(file);
            if (df != null) {
                df.delete();
            }
            df = documentPath.createFile("application/*", file);
            try {
                if (df != null) {
                    OutputStream os = context.getContentResolver().openOutputStream(df.getUri());
                    if (os != null) {
                        os.write(data);
                        os.close();
                    }
                }
            } catch (IOException e) {
                Log.e("saveDocumentFile", String.valueOf(e));
            }
        }
    }

    private static void saveFile(String path, String file, byte[] data) {
        try {
            File fileToSave = new File(path, file);
            if (fileToSave.exists()) {
                fileToSave.delete();
            }
            if (fileToSave.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(fileToSave);
                fileOutputStream.write(data);
                fileOutputStream.close();
            }
        } catch (Exception e) {
            Log.e("saveFile", e.toString());
        }
    }

    public static void saveToFile(Context context, String path, String file, byte[] data) {
        if (specialPathReadType != PathType.SHIZUKU) {
            if (!shouldRequestUriPermission(dataPath)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    saveDocumentFile(context, path, file, data);
                } else {
                    saveFile(path, file, data);
                }
            }
        } else {
            saveFile(localPath, file, data);
            ShizukuFileUtil.remove(path + file);
            ShizukuFileUtil.move(localPath + file, mahjongClubFilesPath + file);
        }
    }

    private static List<String> getFileListByFile(String path) {
        List<String> list = new ArrayList<>();
        File dir = new File(path);
        File[] files;
        if ((files = dir.listFiles()) != null) {
            for (File file : files) {
                list.add(file.getName().substring(0, file.getName().length() - 5));
            }
        }
        return list;
    }

    private static List<String> getFileListByDocument(String path) {
        Uri pathUri = pathToUri(path);
        DocumentFile documentFile = DocumentFile.fromTreeUri(App.get(), pathUri);
        List<String> list = new ArrayList<>();
        if (documentFile != null) {
            DocumentFile[] documentFiles = documentFile.listFiles();
            for (DocumentFile df : documentFiles) {
                String f = df.getName();
                if (f != null) list.add(f.substring(0, f.length() - 5));
            }
        }
        return list;
    }

    public static List<String> getFilesList() {
        List<String> list = new ArrayList<>();
        if (specialPathReadType != PathType.SHIZUKU) {
            if (!shouldRequestUriPermission(dataPath)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    list = getFileListByDocument(mahjongClubFilesPath + "LevelsData/");
                } else {
                    list = getFileListByFile(mahjongClubFilesPath + "LevelsData/");
                }
            }
        } else {
            list = ShizukuFileUtil.list(mahjongClubFilesPath + "LevelsData/");
        }
        return list;
    }
/*
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
    }*/
}
