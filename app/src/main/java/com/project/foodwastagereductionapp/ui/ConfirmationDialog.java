package com.project.foodwastagereductionapp.ui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirmation);
        dialog.setCanceledOnTouchOutside(false);

        TextView titleView = dialog.findViewById(R.id.dialogTitle);
        TextView messageView = dialog.findViewById(R.id.dialogMessage);
        Button negativeView = dialog.findViewById(R.id.dialogNegative);
        Button positiveView = dialog.findViewById(R.id.dialogPositive);
        ImageButton closeView = dialog.findViewById(R.id.dialogClose);

        titleView.setText(title);
        messageView.setText(message);
        positiveView.setText(positiveButton);
        positiveView.setOnClickListener(view -> {
            dialog.dismiss();
            onConfirm.run();
        });
        closeView.setOnClickListener(view -> dialog.dismiss());

        if (negativeButton == null || negativeButton.length() == 0) {
            negativeView.setVisibility(android.view.View.GONE);
        } else {
            negativeView.setText(negativeButton);
            negativeView.setOnClickListener(view -> dialog.dismiss());
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.6f;
            window.setAttributes(attributes);
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
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
