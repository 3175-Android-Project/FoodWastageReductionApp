package com.project.foodwastagereductionapp.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.widget.ImageView;
import com.project.foodwastagereductionapp.R;
import java.io.*;
import java.util.UUID;

/** Bounded, metadata-free copies keep photos available without persistent gallery permissions. */
public final class ProfilePhotoStore {
    private ProfilePhotoStore() { }
    public static String importPhoto(Context context, Uri uri) throws IOException {
        File directory = new File(context.getFilesDir(), "profile_photos");
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IOException("Photo storage unavailable");
        File source = File.createTempFile("selected-", ".tmp", directory);
        File destination = new File(directory, UUID.randomUUID() + ".jpg");
        Bitmap bitmap = null;
        boolean saved = false;
        try {
            try (InputStream input = context.getContentResolver().openInputStream(uri);
                 OutputStream output = new FileOutputStream(source)) {
                if (input == null) throw new IOException("Image unavailable");
                byte[] buffer = new byte[8192];
                int count, total = 0;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > 20 * 1024 * 1024) throw new IOException("Image too large");
                    output.write(buffer, 0, count);
                }
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(source.getPath(), options);
            if (options.outWidth <= 0 || options.outHeight <= 0) throw new IOException("Invalid image");
            options.inSampleSize = 1;
            while (Math.max(options.outWidth, options.outHeight) / options.inSampleSize > 1024) {
                options.inSampleSize *= 2;
            }
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(source.getPath(), options);
            if (bitmap == null) throw new IOException("Invalid image");
            int orientation = ExifInterface.ORIENTATION_NORMAL;
            try {
                orientation = new ExifInterface(source.getPath()).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            } catch (IOException ignored) { /* Some supported bitmaps do not have EXIF metadata. */ }
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.setScale(-1, 1); break;
                case ExifInterface.ORIENTATION_ROTATE_180: matrix.setRotate(180); break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL: matrix.setScale(1, -1); break;
                case ExifInterface.ORIENTATION_TRANSPOSE: matrix.setRotate(90); matrix.postScale(-1, 1); break;
                case ExifInterface.ORIENTATION_ROTATE_90: matrix.setRotate(90); break;
                case ExifInterface.ORIENTATION_TRANSVERSE: matrix.setRotate(270); matrix.postScale(-1, 1); break;
                case ExifInterface.ORIENTATION_ROTATE_270: matrix.setRotate(270); break;
            }
            float scale = Math.min(1f, 512f / Math.max(bitmap.getWidth(), bitmap.getHeight()));
            matrix.postScale(scale, scale);
            Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (resized != bitmap) { bitmap.recycle(); bitmap = resized; }
            try (OutputStream output = new FileOutputStream(destination)) {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) throw new IOException("Unable to save photo");
            }
            saved = true;
            return Uri.fromFile(destination).toString();
        } finally {
            if (bitmap != null) bitmap.recycle();
            source.delete();
            if (!saved) destination.delete();
        }
    }

    public static void show(ImageView image, String uri) {
        image.setImageTintList(null);
        image.setPadding(0, 0, 0, 0);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setImageDrawable(null);
        if (uri != null && !uri.isEmpty()) {
            try { image.setImageURI(Uri.parse(uri)); } catch (SecurityException ignored) { }
        }
        if (image.getDrawable() == null) {
            image.setImageResource(R.drawable.icon_user);
            image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            int padding = Math.round(18 * image.getResources().getDisplayMetrics().density);
            image.setPadding(padding, padding, padding, padding);
        }
    }
}
