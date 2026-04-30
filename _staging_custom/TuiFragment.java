package com.nagato.agent.app.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nagato.agent.R;
import com.nagato.agent.app.NagatoActivity;
import com.nagato.agent.app.termux_legacy.TermuxTerminalSessionActivityClient;
import com.nagato.agent.view.TerminalView;
import com.nagato.agent.app.terminal.TermuxSession;

/**
 * TUI tab: launches `hermes tui` in a dedicated terminal session.
 * Auto-starts the TUI when the TermuxService is bound.
 */
public class TuiFragment extends Fragment implements ServiceConnection {

    private TerminalView mTerminalView;
    private boolean mBound = false;
    private static final String HERMES_TUI_CMD = "/data/data/com.nagato.agent/files/usr/bin/hermes tui";

    public TuiFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tui, container, false);
        mTerminalView = view.findViewById(R.id.tui_terminal_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Bind to TermuxService via parent activity
        if (getActivity() instanceof NagatoActivity) {
            NagatoActivity activity = (NagatoActivity) getActivity();
            Intent intent = new Intent(activity, com.nagato.agent.app.TermuxService.class);
            activity.bindService(intent, this, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Let NagatoActivity handle the actual session creation and view attachment
        if (getActivity() instanceof NagatoActivity) {
            ((NagatoActivity) getActivity()).attachTuiView(mTerminalView, HERMES_TUI_CMD);
        }
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBound = false;
    }

    @Override
    public void onDestroyView() {
        if (mBound && getActivity() != null) {
            getActivity().unbindService(this);
        }
        mTerminalView = null;
        super.onDestroyView();
    }
}
