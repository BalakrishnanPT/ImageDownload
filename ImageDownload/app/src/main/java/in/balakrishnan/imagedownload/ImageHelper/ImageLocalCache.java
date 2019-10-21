package in.balakrishnan.imagedownload.ImageHelper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;


import in.balakrishnan.imagedownload.Storage.StorageHelper;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static in.balakrishnan.imagedownload.ImageHelper.ImageLocalCache.StorageType.CONTEXT_WRAPPER;
import static in.balakrishnan.imagedownload.ImageHelper.ImageLocalCache.StorageType.EXTERNAL_FILE_DIR;
import static in.balakrishnan.imagedownload.ImageHelper.ImageLocalCache.StorageType.INTERNAL_FILE_DIR;

/**
 * Created by Jaison.
 */

public class ImageLocalCache {

    public static int IMAGE_CACHE_PERMISSION_REQUEST_CODE = 1001;
    private static String BUCKET_COLLECTION = "collection";
    private static String BUCKET_PLACE = "place";
    private static String BUCKET_RESTAURANT = "restaurant";
    public String ENABLE_PERMISSION = "Go to settings and enable permissions";
    String TAG = "ImageLocalCache";
    private String AppName = "Listado";
    private String STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private Context context;
    private StorageType type;

    private boolean isPermissionGranted;
    private boolean showRequestPopup;
    private Fragment currentFragment;

    public ImageLocalCache(Context context, StorageType type) {
        this.context = context;
        this.type = type;
        checkValue();
    }

    public ImageLocalCache(Context context, StorageType type, boolean onCreatePermissionCheck) {
        this.context = context;
        this.type = type;
        if (onCreatePermissionCheck)
            isStoragePermissionGranted(true);
        checkValue();
    }

    private void checkValue() {

        switch (type) {
            case CONTEXT_WRAPPER:
                Log.d(TAG, "checkValue: CONTEXT_WRAPPER");
                break;
            case INTERNAL_FILE_DIR:
                Log.d(TAG, "checkValue: INTERNAL_FILE_DIR");
                break;
            case EXTERNAL_FILE_DIR:
                Log.d(TAG, "checkValue: EXTERNAL_FILE_DIR");
                break;
            case ENVIRONMENTAL_EXTERNAL_STORAGE:
                Log.d(TAG, "checkValue: ENVIRONMENTAL_EXTERNAL_STORAGE");
                break;
            case ENVIRONMENTAL_EXTERNAL_STORAGE_PUBLIC:
                Log.d(TAG, "checkValue: ENVIRONMENTAL_EXTERNAL_STORAGE_PUBLIC");
                break;
        }
    }


    public boolean isImageAvailable(BucketName bucketName, String file_name) {
        return isImageAvailable(bucketName, "", file_name);
    }

    /**
     * To check the image availability
     *
     * @param bucketName
     * @param file_name
     * @return
     */
    public boolean isImageAvailable(BucketName bucketName, String uniqueID, String file_name) {
        Log.d("ImageLocalCache", "checkImageAvailability: " + file_name);
        if (TextUtils.isEmpty(file_name))
            return false;

        File file = new File(getFile(bucketName, uniqueID), getFilteredFileName(file_name));
        boolean flag;
        if (file.exists()) {
            // Log.i("file", "exists");
            flag = true;
        } else {
            // Log.i("file", "not exist");
            flag = false;
        }

        return flag;
    }

    /**
     * To store an Imgae
     *
     * @param bucket
     * @param file_name
     * @param fileUri
     * @return
     */
    public boolean storeImage(BucketName bucket, String uniqueID, String file_name, Uri fileUri) {
        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
        return createImageFromBitmap(bitmap, bucket, file_name);
    }

    public boolean createImageFromBitmap(Bitmap bmp, BucketName bucket, String fileName) {
        return createImageFromBitmap(bmp, bucket, "", fileName);
    }


    /**
     * To create an image from the bitmap
     *
     * @param bmp      - bitmap
     * @param bucket   - Name of the directory
     * @param fileName - Name of the file
     * @return
     */
    public boolean createImageFromBitmap(Bitmap bmp, BucketName bucket, String uniqueID, String fileName) {

        if (isAvailable()) {
            File file = new File(getFile(bucket, uniqueID), getFilteredFileName(fileName));

            if (!file.exists()) {
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 60, out);
                    out.flush();
                    out.close();
                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                Log.d(TAG, "File Already exist: " + fileName);
        }
        return false;

    }

    public Bitmap getImage(BucketName bucketName, String file_name) {
        return getImage(bucketName, "", file_name);
    }

    /**
     * To retrieve the image as bitmap
     *
     * @param bucketName - Name of the directory
     * @param file_name  - Name of the file
     * @return
     */
    public Bitmap getImage(BucketName bucketName, String uniqueID, String file_name) {
        if (isAvailable()) {
            File file = new File(getFile(bucketName, uniqueID), getFilteredFileName(file_name));

            Log.d(TAG, "getImage: " + file.getAbsolutePath());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = 2;

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            if (bitmap != null)
                return bitmap;
            else
                return null;
        } else
            return null;
    }

    /**
     * To delete file
     *
     * @param bucketName Name of the directory
     * @param file_name  Name of the file
     */
    public void removeImage(BucketName bucketName, String file_name) {

        if (isAvailable()) {
            File file = new File(getFile(bucketName), getFilteredFileName(file_name));
            if (file.exists()) {
                Log.d(TAG, "file Exist: ");
                if (file.delete())
                    Log.d(TAG, "file deleted: ");
                else
                    Log.d(TAG, "file not deleted: ");
            } else
                Log.d(TAG, "file not Exist: ");
        } else
            Log.d(TAG, "Permission missing ");

    }

    /**
     * Get the file as a bitmap
     *
     * @param file Name of the file
     * @return
     */
    public Bitmap getImage(File file) {

        if (isAvailable()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = 2;

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            if (bitmap != null)
                return bitmap;
            else
                return null;

        } else
            return null;

    }

    /**
     * Used to rotate a bitmap image
     *
     * @param img    Source Image
     * @param degree Rotation degree value
     * @return
     */
    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }


    public File getFilePath(BucketName bucket) {
        return getFilePath(bucket, "");
    }

    public File getFilePath(BucketName bucket, String uniqueID) {
        String bucketName = getBucketName(bucket) + uniqueID;
        Log.d(TAG, "getFilePath: " + bucketName + uniqueID);
        File path = null;

        switch (type) {
            case CONTEXT_WRAPPER:
                ContextWrapper contextWrapper = new ContextWrapper(context);

                try {
                    path = contextWrapper.getDir(bucketName, Context.MODE_PRIVATE);
                    Log.d(TAG, "getFilePath: " + path.getPath());
                } catch (Exception e) {
                    e.printStackTrace();

                }
                return path;
            case INTERNAL_FILE_DIR:
                path = new File(context.getFilesDir() + File.separator + AppName + File.separator + bucketName + File.separator);
                Log.d(TAG, "getFIlePath:INTERNAL_FILE_DIR" + path);
                return path;
            case EXTERNAL_FILE_DIR:
                path = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + AppName + File.separator + bucketName + File.separator);
                Log.d(TAG, "getFIlePath:EXTERNAL_FILE_DIR" + path);
                return path;
            case ENVIRONMENTAL_EXTERNAL_STORAGE:
                if (isAvailable() && StorageHelper.isExternalStorageReadableAndWritable()) {

                    path = new File(Environment.getExternalStorageDirectory() + File.separator + AppName + File.separator + bucketName + File.separator);
                    Log.d(TAG, "getFIlePath: ENVIRONMENTAL_EXTERNAL_STORAGE " + path);
                    return path;
                } else
                    return path;
            case ENVIRONMENTAL_EXTERNAL_STORAGE_PUBLIC:
                if (isAvailable() && StorageHelper.isExternalStorageReadableAndWritable()) {

                    path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + AppName + File.separator + bucketName + File.separator);
                    Log.d(TAG, "getFIlePath: ENVIRONMENTAL_EXTERNAL_STORAGE_PUBLIC " + path);
                    return path;
                } else
                    return path;
        }

        return path;
    }


    public File getFile(BucketName bucketName) {
        return getFile(bucketName, "");
    }

    public File getFile(BucketName bucketName, String uniqueID) {
        //File path = new File(getFIlePath(context,bucketID));

        File path = getFilePath(bucketName, uniqueID);

        if (path != null && !path.exists()) {
            Log.d(TAG, "getFile: not exists");
            boolean flag = path.mkdirs();

            if (flag)
                Log.d(TAG, "getFile: Dir created successfully");
            else
                Log.d(TAG, "getFile: Dir created failed");
        } else
            Log.d(TAG, "getFile: path exist");

        return path;
    }

    /**
     * To separate the file name from the path
     *
     * @param name
     * @return
     */
    public String getFilteredFileName(String name) {
        String fileName = name;

        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }

        return fileName;
    }

    /**
     * To separate the file name from the URL
     *
     * @param url - Source url of the file
     * @return
     */
    public String getFileNameFromURL(String url) {
        return url.substring(url.lastIndexOf('/') + 1, url.length());

    }

    /**
     * To check whether the needed permission is granted
     *
     * @return
     */
    public boolean isStoragePermissionGranted() {
        return isStoragePermissionGranted(showRequestPopup);
    }

    /**
     * To check whether the needed permission is granted
     *
     * @param showRequestPopup - Flag to show the permission check popup
     * @return
     */
    public boolean isStoragePermissionGranted(boolean showRequestPopup) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(context, STORAGE_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                if (showRequestPopup) {
                    if (currentFragment != null) {
                        currentFragment.requestPermissions(new String[]{STORAGE_PERMISSION}, IMAGE_CACHE_PERMISSION_REQUEST_CODE);
                    } else
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{STORAGE_PERMISSION}, IMAGE_CACHE_PERMISSION_REQUEST_CODE);
                }


                return false;
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    /**
     * Permission request result handling
     *
     * @param requestCode   - Permission request identification code
     * @param permissions   - Requested permissions
     * @param grantResults- Status of the requested permissions
     */
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == IMAGE_CACHE_PERMISSION_REQUEST_CODE && grantResults.length > 0)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                isPermissionGranted = true;
            else {
                if (currentFragment != null) {
                    if (!currentFragment.shouldShowRequestPermissionRationale(STORAGE_PERMISSION)) {
                        showRequestPermissionRationale();
                    } else {
                        Log.d(TAG, "onRequestPermissionsResult: Permission is not provided");

                    }
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, STORAGE_PERMISSION)) {
                        showRequestPermissionRationale();
                    } else {
                        Log.d(TAG, "onRequestPermissionsResult: Permission is not provided");

                    }
                }

            }

    }

    public void showRequestPermissionRationale() {
        Log.i("Go to settings", "and enable permissions");
        Toast.makeText(context, ENABLE_PERMISSION, Toast.LENGTH_LONG).show();
        /*

        // To redirect the settings-> app details page

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);*/
    }

    // Setter functions

    // To set whether the util need to request for the needed permission or not

    public void setShowRequestPopup(boolean showRequestPopup) {
        this.showRequestPopup = showRequestPopup;
    }

    // To show the source is Activity or Fragment

    public void setCurrentFragment(Fragment currentFragment) {
        this.currentFragment = currentFragment;
    }

    /**
     * To check the permission is available, if needed
     *
     * @return
     */
    public boolean isAvailable() {
        if (type == CONTEXT_WRAPPER || type == INTERNAL_FILE_DIR || type == EXTERNAL_FILE_DIR)
            return true;
        else
            return isStoragePermissionGranted();
    }

    /**
     * To get the drawable as bitmap by using the resource ID
     *
     * @param resourceID - Auto gen ID of the drawable file
     * @return
     */
    public Bitmap getDrawableAsBitmap(int resourceID) {
        Bitmap mySource;

        if (Build.VERSION.SDK_INT < LOLLIPOP_MR1)
            mySource = ((BitmapDrawable) context.getResources().getDrawable(resourceID)).getBitmap();
        else
            mySource = ((BitmapDrawable) Objects.requireNonNull(ResourcesCompat.getDrawable(context.getResources(), resourceID, null))).getBitmap();

        return mySource;
    }

    public String getBucketName(BucketName bucketName) {
        switch (bucketName) {
            case COLLECTION:
                return BUCKET_COLLECTION;
            case PLACE:
                return BUCKET_PLACE;
            case RESTAURANT:
                return BUCKET_RESTAURANT;
            default:
                return BUCKET_COLLECTION;
        }
    }

    public enum StorageType {
        CONTEXT_WRAPPER,
        INTERNAL_FILE_DIR,
        EXTERNAL_FILE_DIR,
        ENVIRONMENTAL_EXTERNAL_STORAGE,
        ENVIRONMENTAL_EXTERNAL_STORAGE_PUBLIC
    }

    public enum BucketName {
        COLLECTION,
        PLACE,
        RESTAURANT
    }
}
