package in.balakrishnan.imagedownload.ImageHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import in.balakrishnan.imagedownload.R;

import static in.balakrishnan.imagedownload.ImageHelper.ImageLocalCache.StorageType.CONTEXT_WRAPPER;


/**
 * A class created for the Image related funtions to use across the entire application
 * Created by Jaison.
 */
public class ImageHelper {
    Context context;
    ImageLocalCache imageLocalCache;

    public ImageHelper(Context context) {
        this.context = context;
        imageLocalCache = new ImageLocalCache(context, CONTEXT_WRAPPER);
    }

    /**
     * A method to set the Thumb image in ImageView
     *
     * @param ivBannerImage a param has the value of the imageView
     * @param filePath      a param has the url of the image
     * @param bucketName    a param has the value of the type of Collection or Place
     * @param uniqueID      a param has the value of the collection id
     */
    public void setBannerImage(final ImageView ivBannerImage, final String filePath,
                               final ImageLocalCache.BucketName bucketName, String uniqueID) {
        setBannerImage(ivBannerImage, filePath, bucketName, uniqueID, false, null);
    }


    /**
     * A method to set the Thumb default image in ImageView if the Thumbimage not set or not found or failed
     * @param ivBannerImage a param has the value of the imageView
     * @param filePath a param has the url of the image
     * @param bucketName  a param has the value of the type of Collection or Place
     * @param defaultbanner a param has the drawable of the default image
     * @param collectionID  a param has the value of the collection id
     */
    public void setBannerImage(final ImageView ivBannerImage, final String filePath,
                               final ImageLocalCache.BucketName bucketName, String collectionID, Drawable defaultbanner) {
        setBannerImage(ivBannerImage, filePath, bucketName, collectionID, false, defaultbanner);
    }


    /**
     * A method to set the Thumb default image in ImageView if the Thumbimage not set or not found or failed
     * @param ivBannerImage a param has the value of the imageView
     * @param filePath a param has the url of the image
     * @param bucketName  a param has the value of the type of Collection or Place
     * @param defaultbanner a param has the drawable of the default image
     * @param collectionID  a param has the value of the collection id
     */
    public void setBannerImage(final ImageView ivBannerImage, final String filePath,
                               final ImageLocalCache.BucketName bucketName, final String collectionID, boolean isOffline, final Drawable defaultbanner) {
        ivBannerImage.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placelist_place_holder, null));


        Bitmap collectionbanner = imageLocalCache.getImage(bucketName, collectionID, filePath);

        if (collectionbanner != null) {
            ivBannerImage.setImageBitmap(collectionbanner);
        } else {

            if (!isOffline) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);

                Glide.with(context)
                        .asBitmap()
                        .load(filePath).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (defaultbanner != null)
                            ivBannerImage.setImageDrawable(defaultbanner);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        ivBannerImage.setImageBitmap(resource);
                        imageLocalCache.createImageFromBitmap(resource, bucketName, collectionID, filePath);
                        return true;
                    }
                }).into(ivBannerImage);
            } else {
                if (defaultbanner != null)
                    ivBannerImage.setImageDrawable(defaultbanner);
            }

        }
    }
}
