package com.nagato.agent.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.nagato.agent.R;

/**
 * Branded wrapper that shows a purple header bar + terminal frame.
 * Delegates the actual terminal to TermuxActivity via Intent.
 * Shows booting status while Termux initializes.
 */
public class NagatoShellActivity extends Activity {

    private FrameLayout mTerminalContainer;
    private TextView mStatusText;
    private boolean mBooted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate our branded shell
        View shell = LayoutInflater.from(this).inflate(R.layout.activity_nagato_shell, null);
        setContentView(shell);

        mTerminalContainer = shell.findViewById(R.id.terminal_container);
        mStatusText = shell.findViewById(R.id.status_text);

        // Launch the real TermuxActivity inside our container
        launchTermux();
    }

    private void launchTermux() {
        Intent intent = new Intent(this, TermuxActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Update status after a delay (Termux takes time to boot)
        mTerminalContainer.postDelayed(() -> {
            mStatusText.setText("Ready");
            mBooted = true;
        }, 3000);
    }
}
