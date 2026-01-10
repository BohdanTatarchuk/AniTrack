package com.fh.anitrack.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fh.anitrack.R;
import com.fh.anitrack.api.AniTrackConfig;
import com.fh.anitrack.api.AuthRepository;
import com.fh.anitrack.ui.MainActivity;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AuthRepository.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView forgotPasswordTextView = findViewById(R.id.loginForgotPassword);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        TextView signUpTextView = findViewById(R.id.doNotHaveAnAccountText);

        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SingUpActivity.class);
            startActivity(intent);
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> startAniListLogin());
    }

    private void startAniListLogin() {
        String authUrl = AniTrackConfig.AUTH_URL
                + "?client_id=" + AniTrackConfig.CLIENT_ID
                + "&redirect_uri=" + AniTrackConfig.REDIRECT_URI
                + "&response_type=token";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
        startActivity(intent);
    }
}