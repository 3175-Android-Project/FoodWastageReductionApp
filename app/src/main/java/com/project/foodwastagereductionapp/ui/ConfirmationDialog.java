package com.project.foodwastagereductionapp.ui;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import com.project.foodwastagereductionapp.R;

public final class ConfirmationDialog {
    private ConfirmationDialog() {
    }

    public static void show(
            Activity activity,
            CharSequence title,
            CharSequence message,
            CharSequence positiveButton,
            CharSequence negativeButton,
            Runnable onConfirm
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, (dialog, which) -> onConfirm.run());

        if (negativeButton != null && negativeButton.length() > 0) {
            builder.setNegativeButton(negativeButton, null);
        }

        builder.show();
    }

    public static void showLogout(Activity activity, Runnable onConfirm) {
        show(
                activity,
                activity.getString(R.string.dialogLogoutTitle),
                activity.getString(R.string.dialogLogoutMessage),
                activity.getString(R.string.btnLogoutConfirm),
                activity.getString(R.string.btnCancel),
                onConfirm
        );
    }
}
