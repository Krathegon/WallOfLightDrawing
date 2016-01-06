package com.example.drawingapp.util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class BitmapWorkerTask extends AsyncTask<Uri, Void, Void> {
    private static final String TAG = "BitmapWorkerTask";

    private final WeakReference<ContentResolver> contentResolverReference;
    public Bitmap image;
    public byte[] imageData;

    public BitmapWorkerTask(ContentResolver contentResolver) {
        contentResolverReference = new WeakReference<>(contentResolver);
    }

    @Override
    protected Void doInBackground(Uri... params) {
        final Uri imageUri = params[0];

        ContentResolver contentResolver = contentResolverReference.get();

        try {
            image = decodeBitmapFromUri(imageUri, 200, 200);
        } catch (IOException e) {
            Log.e(TAG, "IO exception", e);
        }

        return null;
    }

    private Bitmap decodeBitmapFromUri(Uri imageUri, int reqWidth, int reqHeight) throws IOException {
        ContentResolver contentResolver = contentResolverReference.get();

        if (contentResolver == null) {
            throw new IOException("There was an error opening the image, please try again later!");
        }

        InputStream is = contentResolver.openInputStream(imageUri);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        is.close();

        // Calculate inSampleSize
        options.inSampleSize = BitmapHelperClass.calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap srcBitmap;
        is = contentResolver.openInputStream(imageUri);

        srcBitmap = BitmapFactory.decodeStream(is, null, options);

        is.close();

        int orientation = BitmapHelperClass.getOrientation(imageUri);

        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
        }
        return srcBitmap;
    }
}
