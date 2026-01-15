package com.fh.anitrack.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fh.anitrack.R;
import com.google.android.material.button.MaterialButton;

public class SingUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sing_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText emailEditText = findViewById(R.id.singUpEmailEditText);
        EditText usernameEditText = findViewById(R.id.singUpUsernameEditText);
        EditText passwordEditText = findViewById(R.id.singUpPasswordEditText);
        EditText confirmPasswordEditText = findViewById(R.id.singUpConfirmPasswordEditText);
        CheckBox termsOfService = findViewById(R.id.termsOfServiceCheckbox);
        TextView termsOfServiceText = findViewById(R.id.termsOfServiceText);
        MaterialButton singUpButton = findViewById(R.id.singUpButton);
        TextView resendVerificationText = findViewById(R.id.resendVerificationEmailTextView);
        TextView alreadyHaveAnAccountText = findViewById(R.id.alreadyHaveAnAccountTextView);

        alreadyHaveAnAccountText.setOnClickListener(v -> {
            Intent intent = new Intent(SingUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        termsOfServiceText.setMovementMethod(LinkMovementMethod.getInstance());

        singUpButton.setOnClickListener(v -> {
            handleSingUp();
        });
    }

    private void handleSingUp() {
        String signUpUrl = "https://anilist.co/signup";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(signUpUrl));
        startActivity(intent);
    }
}