package com.nagato.agent.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nagato.agent.R;

/**
 * Me screen: Profile, settings, project management, theme.
 */
public class MeFragment extends Fragment {

    private Button mNewProjectBtn;
    private Button mSettingsBtn;

    public MeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);

        mNewProjectBtn = view.findViewById(R.id.me_new_project_btn);
        mSettingsBtn = view.findViewById(R.id.me_settings_btn);

        mNewProjectBtn.setOnClickListener(v -> showNewProjectDialog());
        mSettingsBtn.setOnClickListener(v -> openSettings());

        return view;
    }

    private void showNewProjectDialog() {
        // TODO: dialog to name + configure new project
    }

    private void openSettings() {
        // TODO: open settings activity
    }
}
