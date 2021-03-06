package com.example.drawingapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.Date;

public class SendBitmapTask extends AsyncTask<Bitmap, Exception, Void> {
    private final WeakReference<Client> clientReference;
    private final WeakReference<Context> contextReference;
    private boolean animateImage;

    public SendBitmapTask(Context context, Client client, boolean animateImage) {
        contextReference = new WeakReference<>(context);
        clientReference = new WeakReference<>(client);
        this.animateImage = animateImage;
    }
    
    @Override
    protected void onProgressUpdate(Exception... values) {
        Exception e = values[0];

        Context context = contextReference.get();

        String error = "";
        if(e instanceof UnknownHostException) {
            error = "unknownHostError";
        } else if(e instanceof IOException) {
            error = "connectionInterrupted";
        } else {
            error = "unknownError";
        }

        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Bitmap... params) {
        Bitmap image = params[0];

        if(image == null)
            return null;

        Client client = clientReference.get();

        if(animateImage) {
            int mode = 1, x = 0, y = 0;
            int sampleSize = BitmapHelperClass.calculateInSampleSize(image.getWidth(), image.getHeight(), 88, 88);
            int snippetScale = 88 * sampleSize;

            int pixelsMoving = sampleSize > 1 ? sampleSize / 2 : 1;

            Log.e("WallOfLightApp", "pixelsMoving: " + pixelsMoving + " imageSize: " + image.getWidth() + "x" + image.getHeight());

            Bitmap scaledImage;

            while(!isCancelled()) {
                Date start = new Date(System.currentTimeMillis());

                Bitmap snippet = Bitmap.createBitmap(image, x, y, snippetScale, snippetScale);
                scaledImage = Bitmap.createScaledBitmap(snippet, 88, 88, true);

                try {
                    client.sendImage(scaledImage);
                } catch (Exception e) {
                    publishProgress(e);
                }

                switch(mode) {
                    case 1:
                        x+=pixelsMoving;
                        if(x+snippetScale >= image.getWidth()) {
                            x = image.getWidth()-snippetScale;
                            mode = 2;
                        }
                        break;
                    case 2:
                        y+=pixelsMoving;
                        if(y+snippetScale >= image.getHeight()) {
                            y = image.getHeight()-snippetScale;
                            mode = 3;
                        }
                        break;
                    case 3:
                        x-=pixelsMoving;
                        if(x<0) x=0;
                        if(x == 0)
                            mode = 4;
                        break;
                    case 4:
                        y-=pixelsMoving;
                        if(y<0) y=0;
                        if(y == 0)
                            mode = 1;
                        break;
                }

                Date end = new Date(System.currentTimeMillis());

                int duration = (int)(end.getTime()-start.getTime());

                if(duration < 30) {
                    try {
                        Thread.sleep(30 - duration);
                    } catch (InterruptedException e) {}
                }
            }
        } else {
            Bitmap scaledImage = Bitmap.createScaledBitmap(image, 88, 88, true);
            try {
                client.sendImage(scaledImage);
            } catch (Exception e) {
                publishProgress(e);
            }
        }

        return null;
    }
}
