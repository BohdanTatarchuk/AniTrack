package com.fh.anitrack.ui;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.fh.anitrack.R;
import com.fh.anitrack.ui.browse.BrowsePage;
import com.fh.anitrack.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {

    public com.google.android.material.floatingactionbutton.FloatingActionButton fabScrollTop;
    
    private View headerView;
    private View headerLogo;
    private ImageView btnSearch;
    private LinearLayout expandedSearchContainer;
    private EditText headerSearchEditText;
    private ImageView btnSearchSubmit;
    private boolean isSearchExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        DrawerLayout drawerLayout = findViewById(R.id.main);

        headerView = findViewById(R.id.header);
        View btnMenu = headerView.findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        
        setupHeaderSearch();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }
    
    private void setupHeaderSearch() {
        headerLogo = headerView.findViewById(R.id.headerLogo);
        btnSearch = headerView.findViewById(R.id.btnSearch);
        expandedSearchContainer = headerView.findViewById(R.id.expandedSearchContainer);
        headerSearchEditText = headerView.findViewById(R.id.headerSearchEditText);
        btnSearchSubmit = headerView.findViewById(R.id.btnSearchSubmit);
        
        // Click on search icon to expand
        btnSearch.setOnClickListener(v -> expandSearch());
        
        // Click on submit button to search
        btnSearchSubmit.setOnClickListener(v -> performHeaderSearch());
        
        // Handle keyboard search action
        headerSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performHeaderSearch();
                return true;
            }
            return false;
        });
        
        // Handle focus loss to collapse search
        headerSearchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && isSearchExpanded) {
                collapseSearch();
            }
        });
    }
    
    private void expandSearch() {
        isSearchExpanded = true;
        headerLogo.setVisibility(View.GONE);
        btnSearch.setVisibility(View.GONE);
        expandedSearchContainer.setVisibility(View.VISIBLE);
        headerSearchEditText.requestFocus();
        
        // Show keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(headerSearchEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }
    
    public void collapseSearch() {
        isSearchExpanded = false;
        expandedSearchContainer.setVisibility(View.GONE);
        headerLogo.setVisibility(View.VISIBLE);
        btnSearch.setVisibility(View.VISIBLE);
        headerSearchEditText.setText("");
        headerSearchEditText.clearFocus();
        
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(headerSearchEditText.getWindowToken(), 0);
        }
    }
    
    private void performHeaderSearch() {
        String query = headerSearchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            // Navigate to BrowsePage with search query
            BrowsePage browsePage = BrowsePage.newInstance(query);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    )
                    .replace(R.id.fragment_container, browsePage)
                    .addToBackStack(null)
                    .commit();
        }
        collapseSearch();
    }

    @Override
    public void onBackPressed() {
        if (isSearchExpanded) {
            collapseSearch();
        } else {
            super.onBackPressed();
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