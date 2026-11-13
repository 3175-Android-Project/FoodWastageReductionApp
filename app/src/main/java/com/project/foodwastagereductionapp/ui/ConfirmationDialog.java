package com.project.foodwastagereductionapp.ui;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import com.project.foodwastagereductionapp.R;

public final class ConfirmationDialog {
    private ConfirmationDialog() {
    }

    public static void showLogout(Activity activity, Runnable onConfirm) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialogLogoutTitle)
                .setMessage(R.string.dialogLogoutMessage)
                .setNegativeButton(R.string.btnCancel, null)
                .setPositiveButton(R.string.btnLogoutConfirm, (dialog, which) -> onConfirm.run())
                .show();
    }
}
