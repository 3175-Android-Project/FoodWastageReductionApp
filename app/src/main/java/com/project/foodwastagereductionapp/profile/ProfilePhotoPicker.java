package com.project.foodwastagereductionapp.profile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.auth.AuthTasks;

/** Shared optional photo field for registration and account editing. */
public final class ProfilePhotoPicker {
    private static final String STATE_PHOTO = "profilePhotoUri";
    private String photoUri;
    private final ImageView preview;
    private final boolean restored;
    private boolean busy;
    private boolean changed;
    public ProfilePhotoPicker(AppCompatActivity activity, Bundle state) {
        preview = activity.findViewById(R.id.imgProfilePhoto);
        restored = state != null && state.containsKey(STATE_PHOTO);
        photoUri = restored ? state.getString(STATE_PHOTO, "") : "";
        ActivityResultLauncher<String> picker = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri == null) return;
                    busy = true;
                    activity.findViewById(R.id.btnChoosePhoto).setEnabled(false);
                    AuthTasks.run(activity, null, () -> ProfilePhotoStore.importPhoto(activity, uri), saved -> {
                        photoUri = saved;
                        changed = true;
                        busy = false;
                        activity.findViewById(R.id.btnChoosePhoto).setEnabled(true);
                        show();
                    }, error -> {
                        busy = false;
                        activity.findViewById(R.id.btnChoosePhoto).setEnabled(true);
                        Toast.makeText(activity, R.string.msgPhotoUnavailable, Toast.LENGTH_LONG).show();
                    });
                });
        activity.findViewById(R.id.btnChoosePhoto).setOnClickListener(v -> picker.launch("image/*"));
        activity.findViewById(R.id.btnRemovePhoto).setOnClickListener(v -> {
            if (!busy) { photoUri = ""; changed = true; show(); }
        });
        show();
    }
    public boolean isBusy() { return busy; }
    public String getPhotoUri() { return photoUri; }
    public void setInitial(String value) { if (!restored && !changed) { photoUri = value == null ? "" : value; show(); } }
    public void saveState(Bundle state) { state.putString(STATE_PHOTO, photoUri); }
    private void show() { ProfilePhotoStore.show(preview, photoUri); }
}
