package com.project.foodwastagereductionapp.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import com.project.foodwastagereductionapp.R;
import java.io.IOException;
import java.io.InputStream;

/** Resource-name URIs survive reinstalls and resource ID changes between builds. */
public final class LocalImageLoader {
    private static final int MAX_PHOTO_EDGE = 1024;

    private LocalImageLoader() { }

    public static void load(ImageView view, String imageUri) {
        Uri uri = Uri.parse(imageUri);
        if (!"android.resource".equals(uri.getScheme())) {
            // Keep the existing content:// and file:// upload behavior.
            view.setImageURI(uri);
            return;
        }
        // decodeStream avoids drawable density upscaling. Sample large bundled photos
        // (including the 6171px takoyaki photo) before allocating their pixel buffers.
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream stream = view.getContext().getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(stream, null, options);
            }
            options.inSampleSize = 1;
            while (Math.max(options.outWidth, options.outHeight) / options.inSampleSize > MAX_PHOTO_EDGE) {
                options.inSampleSize *= 2;
            }
            options.inJustDecodeBounds = false;
            try (InputStream stream = view.getContext().getContentResolver().openInputStream(uri)) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
                if (bitmap != null) {
                    view.setImageBitmap(bitmap);
                    return;
                }
            }
        } catch (IOException | SecurityException | IllegalArgumentException ignored) {
            // A removed or invalid resource should never leave a recycled row's old photo.
        }
        view.setImageResource(R.drawable.icon_food);
    }
}
