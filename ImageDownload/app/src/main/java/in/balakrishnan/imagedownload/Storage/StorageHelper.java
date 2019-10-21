package in.balakrishnan.imagedownload.Storage;

import android.os.Environment;

/**
 * Created by Jaison on 01/02/17.
 */

public class StorageHelper {

    private static boolean externalStorageReadable, externalStorageWritable;

    /**
     * A method to know the ExternalStorage is Readable
     *
     * @return it returns true if the externalStorage is Readable
     */
    public static boolean isExternalStorageReadable() {
        checkStorage();
        return externalStorageReadable;
    }

    /**
     * A method to know the ExternalStorage is Writable
     * @return it returns true if the externalStorage is Writable
     */
    public static boolean isExternalStorageWritable() {
        checkStorage();
        return externalStorageWritable;
    }


    /**
     * A method to know the ExternalStorage is Writable and Readable
     * @return it returns true if the externalStorage is both Writable & Readable
     */
    public static boolean isExternalStorageReadableAndWritable() {
        checkStorage();
        return externalStorageReadable && externalStorageWritable;
    }


    private static void checkStorage() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            externalStorageReadable = externalStorageWritable = true;
        } else if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            externalStorageReadable = true;
            externalStorageWritable = false;
        } else {
            externalStorageReadable = externalStorageWritable = false;
        }
    }

}