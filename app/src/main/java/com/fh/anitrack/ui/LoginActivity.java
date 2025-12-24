package com.fh.anitrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fh.anitrack.R;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText emailEditText = findViewById(R.id.loginEmailEditText);
        EditText passwordEditText = findViewById(R.id.loginPasswordEditText);
        TextView forgotPasswordTextView = findViewById(R.id.loginForgotPassword);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        TextView singUpTextView = findViewById(R.id.doNotHaveAnAccountText);

        singUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SingUpActivity.class);
            startActivity(intent);
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            handleLogin();
        });
    }

    private void handleLogin() {
        // TODO: call an auth service
        // For testing, navigate to MediaDetailActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}