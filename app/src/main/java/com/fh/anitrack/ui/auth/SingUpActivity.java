package com.fh.anitrack.ui.auth;

import android.content.Intent;
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

    private EditText emailEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private CheckBox termsOfService;

    private TextView termsOfServiceText;
    private MaterialButton singUpButton;
    private TextView resendVerificationText;

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

        emailEditText = findViewById(R.id.singUpEmailEditText);
        usernameEditText = findViewById(R.id.singUpUsernameEditText);
        passwordEditText = findViewById(R.id.singUpPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.singUpConfirmPasswordEditText);
        termsOfService = findViewById(R.id.termsOfServiceCheckbox);
        termsOfServiceText = findViewById(R.id.termsOfServiceText);
        singUpButton = findViewById(R.id.singUpButton);
        resendVerificationText = findViewById(R.id.resendVerificationEmailTextView);
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
        //TODO call an auth service here
    }
}