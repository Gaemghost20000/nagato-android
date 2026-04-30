package com.nagato.agent.app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * BottomNavigationView adapter for NagatoActivity.
 * Maps 4 nav items to fragments: Chat | TUI | Debug | Me
 */
public class BottomNavAdapter extends FragmentStateAdapter {

    private final List<Fragment> mFragments;

    public BottomNavAdapter(@NonNull FragmentActivity fragmentActivity,
                            @NonNull List<Fragment> fragments) {
        super(fragmentActivity);
        mFragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }

    public List<Fragment> getFragments() {
        return mFragments;
    }
}
