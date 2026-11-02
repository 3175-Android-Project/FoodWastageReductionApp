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

    private ImageView iconCustomer;
    private ImageView iconManager;

    private TextView txtCustomerTitle;
    private TextView txtCustomerDesc;
    private TextView txtManagerTitle;
    private TextView txtManagerDesc;

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

        // Role Cards
        cardCustomer = findViewById(R.id.cardCustomer);
        cardManager = findViewById(R.id.cardManager);

        // Icons
        iconCustomer = findViewById(R.id.iconCustomer);
        iconManager = findViewById(R.id.iconManager);

        // Text
        txtCustomerTitle = findViewById(R.id.txtCustomerTitle);
        txtCustomerDesc = findViewById(R.id.txtCustomerDesc);

        txtManagerTitle = findViewById(R.id.txtManagerTitle);
        txtManagerDesc = findViewById(R.id.txtManagerDesc);

        // Customer selected by default
        updateRoleUI();

        // Customer click
        cardCustomer.setOnClickListener(v -> {
            selectedRole = "customer";
            updateRoleUI();
        });

        // Manager click
        cardManager.setOnClickListener(v -> {
            selectedRole = "manager";
            updateRoleUI();
        });
    }

    private void updateRoleUI() {

        if (selectedRole.equals("customer")) {

            // Customer selected
            cardCustomer.setBackgroundResource(R.drawable.bg_role_selected);

            iconCustomer.setBackgroundResource(R.drawable.bg_status_badge);
            iconCustomer.setColorFilter(
                    getColor(R.color.background_accent1)
            );

            txtCustomerTitle.setTextColor(
                    getColor(R.color.text_primary)
            );

            txtCustomerDesc.setTextColor(
                    getColor(R.color.text_secondary)
            );

            // Manager unselected
            cardManager.setBackgroundResource(R.drawable.bg_role_default);

            iconManager.setBackgroundResource(R.drawable.bg_role_default_icon);
            iconManager.setColorFilter(
                    getColor(R.color.text_tertiary)
            );

            txtManagerTitle.setTextColor(
                    getColor(R.color.text_secondary)
            );

            txtManagerDesc.setTextColor(
                    getColor(R.color.text_secondary)
            );

        } else {

            // Customer unselected
            cardCustomer.setBackgroundResource(R.drawable.bg_role_default);

            iconCustomer.setBackgroundResource(R.drawable.bg_role_default_icon);
            iconCustomer.setColorFilter(
                    getColor(R.color.text_tertiary)
            );

            txtCustomerTitle.setTextColor(
                    getColor(R.color.text_secondary)
            );

            txtCustomerDesc.setTextColor(
                    getColor(R.color.text_secondary)
            );

            // Manager selected
            cardManager.setBackgroundResource(R.drawable.bg_role_selected);

            iconManager.setBackgroundResource(R.drawable.bg_status_badge);
            iconManager.setColorFilter(
                    getColor(R.color.background_accent1)
            );

            txtManagerTitle.setTextColor(
                    getColor(R.color.text_primary)
            );

            txtManagerDesc.setTextColor(
                    getColor(R.color.text_secondary)
            );
        }
    }
}