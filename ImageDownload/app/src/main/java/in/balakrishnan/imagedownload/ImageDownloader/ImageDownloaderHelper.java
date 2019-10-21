package in.balakrishnan.imagedownload.ImageDownloader;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import in.balakrishnan.imagedownload.Storage.LocalData;


/**
 * a class created to use the functions related to download the images across entire application
 */
public class ImageDownloaderHelper {
    public static WorkManager mWorkManager = WorkManager.getInstance();
    public static MessagerHandler.IncomingMessageHandler mHandler;
    public static LocalData dbLocalData = new LocalData(ListadoApplication.getAppContext());


    public static UUID createImageDownloadWork(ArrayList<String> urls, int collectionId) {
        return createImageDownloadWork(urls, null, collectionId);
    }


    /**
     * a method to create the work and enque the work
     *
     * @param urls           a list of urls of the images
     * @param downloadStatus a param has the listenerof the download status
     * @param collectionId   a param hs the id of the collection
     * @return
     */
    public static UUID createImageDownloadWork(ArrayList<String> urls, DownloadStatus downloadStatus, int collectionId) {
        /*This handler is reference is used in ImageDownloaderWorker*/
        mHandler = new MessagerHandler.IncomingMessageHandler(urls.size(), downloadStatus);
        OneTimeWorkRequest.Builder listDownloadBuilder =
                new OneTimeWorkRequest.Builder(ImageDownloaderWorker.class)
                        .setConstraints(getConstraint());
        dbLocalData.setStringPreferenceValue("value_0", gsonToString(urls));
        listDownloadBuilder.setInputData(new Data.Builder().putStringArray("value", new String[]{"value_0", collectionId + ""}).build());
        OneTimeWorkRequest work = listDownloadBuilder.build();
        mWorkManager.enqueue(work);
        dbLocalData.setStringPreferenceValue("imageDownloadWork"+collectionId, work.getId().toString());
        return work.getId();
    }

    public static void removeFromWork(int collectionID) {
        dbLocalData.setStringPreferenceValue("imageDownloadWork" + collectionID, "");
    }

    public static void getObserver(int collectionID, Observer<WorkStatus> observe) {
        UUID uuid = UUID.fromString(dbLocalData.getStringPreferenceValue("imageDownloadWork" + collectionID));
        mWorkManager.getStatusById(uuid).observe(ProcessLifecycleOwner.get(),observe);
    }

    public static boolean isWorkRunning(int collectionId) {
        if (TextUtils.isEmpty(dbLocalData.getStringPreferenceValue("imageDownloadWork" + collectionId)))
            return false;
        UUID uuid = UUID.fromString(dbLocalData.getStringPreferenceValue("imageDownloadWork" + collectionId));
        AtomicBoolean b = new AtomicBoolean(false);
        mWorkManager.getStatusById(uuid).observe(ProcessLifecycleOwner.get(),
                workStatus -> {
                    if (workStatus != null) {
                        Log.d("check_function", "isWorkRunning: " + workStatus.getState().name());
                    }
                    if (workStatus != null && workStatus.getState().isFinished()) {
                        removeFromWork(collectionId);
                        b.set(!workStatus.getState().isFinished());
                    }
                }
        );
        return b.get();
    }

    public static Constraints getConstraint() {
        return new Constraints.Builder()
                // For Work that requires Internet Connectivity
                .setRequiredNetworkType(NetworkType.CONNECTED)
//                // For work that require minimum battery level i.e Rendering video
                .setRequiresBatteryNotLow(true)
                .build();
    }

    /**
     * a method to convert Gson to String
     *
     * @param arrayList list of String to convert json
     * @return it return the json
     */
    public static String gsonToString(List<String> arrayList) {
        Gson gson = new Gson();
        return gson.toJson(arrayList);

    }

    /**
     * A interface created to know about the Download status
     */
    public interface DownloadStatus {
        /**
         * a method will be called when the items downloaded
         *
         * @param totalurls          total size of the url size
         * @param downloadPercentage downloaded percentage
         * @param successPercent     success percentage
         * @param failurePercent     failure percentage
         */
        void DownloadedItems(int totalurls, int downloadPercentage, int successPercent, int failurePercent);

        void CurrentDownloadPercentage(int percentage, String url);
    }


}


