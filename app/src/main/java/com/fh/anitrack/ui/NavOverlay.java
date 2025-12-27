package com.fh.anitrack.ui;

import static android.widget.Toast.LENGTH_LONG;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.fh.anitrack.R;

public class NavOverlay extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_overlay, container, false);

        View btnClose = view.findViewById(R.id.btnClose);
        FragmentActivity currentActivity = getActivity();
        DrawerLayout drawerLayout = currentActivity.findViewById(R.id.main);

        btnClose.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });


        return view;
    }
}