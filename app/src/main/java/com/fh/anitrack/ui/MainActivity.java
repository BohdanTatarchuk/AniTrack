package com.fh.anitrack.ui;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.fh.anitrack.R;
import com.fh.anitrack.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {

    public com.google.android.material.floatingactionbutton.FloatingActionButton fabScrollTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        DrawerLayout drawerLayout = findViewById(R.id.main);

        View headerView = findViewById(R.id.header);
        View btnMenu = headerView.findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    public void setupScrollToTop(androidx.core.widget.NestedScrollView scrollView) {
        if (fabScrollTop == null) return;

        scrollView.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > 10) {
                fabScrollTop.show();
            } else {
                fabScrollTop.hide();
            }
        });

        fabScrollTop.setOnClickListener(v -> {
            scrollView.smoothScrollTo(0, 0);
        });
    }
}