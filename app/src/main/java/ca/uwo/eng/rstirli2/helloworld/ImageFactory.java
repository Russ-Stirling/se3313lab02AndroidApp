package ca.uwo.eng.rstirli2.helloworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * To use this class, follow the instructions here and add the Gradle dependency as specified in
 * the assignment document. 
 * 
 * Singleton instance that downloads imeages from a {@code String} URL into a {@link Bitmap}.
 *
 * To get an instance, you must pass a {@link Context} instance. For example, from within
 * {@link android.app.Activity#onCreate(Bundle)}, this will return an instance. :
 * <code>
 *     // in Activity#onCreate(Bundle)
 *     final ImageFactory imgFactory = ImageFactory.getInstance(getApplicationContext());
 * </code>
 *
 * To get a new image, you need to create an instance of {@link ImageFactory.ImageFoundCallback} and
 * pass it to {@link ImageFactory#fetchImage(String, ImageFoundCallback)}.
 *
 * All callbacks and operations made can be considered "in the main thread." This means you can
 * modify the UI within callbacks for this class (in fact, this is what you <em>should</em> do.
 */
public class ImageFactory {

    /**
     * Interface used to pass callbacks into the
     * {@link ImageFactory#fetchImage(String, ImageFoundCallback)} method for asynchronous image
     * fetching.
     */
    public interface ImageFoundCallback {

        /**
         * Called when an image is successfully fetched, the resultant {@link Bitmap} instance is
         * passed.
         *
         * @param bitmap Loaded image, can not be be {@code null}.
         */
        public abstract void onSuccess(final Bitmap bitmap);

        /**
         * Called if the URL fails to resolve.
         * @param reason The exception thrown when trying to resolve, typically a
         *               {@link VolleyError}
         */
        public abstract void onFailure(final Throwable reason);

    }

    // Static singleton instance
    private static ImageFactory instance = null;
    private static Context      context = null;

    /// Instance lock used for initialization fo the singleton
    private static final Lock instanceLock = new ReentrantLock();

    /**
     * Performs singleton initialization as required. Returns the singleton instance. This method is
     * synchronized internally.
     *
     * @param context Android context, usually filled by {@link Activity#getApplicationContext()}
     * @return Singleton instance of {@code ImageFactory}
     */
    public static ImageFactory getInstance(Context context) {
        // Use a double-checked lock example to make sure that the instance is only initialized once
        if (ImageFactory.instance == null || ImageFactory.context == null)
        {
            instanceLock.lock();
            try {
                if (null == ImageFactory.instance || null == ImageFactory.context) {
                    if (context == null) {
                        throw new NullPointerException("context == null");
                    }

                    ImageFactory.context = context; // context must be first

                    ImageFactory.instance = new ImageFactory();
                }
            } finally {
                // always besure to unlock the Lock
                instanceLock.unlock();
            }
        }

        return instance;
    }

    // Request queue used internally for http requests
    private final RequestQueue requestQueue;

    // Use an LruCache to stop unecessary fetches from happening.
    private final Lock cacheLock = new ReentrantLock(true);
    private final LruCache<String, Bitmap> cache = new LruCache<>(20);

    // Private constructor that can not be called outside of {@link #getInstance(Context)}
    private ImageFactory() {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    /**
     * Asynchronously fetches an Image as specified by imageUrl.
     *
     * @param imageUrl The URL to fetch an image from
     * @param callback A callback construct that will asynchronously be called on success or failure
     *                 respectively.
     */
    public void fetchImage(final String imageUrl,
                           final ImageFoundCallback callback) {
        // check the parameters
        if (null == imageUrl) {
            throw new NullPointerException("imageUrl == null");
        }

        if (imageUrl.isEmpty()) {
            throw new IllegalArgumentException("imageUrl is empty");
        }

        if (null == callback) {
            throw new NullPointerException("callback == null");
        }

        // Get the cache lock and check for the bitmap
        // we try to keep the lock for as little time as possible.
        Bitmap cacheBitmap;
        cacheLock.lock();
        try {
            cacheBitmap = cache.get(imageUrl);
        } finally {
            cacheLock.unlock();
        }

        if (null != cacheBitmap) {
            // the cached response exists, thus we can immediately return.
            callback.onSuccess(cacheBitmap);
        } else {
            // create the image request
            final ImageRequest request =
                    new ImageRequest(imageUrl, new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            if (response != null) {
                                // since we got this far, we know that the response wasn't
                                // (likely) previously cached, thus add it. Even if it was
                                // cached by someone else during this request, it really doesn't
                                // matter -- the cache expects this behaviour.

                                cacheLock.lock();
                                try {
                                    cache.put(imageUrl, response);
                                } finally {
                                    cacheLock.unlock();
                                }

                                callback.onSuccess(response);
                            } else {
                                callback.onFailure(new IllegalStateException("bitmap response was null"));
                            }
                        }

                    }, 0, 0, null, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onFailure(error);
                        }
                    });

            // add it to the async queue
            requestQueue.add(request);
        }

    }

}
