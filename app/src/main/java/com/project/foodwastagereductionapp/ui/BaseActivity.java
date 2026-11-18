package com.project.foodwastagereductionapp.ui;

import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Base activity providing global keyboard dismissal on touch outside EditText
 * across all derived activities.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        KeyboardUtils.handleDispatchTouchEvent(this, ev);
        return super.dispatchTouchEvent(ev);
    }
}
