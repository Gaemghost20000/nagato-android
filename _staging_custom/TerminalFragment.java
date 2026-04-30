package com.nagato.agent.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.nagato.agent.R;
import com.nagato.agent.app.NagatoActivity;
import com.nagato.agent.view.TerminalView;

/**
 * Terminal tab: wraps the existing Termux terminal + session drawer.
 * Managed by NagatoActivity which binds to TermuxService.
 */
public class TerminalFragment extends Fragment {

    private TerminalView mTerminalView;
    private DrawerLayout mDrawerLayout;

    public TerminalFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        mTerminalView = view.findViewById(R.id.terminal_view);
        mDrawerLayout = view.findViewById(R.id.drawer_layout);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // NagatoActivity passes the active session here after service bind
        if (getActivity() instanceof NagatoActivity) {
            ((NagatoActivity) getActivity()).attachTerminalView(mTerminalView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTerminalView = null;
        mDrawerLayout = null;
    }
}
