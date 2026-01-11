package com.fh.anitrack.ui.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fh.anitrack.R;
import com.fh.anitrack.api.AniTrackConfig;
import com.fh.anitrack.api.AuthRepository;
import com.fh.anitrack.ui.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if already logged in
        if (AuthRepository.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        webView = findViewById(R.id.loginWebView);
        progressBar = findViewById(R.id.loginProgressBar);

        setupWebView();
        loadAniListLogin();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        String newUserAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36";
        webView.getSettings().setUserAgentString(newUserAgent);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.startsWith(AniTrackConfig.REDIRECT_URI)) {
                    handleUrlFragment(url);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadAniListLogin() {

        String finalUrl = AniTrackConfig.AUTH_URL + "?client_id=" + AniTrackConfig.CLIENT_ID + "&response_type=token";

        android.util.Log.d("AniTrack", "URL: " + finalUrl);

        android.webkit.CookieManager.getInstance().removeAllCookies(null);

        webView.loadUrl(finalUrl);
    }

    private void handleUrlFragment(String url) {
        // token is in the URL after #
        try {
            Uri uri = Uri.parse(url);
            String fragment = uri.getFragment();

            //getting the token from the redirect url
            if (fragment != null && fragment.contains("access_token=")) {
                String[] params = fragment.split("&");
                for (String param : params) {
                    if (param.startsWith("access_token=")) {
                        String token = param.split("=")[1];

                        // save token
                        AuthRepository.getInstance(this).saveAccessToken(token);

                        // nav to main
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse login token", Toast.LENGTH_SHORT).show();
        }
    }
}