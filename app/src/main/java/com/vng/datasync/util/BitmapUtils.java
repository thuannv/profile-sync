package com.vng.datasync.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.Callable;

import rx.Observable;

/**
 * @author thuannv
 * @version 1.0
 * @since 21/04/2017
 */

public final class BitmapUtils {

    private BitmapUtils() {
        throw new UnsupportedOperationException("Not allow instantiating object.");
    }

    /**
     * @param candidate     - Bitmap to check
     * @param targetOptions - Options that have the out* value populated
     * @return true if <code>candidate</code> can be used for inBitmap re-use with
     * <code>targetOptions</code>
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {
        if (!PlatformUtils.hasKitKat()) {
            // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
            return candidate.getWidth() == targetOptions.outWidth
                    && candidate.getHeight() == targetOptions.outHeight
                    && targetOptions.inSampleSize == 1;
        }

        // From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
        // is smaller than the reusable bitmap candidate allocation byte count.
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
        return byteCount <= candidate.getAllocationByteCount();
    }

    /**
     * Return the byte usage per pixel of a bitmap based on its configuration.
     *
     * @param config The bitmap configuration.
     * @return The byte usage per pixel.
     */
    private static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     *
     * @param res       The resources object containing the image data
     * @param resId     The resource id of the image data
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     * that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // END_INCLUDE (read_bitmap_dimensions)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode sample bitmap and resize to required width, height in that its aspect's ratio is
     * maintained.
     *
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResourceWithResize(Resources res, int resId, int reqWidth, int reqHeight) {
        Bitmap bitmap = decodeSampledBitmapFromResource(res, resId, reqWidth, reqHeight);

        return scaleBitmap(bitmap, reqWidth, reqHeight);
    }

    /**
     * Scale bitmap to the required width, height. The scaled bitmap will maintain its original
     * aspect's ratio.
     *
     * @param origin    Original bitmap.
     * @param reqWidth  Required scale width.
     * @param reqHeight Required scale height.
     * @return Scale bitmap that maintains original aspect's ratio.
     */
    private static Bitmap scaleBitmap(Bitmap origin, int reqWidth, int reqHeight) {
        int originWidth = origin.getWidth();
        int originHeight = origin.getHeight();
        float originRatio = 1.0f * originWidth / originHeight;
        float desiredRatio = 1.0f * reqWidth / reqHeight;
        float scaleFactor = 1.0f;

        // If desire image and origin image have different ratio
        // Origin is width > height and desired is width < height
        if (originRatio > 1.0f && desiredRatio < 1.0f) {
            scaleFactor = 1.0f * reqWidth / originWidth;
            reqHeight = (int) (originHeight * scaleFactor);
        }

        // Origin is width < height and desired is width > height
        if (originRatio < 1.0f && desiredRatio > 1.0f) {
            scaleFactor = 1.0f * reqHeight / originHeight;
            reqWidth = (int) (originWidth * scaleFactor);
        }

        // Origin and desired have same type of orientation
        int realWidth = reqWidth;
        int realHeight = (int) (realWidth / originRatio);
        if (realHeight > reqHeight) {
            realHeight = reqHeight;
            realWidth = (int) (realHeight * originRatio);
        }

        return Bitmap.createScaledBitmap(origin, realWidth, realHeight, true);
    }

    /**
     * Decode bitmap from resource then scale to required size. Scale image will maintain original
     * aspect's ratio.
     *
     * @param res       Android resource
     * @param resId     Resource id of image
     * @param reqWidth  Required width
     * @param reqHeight Required height
     * @return A bitmap from resource id with required size
     */
    public static Bitmap decodeBitmapWithSize(Resources res, int resId, int reqWidth, int reqHeight) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId);
        return scaleBitmap(bitmap, reqWidth, reqHeight);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename  The full path of the file to decode
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     * that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth       The requested width of the resulting bitmap
     * @param reqHeight      The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     * that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    public static Bitmap decodeSampleBitmapFromStream(InputStream is, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(is, null, options);
    }

    public static Bitmap decodeSampleBitmapFromUrl(String strURL, int reqWidth, int reqHeight) {
        if (TextUtils.isEmpty(strURL)) {
            return null;
        }

        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final URL url = new URL(strURL);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            inputStream = url.openStream();
            BitmapFactory.decodeStream(inputStream, null, options);
            IoUtils.safeClose(inputStream);

            // Calculate inSampleSize
            options.inSampleSize = BitmapUtils.calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            inputStream = url.openStream();
            bitmap = BitmapFactory.decodeStream(url.openStream(), null, options);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.safeClose(inputStream);
        }
        return bitmap;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that is a power of 2 and will result in the final decoded bitmap
     * having a width and height equal to or larger than the requested width and height.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // BEGIN_INCLUDE (calculate_sample_size)
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / inSampleSize;

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        return inSampleSize;
        // END_INCLUDE (calculate_sample_size)
    }

    /**
     * Get the size in bytes of a bitmap in a Bitmap. Note that from Android 4.4 (KitKat)
     * onward this returns the allocated memory size of the bitmap which can be larger than the
     * actual bitmap data byte count (in the case it was re-used).
     *
     * @param bitmap
     * @return size in bytes
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static int getBitmapSize(Bitmap bitmap) {
        // From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
        // larger than bitmap byte count.
        if (PlatformUtils.hasKitKat()) {
            return bitmap.getAllocationByteCount();
        }

        if (PlatformUtils.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }

        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static Bitmap getRoundedCorner(Bitmap bitmap, int radius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = radius;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    public static File saveBitmap(Context context,
                                  Bitmap bitmap,
                                  String directory,
                                  String filename,
                                  CompressConfigs compressConfigs) {

        if (directory == null) {
            directory = context.getCacheDir().getAbsolutePath();
        } else {
            // Check if the given directory exists or try to create it.
            File file = new File(directory);
            if (!file.isDirectory() && !file.mkdirs()) {
                return null;
            }
        }

        long byteCount = BitmapUtils.getBitmapSize(bitmap);

        final long max = compressConfigs.getMaxSize();
        int compressRatio = 100;
        if (byteCount > max) {
            compressRatio = (int) (100.0f * max / byteCount);
        }

        File file = null;
        OutputStream os = null;
        try {
            Bitmap.CompressFormat format = compressConfigs.getCompressFormat();
            filename = filename + compressConfigs.getExtension();
            file = new File(directory, filename);
            os = new FileOutputStream(file);
            bitmap.compress(format, compressRatio, os);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            IoUtils.safeClose(os);
        }
        return file;
    }

    public static File saveBitmap(Context context,
                                  Bitmap bitmap,
                                  File parentDir,
                                  String fileName,
                                  CompressConfigs compressConfigs) {

        return saveBitmap(context, bitmap, parentDir.getAbsolutePath(), fileName, compressConfigs);
    }

    public static Observable<File> asyncSaveBitmap(final Context context,
                                                   final Bitmap bitmap,
                                                   final String directory,
                                                   final String filename,
                                                   final CompressConfigs compressConfigs) {
        return Observable.fromCallable(new Callable<File>() {
            @Override
            public File call() throws Exception {
                return saveBitmap(context, bitmap, directory, filename, compressConfigs);
            }
        });
    }

    public static Observable<File> asyncSaveBitmap(final Context context,
                                                   final Bitmap bitmap,
                                                   final File parentDir,
                                                   final String fileName,
                                                   final CompressConfigs compressConfigs) {
        return Observable.fromCallable(new Callable<File>() {
            @Override
            public File call() throws Exception {
                return saveBitmap(context, bitmap, parentDir, fileName, compressConfigs);
            }
        });
    }

    public static Uri getImageUri(Context context, Bitmap.CompressFormat format, int quality, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
      try {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          bitmap.compress(format, quality, outputStream);

          String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "", null);
          IoUtils.safeClose(outputStream);
          return Uri.parse(path);
      }catch (Exception e){}

      return null;
    }

    /**
     * {@link CompressConfigs}
     */
    public static final class CompressConfigs {
        private final long mMaxSize;

        private final Bitmap.CompressFormat mCompressFormat;

        public CompressConfigs(long maxSize, Bitmap.CompressFormat compressFormat) {
            mMaxSize = maxSize;
            mCompressFormat = compressFormat;
        }

        public long getMaxSize() {
            return mMaxSize;
        }

        public Bitmap.CompressFormat getCompressFormat() {
            return mCompressFormat;
        }

        public String getExtension() {
            switch (mCompressFormat) {
                case PNG:
                    return ".png";

                case JPEG:
                default:
                    return ".jpg";
            }
        }
    }
}