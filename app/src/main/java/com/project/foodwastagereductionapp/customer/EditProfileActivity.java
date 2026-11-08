package com.project.foodwastagereductionapp.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.txtEditProfileTitle);

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // back to Profile

        // Save Changes
        findViewById(R.id.btnSaveChanges).setOnClickListener(v -> {
            // TODO: save to database later
            Toast.makeText(this, R.string.msgProfileSaved, Toast.LENGTH_SHORT).show();
            finish(); // back to Profile
        });
    }
}