package com.nagato.agent.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.nagato.agent.R;

/**
 * Debug screen: Agent status, system logs, verbose output.
 * For developers. Not the primary user experience.
 */
public class DebugFragment extends Fragment {

    private RecyclerView mLogRecycler;
    private TextView mAgentStatus;
    private Button mStartAgentBtn;
    private Button mStopAgentBtn;

    public DebugFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debug, container, false);

        mLogRecycler = view.findViewById(R.id.debug_log_recycler);
        mAgentStatus = view.findViewById(R.id.agent_status_text);
        mStartAgentBtn = view.findViewById(R.id.agent_start_btn);
        mStopAgentBtn = view.findViewById(R.id.agent_stop_btn);

        mStartAgentBtn.setOnClickListener(v -> startAgent());
        mStopAgentBtn.setOnClickListener(v -> stopAgent());

        updateStatus();
        return view;
    }

    private void updateStatus() {
        mAgentStatus.setText("Agent: ○ Stopped | Projects: 0 active");
        // TODO: wire to service binder
    }

    private void startAgent() {
        // TODO: trigger hermes agent start via service
    }

    private void stopAgent() {
        // TODO: kill agent process via service
    }
}
