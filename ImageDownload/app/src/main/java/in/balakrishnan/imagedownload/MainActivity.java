package in.balakrishnan.imagedownload;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.UUID;

import in.balakrishnan.imagedownload.ImageDownloader.ImageDownloaderHelper;
import in.balakrishnan.imagedownload.Storage.LocalData;

import static in.balakrishnan.imagedownload.ImageDownloader.ImageDownloaderHelper.removeFromWork;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    LocalData localData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        localData = new LocalData(this);
        sampleImageDownloadCall();
    }

    /**
     * A method to download the multiple imaages check
     */
    private void sampleImageDownloadCall() {
        ArrayList<String> images = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            images.add("https://picsum.photos/200/300?image=" + i);
        }
        Log.d(TAG, "onCreate: in job creation");
        UUID uuid = ImageDownloaderHelper.createImageDownloadWork(images, 1);
        statusCallBack(1);
    }

    /**
     * A method to know the Download Status
     * @param collectionId the param has the value of the collection id
     */
    private void statusCallBack(int collectionId) {
        Log.d(TAG, "statusCallBack: " + ImageDownloaderHelper.isWorkRunning(collectionId));
        ImageDownloaderHelper.getObserver(collectionId, workStatus -> {

            Log.d(TAG, "statusCallBack: " + workStatus);
            switch (workStatus.getState()) {
                case CANCELLED:
                    removeFromWork(collectionId);
                    break;
                case SUCCEEDED:
                    Log.d(TAG, "InitiateImageDownload: Image Download is Success");
                    removeFromWork(collectionId);
                    break;
                case RUNNING:
                    break;
                case BLOCKED:
                    break;
                case FAILED:
                    removeFromWork(collectionId);
                    break;
                case ENQUEUED:
                    break;
            }
        });
    }


}
