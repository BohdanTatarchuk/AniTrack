package com.fh.anitrack.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.fh.anitrack.R;
import com.fh.anitrack.ui.home.HomeFragment;
import com.fh.anitrack.ui.browse.BrowsePage;
import com.fh.anitrack.ui.notifications.NotificationsPage;
import com.fh.anitrack.ui.profile.ProfileAnimeList;
import com.fh.anitrack.ui.profile.ProfileMangaList;
import com.fh.anitrack.ui.profile.ProfileOverview;
import com.fh.anitrack.ui.settings.SettingsProfile;

public class NavOverlay extends Fragment {

    private DrawerLayout drawerLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_overlay, container, false);

        drawerLayout = getActivity().findViewById(R.id.main);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> closeDrawer());

        view.findViewById(R.id.navHome).setOnClickListener(v -> replaceFragment(new HomeFragment()));
        view.findViewById(R.id.navProfile).setOnClickListener(v -> replaceFragment(new ProfileOverview()));
        view.findViewById(R.id.navAnimeList).setOnClickListener(v -> replaceFragment(new ProfileAnimeList()));
        view.findViewById(R.id.navMangaList).setOnClickListener(v -> replaceFragment(new ProfileMangaList()));
        view.findViewById(R.id.navBrowse).setOnClickListener(v -> replaceFragment(new BrowsePage()));
        view.findViewById(R.id.navSettings).setOnClickListener(v -> replaceFragment(new SettingsProfile()));
        view.findViewById(R.id.navNotifications).setOnClickListener(v -> replaceFragment(new NotificationsPage()));

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();

        closeDrawer();
    }

    private void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
    }
}