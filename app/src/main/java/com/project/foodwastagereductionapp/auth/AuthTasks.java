package com.project.foodwastagereductionapp.auth;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Serializes DB/password work and avoids updating a finished or rotated-away activity. */
public final class AuthTasks {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private AuthTasks() { }

    public static <T> void run(Activity activity, View button, Callable<T> work, Consumer<T> done) {
        if (button != null) {
            if (!button.isEnabled()) return;
            button.setEnabled(false);
        }
        WORKER.execute(() -> {
            try {
                T result = work.call();
                MAIN.post(() -> {
                    if (activity.isFinishing() || activity.isDestroyed()) return;
                    if (button != null) button.setEnabled(true);
                    done.accept(result);
                });
            } catch (Exception error) {
                Log.e("GrabbitDatabase", "Local account operation failed", error);
                MAIN.post(() -> {
                    if (activity.isFinishing() || activity.isDestroyed()) return;
                    if (button != null) button.setEnabled(true);
                    Toast.makeText(activity, error instanceof AppDatabaseHelper.DuplicateEmailException
                            ? R.string.msgEmailExists : R.string.msgDatabaseError, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
