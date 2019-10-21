package in.balakrishnan.imagedownload.ImageDownloader;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

public class MessagerHandler {

    /**
     * This method used to send current status of downloaded image
     *
     * @param messageID unique id
     * @param params    message to sent
     */
    public static void sendMessage(int messageID, @Nullable String params) {
        String TAG = "sendMessage";
        Messenger mActivityMessenger = null;

        if (ImageDownloaderHelper.mHandler != null) {
            mActivityMessenger = new Messenger(ImageDownloaderHelper.mHandler);
        } else {
            //Logic for progressing the notification
        }
        // If this service is launched by the JobScheduler, there's no callback Messenger. It
        // only exists when the MainActivity calls startService() with the callback in the Intent.

        if (mActivityMessenger == null)
            return;

        Message m = Message.obtain();
        m.what = messageID;
        m.obj = params;

        try {
            mActivityMessenger.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }

    }

    /**
     * This handler is used for getting Success / Failure message from Work Manager Downloading image
     */
    protected static class IncomingMessageHandler extends Handler {
        int total, success = 0, failure = 0, percentage;
        ImageDownloaderHelper.DownloadStatus downloadStatus;
        private String TAG = "IncomingMessageHandler";

        public IncomingMessageHandler(int total, ImageDownloaderHelper.DownloadStatus status) {
            this.downloadStatus = status;
            this.total = total;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (msg.obj.toString().equalsIgnoreCase("success")) {
                        success += 1;
                    }
                    if (msg.obj.toString().equalsIgnoreCase("failure")) {
                        failure += 1;
                    }
                    Log.d(TAG, "handleMessage: percentage" + (((success + failure) * 100) / total));
                    if (downloadStatus != null)
                        downloadStatus.DownloadedItems(total, ((success + failure) * 100) / total, success, failure);
                    break;
                case 2:
                    String t = msg.obj.toString();
                    String[] tt = t.split(";");
                    percentage = Integer.parseInt(tt[0]);
                    if (downloadStatus != null)
                        downloadStatus.CurrentDownloadPercentage(percentage, tt[1]);
            }
        }

    }

}
