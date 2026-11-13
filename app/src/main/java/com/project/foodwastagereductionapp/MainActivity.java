package com.project.foodwastagereductionapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.auth.LoginActivity;
import com.project.foodwastagereductionapp.auth.AuthNavigator;
import com.project.foodwastagereductionapp.auth.AuthTasks;
import com.project.foodwastagereductionapp.auth.SessionManager;
import com.project.foodwastagereductionapp.auth.SignUpActivity;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.User;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tryAutoLogin();

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void tryAutoLogin() {
        SessionManager session = new SessionManager(this);
        long userId = session.getUserId();
        if (userId == -1 || !session.shouldAutoLogin()) return;

        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        AuthTasks.run(this, null, () -> {
            User user = db.getUser(userId);
            if (user == null || !user.emailVerified) return null;
            return AuthNavigator.destination(db, user);
        }, screen -> {
            if (screen == null) {
                session.clear();
                return;
            }
            AuthNavigator.open(this, screen, userId);
        });
    }
}