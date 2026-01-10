package com.fh.anitrack.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.fh.anitrack.api.AniTrackConfig;
import com.fh.anitrack.api.AuthRepository;
import com.fh.anitrack.ui.MainActivity;

public class AuthCallbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(AniTrackConfig.REDIRECT_URI)) {
            String fragment = uri.getFragment();

            if (fragment != null && fragment.contains("access_token=")) {
                String[] params = fragment.split("&");
                for (String param : params) {
                    if (param.startsWith("access_token=")) {
                        String token = param.split("=")[1];

                        AuthRepository.getInstance(this).saveAccessToken(token);


                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            }
        }
        finish();
    }
}