package com.project.foodwastagereductionapp.auth;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;

public class ChooseRoleActivity extends AppCompatActivity {

    private String selectedRole = "customer";

    private LinearLayout cardCustomer;
    private LinearLayout cardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_role);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.app_name);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Role cards
        cardCustomer = findViewById(R.id.cardCustomer);
        cardManager = findViewById(R.id.cardManager);

        cardCustomer.setOnClickListener(v -> {
            selectedRole = "customer";

            cardCustomer.setBackgroundResource(R.drawable.bg_role_selected);
            cardManager.setBackgroundResource(R.drawable.bg_role_default);
        });

        cardManager.setOnClickListener(v -> {
            selectedRole = "manager";

            cardManager.setBackgroundResource(R.drawable.bg_role_selected);
            cardCustomer.setBackgroundResource(R.drawable.bg_role_default);
        });
    }
}