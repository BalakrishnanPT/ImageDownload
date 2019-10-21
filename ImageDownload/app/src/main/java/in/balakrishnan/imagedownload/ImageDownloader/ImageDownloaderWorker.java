package in.balakrishnan.imagedownload.ImageDownloader;

import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import in.balakrishnan.imagedownload.ImageHelper.ImageLocalCache;
import in.balakrishnan.imagedownload.Storage.LocalData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageDownloaderWorker extends Worker {
    private String TAG = getClass().getSimpleName();

    @NonNull
    @Override
    public Result doWork() {
        String[] value = getInputData().getStringArray("value");
        if (value == null) return Result.FAILURE;
//        AppDataBase.getAppDatabase(getApplicationContext()).CollectionDao().setDownloadStatus(Integer.parseInt(value[1]), 1);
        LocalData localData = new LocalData(getApplicationContext());
        List<String> g = toList(localData.getStringPreferenceValue(value[0]));
        final CountDownLatch startSignal = new CountDownLatch(g.size());
        for (String s : g) {
            final Result[] result = {Result.SUCCESS};
            final Request request = new Request.Builder()
                    .url(s)
                    .build();

            OkHttpClient client = CustomDownloadClient.getClient(true);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                    Log.d(TAG, "onFailure: " + call.request().url());
                    MessagerHandler.sendMessage(1, "failure");
                    result[0] = Result.FAILURE;
                    startSignal.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) {
//                    Log.d(TAG, "onResponse: " + response.request().url());
                    ImageLocalCache imageLocalCache = new ImageLocalCache(getApplicationContext(), ImageLocalCache.StorageType.CONTEXT_WRAPPER);
                    imageLocalCache.createImageFromBitmap(BitmapFactory.decodeStream(response.body().byteStream()), ImageLocalCache.BucketName.COLLECTION, value[1], s);
                    MessagerHandler.sendMessage(1, "success");
                    result[0] = Result.SUCCESS;
                    startSignal.countDown();
                }
            });
        }
        try {
            startSignal.await();
//            AppDataBase.getAppDatabase(getApplicationContext()).CollectionDao().setDownloadStatus(Integer.parseInt(value[1]), 2);
//            Log.d(TAG, "doWork: " + AppDataBase.getAppDatabase(getApplicationContext()).CollectionDao().getDownloadStatus(Integer.parseInt(value[1])));
            return Result.SUCCESS;
        } catch (InterruptedException e) {
            Log.d("InterruptedException", "doWork: ");
            e.printStackTrace();
//            AppDataBase.getAppDatabase(getApplicationContext()).CollectionDao().setDownloadStatus(Integer.parseInt(value[1]), 1);
//            Log.d(TAG, "doWork: " + AppDataBase.getAppDatabase(getApplicationContext()).CollectionDao().getDownloadStatus(Integer.parseInt(value[1])));
            return Result.RETRY;
        }
    }


    List<String> toList(String g) {
        Gson gson = new Gson();
        return gson.fromJson(g,
                new TypeToken<List<String>>() {
                }.getType());
    }
}