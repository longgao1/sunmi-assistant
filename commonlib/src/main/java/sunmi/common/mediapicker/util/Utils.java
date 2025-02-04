package sunmi.common.mediapicker.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.commonlibrary.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import sunmi.common.utils.FileHelper;

/**
 * Utils method for TakePhoto.
 *
 * @author Jacob
 * @date 17-11-24
 */
@SuppressWarnings({"unused"})
public final class Utils {

    private static final String URI_SCHEME_FILE = "file";

    private static final String TAG = "Utils";
    private static long time;

    public static void timeLogStart() {
        time = System.currentTimeMillis();
    }

    public static void timeLogEnd(String tag, String msg) {
        Log.d(tag, msg + " time spend: " + (System.currentTimeMillis() - time) + "ms.");
        time = 0;
    }

    public static String getCheckedNum(int num, int limit) {
        return (num != 0 && limit != 0) ? " (" + num + "/" + limit + ")" : "";
    }

    public static boolean isFileUri(Uri uri) {
        return URI_SCHEME_FILE.equalsIgnoreCase(uri.getScheme());
    }

    @Nullable
    private static File getDefaultPhotoCacheDir(Context context, boolean isSavePhoto) {
        File path = new File(FileHelper.SDCARD_CACHE_IMAGE_PATH);
        if (!FileUtils.createOrExistDir(path, false, true)) {
            Log.e(TAG, "Cache path prepare FAILED: " + path);
            return null;
        }
        return path;
    }

    public static File getNewImage(Context context, boolean isSavePhoto)
            throws IOException {
        File targetPath = getDefaultPhotoCacheDir(context, isSavePhoto);
        if (targetPath == null) {
            throw new IOException("Target path prepare FAILED!");
        }
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault())
                .format(new Date());
        String imageFileName = "photo_" + timeStamp;
        return File.createTempFile(imageFileName, ".jpg", targetPath);
    }

    public static void galleryAddPic(Context context, Uri file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(file);
        context.sendBroadcast(mediaScanIntent);
    }

    public static void takePhoto(ContextWrap context, File photoFile) {
        // Start activity for take photo.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Context appContext = context.getContext();
        ComponentName camera = intent.resolveActivity(appContext.getPackageManager());
        if (camera != null) {
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(appContext,
                        appContext.getPackageName() + ".fileprovider", photoFile);
                appContext.grantUriPermission(camera.getPackageName(), photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                ActivityUtils.startActivityForResult(context, intent, Constant.REQUEST_TAKE_PHOTO);
            }
        } else {
            Log.e(TAG, "No camera app installed.");
            Toast.makeText(appContext, context.getContext().getString(R.string.picker_toast_no_camera),
                    Toast.LENGTH_SHORT).show();
        }
    }

}
