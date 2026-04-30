package com.nagato.agent.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nagato.agent.R;
import com.nagato.agent.app.adapter.BottomNavAdapter;
import com.nagato.agent.app.fragments.ChatFragment;
import com.nagato.agent.app.fragments.DebugFragment;
import com.nagato.agent.app.fragments.MeFragment;
import com.nagato.agent.app.fragments.TuiFragment;
import com.nagato.agent.app.TermuxService;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity for Nagato Agent.
 * Bottom navigation with 4 destinations: Chat, TUI, Debug, Me.
 * Chat is the default/hero experience.
 *
 * Termux service binding is preserved from upstream for running
 * agent processes in the background.
 */
public final class NagatoActivity extends AppCompatActivity implements ServiceConnection {

    private static final int NAV_CHAT = R.id.nav_chat;
    private static final int NAV_TUI = R.id.nav_tui;
    private static final int NAV_DEBUG = R.id.nav_debug;
    private static final int NAV_ME = R.id.nav_me;

    private ViewPager2 mViewPager;
    private BottomNavigationView mBottomNav;
    private BottomNavAdapter mPagerAdapter;

    private TermuxService mTermuxService;
    private boolean mServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nagato);

        // Bind to TermuxService (keeps agent processes alive)
        Intent serviceIntent = new Intent(this, TermuxService.class);
        startService(serviceIntent);
        bindService(serviceIntent, this, 0);

        mViewPager = findViewById(R.id.view_pager);
        mBottomNav = findViewById(R.id.bottom_navigation);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new ChatFragment());    // 0 - Chat (Hero)
        fragments.add(new TuiFragment());     // 1 - TUI
        fragments.add(new DebugFragment());   // 2 - Debug
        fragments.add(new MeFragment());      // 3 - Me

        mPagerAdapter = new BottomNavAdapter(this, fragments);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setUserInputEnabled(false); // Disable swipe; use bottom nav only

        mBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == NAV_CHAT) {
                mViewPager.setCurrentItem(0, false);
                return true;
            } else if (id == NAV_TUI) {
                mViewPager.setCurrentItem(1, false);
                return true;
            } else if (id == NAV_DEBUG) {
                mViewPager.setCurrentItem(2, false);
                return true;
            } else if (id == NAV_ME) {
                mViewPager.setCurrentItem(3, false);
                return true;
            }
            return false;
        });

        // Default to Chat tab
        mBottomNav.setSelectedItemId(NAV_CHAT);
        mViewPager.setCurrentItem(0, false);

        // Auto-hide bottom nav when keyboard opens
        getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            // Simplified; real impl uses WindowInsetsCompat
            return insets;
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (service instanceof TermuxService.LocalBinder) {
            mTermuxService = ((TermuxService.LocalBinder) service).service;
            mServiceBound = true;
            // Notify fragments that service is ready
            for (Fragment f : mPagerAdapter.getFragments()) {
                if (f instanceof ServiceConnection) {
                    // Handled per-fragment if needed
                }
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mTermuxService = null;
        mServiceBound = false;
    }

    @Override
    protected void onDestroy() {
        if (mServiceBound) {
            unbindService(this);
        }
        super.onDestroy();
    }

    public TermuxService getTermuxService() {
        return mTermuxService;
    }

    public void showBottomNav(boolean show) {
        mBottomNav.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
