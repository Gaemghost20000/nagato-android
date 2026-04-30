package com.nagato.agent.app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nagato.agent.app.fragments.TerminalFragment;
import com.nagato.agent.app.fragments.ChatFragment;
import com.nagato.agent.app.fragments.TuiFragment;

/**
 * ViewPager2 adapter for Nagato Activity tabs.
 * Tabs: Terminal | Chat | TUI
 */
public class MainPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 3;
    public static final int TAB_TERMINAL = 0;
    public static final int TAB_CHAT = 1;
    public static final int TAB_TUI = 2;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_TERMINAL:
                return new TerminalFragment();
            case TAB_CHAT:
                return new ChatFragment();
            case TAB_TUI:
                return new TuiFragment();
            default:
                return new TerminalFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
